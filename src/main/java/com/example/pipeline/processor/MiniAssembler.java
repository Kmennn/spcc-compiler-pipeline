package com.example.pipeline.processor;

import com.example.pipeline.model.Error;

import java.util.ArrayList;
import java.util.List;

public class MiniAssembler {

    public List<String> generateAssembly(
            List<String> irCode,
            List<Error> errors
    ) {
        List<String> assembly = new ArrayList<>();

        for (int i = 0; i < irCode.size(); i++) {
            String line = irCode.get(i).trim();
            int lineNo = i + 1;
            if (line.isEmpty()) continue;

            if (line.contains("=")) {
                String[] parts = line.split("\\s*=\\s*", 2);
                if (parts.length != 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
                    errors.add(new Error(lineNo, "Invalid IR line: " + line, "ASSEMBLER_ERROR"));
                    continue;
                }
                String target = parts[0].trim();
                String expr = parts[1].trim();

                String[] exprParts = expr.split("\\s+");

                if (exprParts.length == 1) {
                    // Simple assignment: Z = t1 OR Z = 15
                    if (isNumeric(exprParts[0])) {
                        assembly.add("LDI R1, " + exprParts[0]);
                        assembly.add("STR " + target + ", R1");
                    } else if (isIdentifier(exprParts[0])) {
                        assembly.add("LDR R1, " + exprParts[0]);
                        assembly.add("STR " + target + ", R1");
                    } else {
                        errors.add(new Error(lineNo, "Invalid IR line: " + line, "ASSEMBLER_ERROR"));
                    }
                } else if (exprParts.length == 3) {
                    // Binary expression: t1 = X + Y OR t1 = X + 5
                    String op1 = exprParts[0];
                    String operator = exprParts[1];
                    String op2 = exprParts[2];

                    // Validate operator BEFORE generating any instructions
                    if (!operator.matches("[+\\-*/]")) {
                        errors.add(new Error(lineNo, "Unknown operator: " + operator, "ASSEMBLER_ERROR"));
                        continue;
                    }

                    // Validate operands BEFORE generating any instructions
                    if (!isNumeric(op1) && !isIdentifier(op1)) {
                        errors.add(new Error(lineNo, "Invalid IR line: " + line, "ASSEMBLER_ERROR"));
                        continue;
                    }
                    if (!isNumeric(op2) && !isIdentifier(op2)) {
                        errors.add(new Error(lineNo, "Invalid IR line: " + line, "ASSEMBLER_ERROR"));
                        continue;
                    }

                    // Generate LDR/LDI for op1
                    if (isNumeric(op1)) {
                        assembly.add("LDI R1, " + op1);
                    } else {
                        assembly.add("LDR R1, " + op1);
                    }

                    // Generate LDR/LDI for op2
                    if (isNumeric(op2)) {
                        assembly.add("LDI R2, " + op2);
                    } else {
                        assembly.add("LDR R2, " + op2);
                    }

                    switch (operator) {
                        case "+": assembly.add("ADD R1, R2"); break;
                        case "-": assembly.add("SUB R1, R2"); break;
                        case "*": assembly.add("MUL R1, R2"); break;
                        case "/": assembly.add("DIV R1, R2"); break;
                    }
                    assembly.add("STR " + target + ", R1");
                } else {
                    errors.add(new Error(lineNo, "Invalid IR line: " + line, "ASSEMBLER_ERROR"));
                }
            } else if (line.startsWith("OUT ")) {
                String[] parts = line.split("\\s+");
                if (parts.length == 2 && parts[0].equals("OUT")) {
                    assembly.add("PRN " + parts[1]);
                } else {
                    errors.add(new Error(lineNo, "Invalid IR line: " + line, "ASSEMBLER_ERROR"));
                }
            } else {
                errors.add(new Error(lineNo, "Invalid IR line: " + line, "ASSEMBLER_ERROR"));
            }
        }

        assembly.add("HLT");
        return assembly;
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+");
    }

    private boolean isIdentifier(String str) {
        return str.matches("[A-Za-z_][A-Za-z0-9_]*");
    }
}
