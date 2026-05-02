package com.example.pipeline.processor;

import com.example.pipeline.model.Error;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Complete Two-Pass Assembler Engine (Dhamdhere-style).
 * 
 * Pass-1: Generates Intermediate Code (IC), populates SYMTAB, LITTAB, POOLTAB.
 * Pass-2: Resolves IC into final Machine Code using table lookups.
 */
public class AssemblerEngine {

    // ===================== STATIC INSTRUCTION TABLES =====================

    // MOT — Machine Opcode Table (Imperative Statements)
    private static final Map<String, Integer> MOT = new LinkedHashMap<>();
    static {
        MOT.put("STOP", 0);
        MOT.put("ADD", 1);
        MOT.put("SUB", 2);
        MOT.put("MULT", 3);
        MOT.put("MOVER", 4);
        MOT.put("MOVEM", 5);
        MOT.put("COMP", 6);
        MOT.put("BC", 7);
        MOT.put("DIV", 8);
        MOT.put("READ", 9);
        MOT.put("PRINT", 10);
    }

    // POT — Pseudo Opcode Table (Assembler Directives)
    private static final Map<String, Integer> POT = new LinkedHashMap<>();
    static {
        POT.put("START", 1);
        POT.put("END", 2);
        POT.put("ORIGIN", 3);
        POT.put("EQU", 4);
        POT.put("LTORG", 5);
    }

    // DL — Declarative Statements
    private static final Map<String, Integer> DL = new LinkedHashMap<>();
    static {
        DL.put("DS", 1);
        DL.put("DC", 2);
    }

    // REG — Register Table
    private static final Map<String, Integer> REG = new LinkedHashMap<>();
    static {
        REG.put("AREG", 1);
        REG.put("BREG", 2);
        REG.put("CREG", 3);
        REG.put("DREG", 4);
    }

    // COND — Condition Codes (for BC instruction)
    private static final Map<String, Integer> COND = new LinkedHashMap<>();
    static {
        COND.put("LT", 1);
        COND.put("LE", 2);
        COND.put("EQ", 3);
        COND.put("GT", 4);
        COND.put("GE", 5);
        COND.put("ANY", 6);
    }

    // ===================== DATA STRUCTURES =====================

    public static class Symbol {
        public String name;
        public int address;
        public int index;

        public Symbol(String name, int address, int index) {
            this.name = name;
            this.address = address;
            this.index = index;
        }
    }

    public static class Literal {
        public String value;   // e.g. "5" from =5
        public int address;    // -1 means unassigned
        public int index;

        public Literal(String value, int address, int index) {
            this.value = value;
            this.address = address;
            this.index = index;
        }
    }

    // IC line with associated LC
    public static class ICLine {
        public int lc;         // -1 for directives that don't occupy memory
        public String ic;      // e.g. "(IS,04) (RG,01) (L,0)"

        public ICLine(int lc, String ic) {
            this.lc = lc;
            this.ic = ic;
        }
    }

    // ===================== RESULT CONTAINER =====================

    public static class AssemblerResult {
        public List<ICLine> intermediateCode = new ArrayList<>();
        public List<Symbol> symtab = new ArrayList<>();
        public List<Literal> littab = new ArrayList<>();
        public List<Integer> pooltab = new ArrayList<>();
        public List<String> machineCode = new ArrayList<>();
        public List<String> timeline = new ArrayList<>();
    }

    // ===================== MAIN ENTRY POINT =====================

    public AssemblerResult assemble(String sourceCode, List<Error> errors) {
        AssemblerResult result = new AssemblerResult();

        String[] lines = sourceCode.split("\\r?\\n");

        // Pass 1
        pass1(lines, result, errors);

        // Pass 2 (only if no hard errors in Pass 1)
        if (!hasHardErrors(errors)) {
            pass2(result, errors);
        }

        return result;
    }

    // ===================== PASS 1 =====================

    private void pass1(String[] lines, AssemblerResult result, List<Error> errors) {
        int LC = 0;
        result.pooltab.add(0); // First pool starts at LITTAB index 0
        result.timeline.add("Pass-1 started");

        for (int lineIdx = 0; lineIdx < lines.length; lineIdx++) {
            String rawLine = lines[lineIdx].trim();
            if (rawLine.isEmpty()) continue;

            int lineNo = lineIdx + 1;
            String label = null;
            String instruction = rawLine;

            // --- Extract label ---
            // Format 1: "LABEL: instruction" (colon-separated)
            if (rawLine.contains(":") && !rawLine.contains("='")) {
                int colonIdx = rawLine.indexOf(':');
                label = rawLine.substring(0, colonIdx).trim();
                instruction = rawLine.substring(colonIdx + 1).trim();
            }
            // Format 2: "LABEL MNEMONIC OPERANDS" (Dhamdhere — no colon, first token is label)
            else {
                String[] parts = rawLine.split("\\s+");
                if (parts.length >= 2) {
                    String firstToken = parts[0].toUpperCase();
                    // If first token is NOT a known keyword, it's a label
                    if (!isKnownMnemonic(firstToken)) {
                        label = parts[0];
                        instruction = rawLine.substring(rawLine.indexOf(parts[1]));
                    }
                }
            }

            // Register label in SYMTAB with current LC
            if (label != null && !label.isEmpty()) {
                addOrUpdateSymbol(result.symtab, label, LC);
                result.timeline.add("Label '" + label + "' assigned address " + LC);
            }

            // Tokenize instruction
            String[] tokens = tokenize(instruction);
            if (tokens.length == 0) continue;

            String mnemonic = tokens[0].toUpperCase();

            // ============ CASE: START ============
            if (mnemonic.equals("START")) {
                if (tokens.length >= 2) {
                    try {
                        LC = Integer.parseInt(tokens[1]);
                    } catch (NumberFormatException e) {
                        errors.add(new Error(lineNo, "Invalid START operand: " + tokens[1], "ASSEMBLER_ERROR"));
                        continue;
                    }
                }
                result.intermediateCode.add(new ICLine(-1, "(AD,01) (C," + LC + ")"));
                result.timeline.add("LC initialized → " + LC);
            }
            // ============ CASE: END ============
            else if (mnemonic.equals("END")) {
                // Process remaining unassigned literals (like LTORG at end)
                LC = processLiterals(result, LC);
                result.intermediateCode.add(new ICLine(-1, "(AD,02)"));
                result.timeline.add("END directive processed, remaining literals assigned");
            }
            // ============ CASE: ORIGIN ============
            else if (mnemonic.equals("ORIGIN")) {
                if (tokens.length >= 2) {
                    String expr = joinFrom(tokens, 1);
                    int newLC = evaluateExpression(expr, result.symtab, errors, lineNo, LC);
                    if (newLC >= 0) {
                        result.intermediateCode.add(new ICLine(-1, "(AD,03) (C," + newLC + ")"));
                        result.timeline.add("ORIGIN: LC changed from " + LC + " → " + newLC);
                        LC = newLC;
                    }
                } else {
                    errors.add(new Error(lineNo, "ORIGIN requires an expression", "ASSEMBLER_ERROR"));
                }
            }
            // ============ CASE: EQU ============
            else if (mnemonic.equals("EQU")) {
                if (tokens.length >= 2) {
                    String expr = joinFrom(tokens, 1);
                    int val = evaluateExpression(expr, result.symtab, errors, lineNo, LC);
                    if (val >= 0) {
                        if (label != null) {
                            addOrUpdateSymbol(result.symtab, label, val);
                        }
                        result.intermediateCode.add(new ICLine(-1, "(AD,04) (C," + val + ")"));
                        result.timeline.add("EQU: equated to " + val);
                    }
                } else {
                    errors.add(new Error(lineNo, "EQU requires an expression", "ASSEMBLER_ERROR"));
                }
            }
            // ============ CASE: LTORG ============
            else if (mnemonic.equals("LTORG")) {
                int beforeLC = LC;
                LC = processLiterals(result, LC);
                result.intermediateCode.add(new ICLine(-1, "(AD,05)"));
                result.timeline.add("LTORG processed, literals assigned addresses " + beforeLC + "-" + (LC - 1));
            }
            // ============ CASE: DS ============
            else if (DL.containsKey(mnemonic) && mnemonic.equals("DS")) {
                if (label != null) {
                    addOrUpdateSymbol(result.symtab, label, LC);
                }
                int size = 1;
                if (tokens.length >= 2) {
                    try {
                        size = Integer.parseInt(tokens[1]);
                    } catch (NumberFormatException e) {
                        errors.add(new Error(lineNo, "Invalid DS size: " + tokens[1], "ASSEMBLER_ERROR"));
                    }
                }
                result.intermediateCode.add(new ICLine(LC, "(DL,01) (C," + size + ")"));
                result.timeline.add("DS: " + (label != null ? label : "?") + " allocated " + size + " word(s) at address " + LC);
                LC += size;
            }
            // ============ CASE: DC ============
            else if (DL.containsKey(mnemonic) && mnemonic.equals("DC")) {
                if (label != null) {
                    addOrUpdateSymbol(result.symtab, label, LC);
                }
                String val = "0";
                if (tokens.length >= 2) {
                    val = tokens[1].replace("'", "");
                }
                result.intermediateCode.add(new ICLine(LC, "(DL,02) (C," + val + ")"));
                result.timeline.add("DC: " + (label != null ? label : "?") + " = " + val + " at address " + LC);
                LC++;
            }
            // ============ CASE: IMPERATIVE STATEMENT ============
            else if (MOT.containsKey(mnemonic)) {
                int opcode = MOT.get(mnemonic);
                StringBuilder ic = new StringBuilder();
                ic.append("(IS,").append(String.format("%02d", opcode)).append(")");

                if (mnemonic.equals("STOP")) {
                    // STOP has no operands
                    result.intermediateCode.add(new ICLine(LC, ic.toString()));
                    result.timeline.add(LC + ": STOP instruction");
                    LC++;
                } else if (mnemonic.equals("BC")) {
                    // BC <condition>, <symbol>
                    if (tokens.length >= 3) {
                        String condStr = tokens[1].replace(",", "").toUpperCase();
                        String operand = tokens[2].replace(",", "").trim();

                        if (COND.containsKey(condStr)) {
                            ic.append(" (CC,").append(COND.get(condStr)).append(")");
                        } else {
                            errors.add(new Error(lineNo, "Unknown condition code: " + condStr, "ASSEMBLER_ERROR"));
                            continue;
                        }

                        ic.append(" ").append(resolveOperand(operand, result, errors, lineNo));

                        result.intermediateCode.add(new ICLine(LC, ic.toString()));
                        result.timeline.add(LC + ": " + mnemonic + " " + condStr + ", " + operand);
                        LC++;
                    } else {
                        errors.add(new Error(lineNo, "BC requires condition and operand", "ASSEMBLER_ERROR"));
                    }
                } else if (mnemonic.equals("READ") || mnemonic.equals("PRINT")) {
                    // READ/PRINT <symbol> — no register operand
                    if (tokens.length >= 2) {
                        String operand = tokens[1].replace(",", "").trim();
                        ic.append(" ").append(resolveOperand(operand, result, errors, lineNo));
                        result.intermediateCode.add(new ICLine(LC, ic.toString()));
                        result.timeline.add(LC + ": " + mnemonic + " " + operand);
                        LC++;
                    } else {
                        errors.add(new Error(lineNo, mnemonic + " requires an operand", "ASSEMBLER_ERROR"));
                    }
                } else {
                    // Standard format: MNEMONIC REG, OPERAND (operand can be symbol, literal, or register)
                    if (tokens.length >= 3) {
                        String regStr = tokens[1].replace(",", "").toUpperCase();
                        String operand = tokens[2].replace(",", "").trim().toUpperCase();

                        if (REG.containsKey(regStr)) {
                            ic.append(" (RG,").append(REG.get(regStr)).append(")");
                        } else {
                            errors.add(new Error(lineNo, "Unknown register: " + regStr, "ASSEMBLER_ERROR"));
                            continue;
                        }

                        // Check if second operand is also a register
                        if (REG.containsKey(operand)) {
                            ic.append(" (RG,").append(REG.get(operand)).append(")");
                        } else {
                            ic.append(" ").append(resolveOperand(tokens[2].replace(",", "").trim(), result, errors, lineNo));
                        }

                        result.intermediateCode.add(new ICLine(LC, ic.toString()));
                        result.timeline.add(LC + ": " + mnemonic + " " + regStr + ", " + operand);
                        LC++;
                    } else if (tokens.length == 2) {
                        // Some instructions may have only register (edge case)
                        String regStr = tokens[1].replace(",", "").toUpperCase();
                        if (REG.containsKey(regStr)) {
                            ic.append(" (RG,").append(REG.get(regStr)).append(")");
                        }
                        result.intermediateCode.add(new ICLine(LC, ic.toString()));
                        result.timeline.add(LC + ": " + mnemonic + " " + regStr);
                        LC++;
                    } else {
                        errors.add(new Error(lineNo, mnemonic + " requires register and operand", "ASSEMBLER_ERROR"));
                    }
                }
            }
            // ============ UNKNOWN ============
            else {
                // Could be a label-only line (already handled) or unknown
                if (label == null) {
                    errors.add(new Error(lineNo, "Unknown mnemonic: " + mnemonic, "ASSEMBLER_ERROR"));
                }
            }
        }

        result.timeline.add("Pass-1 completed. SYMTAB has " + result.symtab.size() + " entries, LITTAB has " + result.littab.size() + " entries.");
    }

    // ===================== PASS 2 =====================

    private void pass2(AssemblerResult result, List<Error> errors) {
        result.timeline.add("Pass-2 started");

        for (ICLine icLine : result.intermediateCode) {
            if (icLine.lc < 0) {
                // Assembler directives (AD) don't produce machine code
                // But LTORG lines for literal values do - handle DC literals
                continue;
            }

            String ic = icLine.ic;
            StringBuilder mc = new StringBuilder();
            mc.append(String.format("%03d", icLine.lc)).append(" ");

            String opcode = "00";
            String reg = "00";
            String addr = "000";
            boolean isDL = false;
            int dlType = 0;

            // Parse IC tokens: (TYPE,VALUE)
            Pattern pattern = Pattern.compile("\\(([A-Z]{2}),(\\d+)\\)");
            Matcher matcher = pattern.matcher(ic);

            while (matcher.find()) {
                String type = matcher.group(1);
                int value = Integer.parseInt(matcher.group(2));

                switch (type) {
                    case "IS":
                        opcode = String.format("%02d", value);
                        break;
                    case "DL":
                        isDL = true;
                        dlType = value;
                        break;
                    case "RG":
                    case "CC":
                        reg = String.format("%02d", value);
                        break;
                    case "S":
                        // Resolve from SYMTAB
                        if (value >= 0 && value < result.symtab.size()) {
                            addr = String.format("%03d", result.symtab.get(value).address);
                        } else {
                            errors.add(new Error(0, "Invalid SYMTAB index: " + value, "ASSEMBLER_ERROR"));
                            addr = "???";
                        }
                        break;
                    case "L":
                        // Resolve from LITTAB
                        if (value >= 0 && value < result.littab.size()) {
                            addr = String.format("%03d", result.littab.get(value).address);
                        } else {
                            errors.add(new Error(0, "Invalid LITTAB index: " + value, "ASSEMBLER_ERROR"));
                            addr = "???";
                        }
                        break;
                    case "C":
                        // Constant
                        addr = String.format("%03d", value);
                        break;
                }
            }

            // Build machine code line
            if (isDL) {
                if (dlType == 1) { // DS
                    mc.append("00 00 000");
                } else if (dlType == 2) { // DC
                    mc.append("00 00 ").append(addr);
                }
            } else {
                mc.append(opcode).append(" ").append(reg).append(" ").append(addr);
            }

            result.machineCode.add(mc.toString());
        }

        // Also generate machine code entries for literals that were assigned addresses
        for (Literal lit : result.littab) {
            if (lit.address >= 0) {
                String litMC = String.format("%03d 00 00 %03d", lit.address, Integer.parseInt(lit.value));
                result.machineCode.add(litMC);
            }
        }

        // Sort machine code by address
        result.machineCode.sort((a, b) -> {
            try {
                int addrA = Integer.parseInt(a.substring(0, 3).trim());
                int addrB = Integer.parseInt(b.substring(0, 3).trim());
                return Integer.compare(addrA, addrB);
            } catch (NumberFormatException e) {
                return 0;
            }
        });

        result.timeline.add("Pass-2 completed. Generated " + result.machineCode.size() + " machine code entries.");
    }

    // ===================== HELPER METHODS =====================

    /**
     * Resolves an operand to its IC representation.
     * Literal (=5) → (L,index), Symbol → (S,index)
     */
    private String resolveOperand(String operand, AssemblerResult result, List<Error> errors, int lineNo) {
        if (operand.startsWith("=")) {
            // Literal
            String litValue = operand.substring(1).replace("'", "");
            int idx = findLiteralIndex(result.littab, litValue);
            if (idx < 0) {
                idx = result.littab.size();
                result.littab.add(new Literal(litValue, -1, idx));
                result.timeline.add("Literal '" + operand + "' added to LITTAB at index " + idx);
            }
            return "(L," + idx + ")";
        } else {
            // Symbol
            int idx = getSymbolIndex(result.symtab, operand);
            if (idx < 0) {
                // Forward reference — add to SYMTAB with address -1
                idx = result.symtab.size();
                result.symtab.add(new Symbol(operand, -1, idx));
                result.timeline.add("Forward reference: '" + operand + "' added to SYMTAB at index " + idx);
            }
            return "(S," + idx + ")";
        }
    }

    /**
     * Process unassigned literals — assign LC addresses.
     * Returns the updated LC after all literals have been placed.
     */
    private int processLiterals(AssemblerResult result, int startLC) {
        int lc = startLC;
        int poolStart = result.pooltab.get(result.pooltab.size() - 1);
        boolean assigned = false;

        for (int i = poolStart; i < result.littab.size(); i++) {
            Literal lit = result.littab.get(i);
            if (lit.address < 0) {
                lit.address = lc;
                result.timeline.add("Literal '=" + lit.value + "' assigned address " + lc);
                lc++;
                assigned = true;
            }
        }

        // Add new pool entry only if literals were actually assigned
        if (assigned) {
            result.pooltab.add(result.littab.size());
        }

        return lc;
    }

    /**
     * Check if a token is a known assembler mnemonic/directive/keyword.
     */
    private boolean isKnownMnemonic(String token) {
        return MOT.containsKey(token) || token.equals("START") || token.equals("END")
                || token.equals("ORIGIN") || token.equals("EQU") || token.equals("LTORG")
                || token.equals("DS") || token.equals("DC") || token.equals("MACRO")
                || token.equals("MEND") || token.equals("ENDM");
    }

    /**
     * Add or update a symbol in SYMTAB.
     */
    private void addOrUpdateSymbol(List<Symbol> symtab, String name, int address) {
        for (Symbol s : symtab) {
            if (s.name.equals(name)) {
                s.address = address;
                return;
            }
        }
        symtab.add(new Symbol(name, address, symtab.size()));
    }

    /**
     * Get the index of a symbol in SYMTAB, or -1 if not found.
     */
    private int getSymbolIndex(List<Symbol> symtab, String name) {
        for (int i = 0; i < symtab.size(); i++) {
            if (symtab.get(i).name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Find a literal in LITTAB by value. Only searches unassigned literals in current pool.
     */
    private int findLiteralIndex(List<Literal> littab, String value) {
        for (int i = 0; i < littab.size(); i++) {
            if (littab.get(i).value.equals(value) && littab.get(i).address < 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Evaluate an expression like "L1 + 3", "* + 5", or "200".
     */
    private int evaluateExpression(String expr, List<Symbol> symtab, List<Error> errors, int lineNo, int currentLC) {
        expr = expr.trim();

        // Single asterisk (current LC)
        if (expr.equals("*")) {
            return currentLC;
        }

        // Simple constant
        if (expr.matches("-?\\d+")) {
            return Integer.parseInt(expr);
        }

        // Expression with + or -
        Pattern p = Pattern.compile("([A-Za-z0-9_*]+)\\s*([+-])\\s*([A-Za-z0-9_*]+)");
        Matcher m = p.matcher(expr);
        if (m.matches()) {
            String op1 = m.group(1);
            String op = m.group(2);
            String op2 = m.group(3);

            int val1 = evaluateOperand(op1, symtab, currentLC);
            int val2 = evaluateOperand(op2, symtab, currentLC);

            if (val1 >= 0 && val2 >= 0) {
                return op.equals("+") ? val1 + val2 : val1 - val2;
            } else {
                errors.add(new Error(lineNo, "Undefined symbol in expression: " + expr, "ASSEMBLER_ERROR"));
                return -1;
            }
        }

        // Simple symbol reference
        int val = evaluateOperand(expr, symtab, currentLC);
        if (val >= 0) {
            return val;
        }

        errors.add(new Error(lineNo, "Cannot evaluate expression: " + expr, "ASSEMBLER_ERROR"));
        return -1;
    }

    /**
     * Helper to evaluate a single operand in an expression.
     */
    private int evaluateOperand(String op, List<Symbol> symtab, int currentLC) {
        if (op.equals("*")) return currentLC;
        if (op.matches("\\d+")) return Integer.parseInt(op);
        int idx = getSymbolIndex(symtab, op);
        if (idx >= 0 && symtab.get(idx).address >= 0) {
            return symtab.get(idx).address;
        }
        return -1;
    }

    /**
     * Tokenize an instruction line, handling commas.
     */
    private String[] tokenize(String instruction) {
        if (instruction == null || instruction.trim().isEmpty()) {
            return new String[0];
        }
        // Split by whitespace and commas, preserving comma-separated values
        return instruction.trim().split("[\\s,]+");
    }

    /**
     * Join tokens from a specific index.
     */
    private String joinFrom(String[] tokens, int startIdx) {
        StringBuilder sb = new StringBuilder();
        for (int i = startIdx; i < tokens.length; i++) {
            if (i > startIdx) sb.append(" ");
            sb.append(tokens[i]);
        }
        return sb.toString();
    }

    /**
     * Check if the source code is an assembler program.
     */
    public static boolean isAssemblerProgram(String code) {
        if (code == null) return false;
        String upper = code.toUpperCase();
        // Assembler programs contain START, MOVER, MOVEM, READ, PRINT, DS, DC, LTORG, etc.
        return upper.contains("START") && (
                upper.contains("MOVER") || upper.contains("MOVEM") ||
                upper.contains("READ") || upper.contains("PRINT") ||
                upper.contains("LTORG") || upper.contains("STOP") ||
                upper.contains(" DS ") || upper.contains(" DC "));
    }

    private boolean hasHardErrors(List<Error> errors) {
        for (Error e : errors) {
            if (!"WARNING".equals(e.getType())) {
                return true;
            }
        }
        return false;
    }
}
