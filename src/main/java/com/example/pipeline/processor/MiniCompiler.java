package com.example.pipeline.processor;

import com.example.pipeline.model.Error;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiniCompiler {

    private static final Pattern VAR_PATTERN = Pattern.compile("^VAR\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*(-?\\d+)$");
    private static final Pattern OUT_PATTERN = Pattern.compile("^OUT\\s+([A-Za-z_][A-Za-z0-9_]*)$");
    private static final Pattern SET_PATTERN = Pattern.compile("^SET\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*([^\\s]+)\\s*([+\\-*/])\\s*([^\\s]+)$");

    private int tempCounter = 1;

    public List<String> compile(
            String expandedCode,
            List<Error> errors,
            Map<String, Integer> symbolTable
    ) {
        List<String> irCode = new ArrayList<>();
        String[] lines = expandedCode.split("\\r?\\n");
        Set<String> modifiedVars = new HashSet<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("VAR ") || line.equals("VAR")) {
                Matcher m = VAR_PATTERN.matcher(line);
                if (m.matches()) {
                    String varName = m.group(1);
                    if (symbolTable.containsKey(varName)) {
                        errors.add(new Error(i + 1, "Variable already declared: " + varName, "SEMANTIC_ERROR"));
                    } else {
                        int value = Integer.parseInt(m.group(2));
                        symbolTable.put(varName, value);
                        irCode.add(varName + " = " + value);
                    }
                } else {
                    errors.add(new Error(i + 1, "Invalid VAR syntax", "SYNTAX_ERROR"));
                }
            } else if (line.startsWith("OUT ") || line.equals("OUT")) {
                Matcher m = OUT_PATTERN.matcher(line);
                if (m.matches()) {
                    String varName = m.group(1);
                    if (symbolTable.containsKey(varName)) {
                        // Dead value detection: warn if variable was never updated via SET
                        if (!modifiedVars.contains(varName)) {
                            errors.add(new Error(i + 1, "Variable " + varName + " is never updated before OUT", "WARNING"));
                        }
                        irCode.add("OUT " + varName);
                    } else {
                        errors.add(new Error(i + 1, "Undeclared variable " + varName, "SEMANTIC_ERROR"));
                    }
                } else {
                    errors.add(new Error(i + 1, "Invalid OUT syntax", "SYNTAX_ERROR"));
                }
            } else if (line.startsWith("SET ") || line.equals("SET")) {
                Matcher m = SET_PATTERN.matcher(line);
                if (m.matches()) {
                    String target = m.group(1);
                    String op1 = m.group(2);
                    String operator = m.group(3);
                    String op2 = m.group(4);

                    if (!symbolTable.containsKey(target)) {
                        errors.add(new Error(i + 1, "Undeclared variable " + target, "SEMANTIC_ERROR"));
                        continue;
                    }

                    boolean valid1 = validateOperand(op1, symbolTable, errors, i + 1);
                    boolean valid2 = validateOperand(op2, symbolTable, errors, i + 1);

                    if (!valid1 || !valid2) {
                        continue;
                    }

                    boolean isOp1Literal = isNumeric(op1);
                    boolean isOp2Literal = isNumeric(op2);

                    if (isOp1Literal && isOp2Literal) {
                        // Constant folding
                        try {
                            int val1 = Integer.parseInt(op1);
                            int val2 = Integer.parseInt(op2);
                            int result = evaluate(val1, val2, operator);
                            irCode.add(target + " = " + result);
                            symbolTable.put(target, result);
                            modifiedVars.add(target);
                        } catch (ArithmeticException e) {
                            errors.add(new Error(i + 1, "Arithmetic error: " + e.getMessage(), "SEMANTIC_ERROR"));
                        }
                    } else {
                        // 3AC generation
                        String temp = "t" + tempCounter++;
                        irCode.add(temp + " = " + op1 + " " + operator + " " + op2);
                        irCode.add(target + " = " + temp);
                        symbolTable.put(target, null); // Mark as runtime computed
                        modifiedVars.add(target);
                    }
                } else {
                    errors.add(new Error(i + 1, "Invalid SET syntax", "SYNTAX_ERROR"));
                }
            } else {
                errors.add(new Error(i + 1, "Unsupported statement: " + line, "SYNTAX_ERROR"));
            }
        }
        return irCode;
    }

    private boolean validateOperand(String op, Map<String, Integer> symbolTable, List<Error> errors, int lineNo) {
        if (isNumeric(op)) {
            return true;
        } else if (symbolTable.containsKey(op)) {
            return true;
        } else {
            errors.add(new Error(lineNo, "Undeclared operand " + op, "SEMANTIC_ERROR"));
            return false;
        }
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+");
    }

    private int evaluate(int v1, int v2, String op) {
        switch (op) {
            case "+": return v1 + v2;
            case "-": return v1 - v2;
            case "*": return v1 * v2;
            case "/": 
                if (v2 == 0) throw new ArithmeticException("Division by zero");
                return v1 / v2;
            default: throw new IllegalArgumentException("Unknown operator " + op);
        }
    }
}
