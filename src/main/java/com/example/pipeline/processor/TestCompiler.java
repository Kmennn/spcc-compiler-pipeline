package com.example.pipeline.processor;

import com.example.pipeline.model.Error;
import com.example.pipeline.model.MacroDescriptor;

import java.util.*;

public class TestCompiler {
    public static void main(String[] args) {
        String input =
            "#MACRO INCREMENT &A\n" +
            "SET &A = &A + 1\n" +
            "#MEND\n" +
            "\n" +
            "#MACRO ADDPAIR &A, &B\n" +
            "SET &A = &A + &B\n" +
            "#MEND\n" +
            "\n" +
            "VAR X = 10\n" +
            "VAR Y = 5\n" +
            "VAR Z = 0\n" +
            "\n" +
            "@CALL INCREMENT(X)\n" +
            "@CALL ADDPAIR(X, Y)\n" +
            "\n" +
            "SET Z = X + Y\n" +
            "\n" +
            "OUT Z\n";

        List<Error> errors = new ArrayList<>();
        Map<String, MacroDescriptor> mnt = new HashMap<>();
        List<String> mdt = new ArrayList<>();
        Map<String, Integer> symbolTable = new LinkedHashMap<>();

        // ========== PHASE 1: MACRO PROCESSING ==========
        System.out.println("================================================");
        System.out.println("        PHASE 1: MACRO PROCESSING");
        System.out.println("================================================");

        MacroProcessor macroProcessor = new MacroProcessor();
        String expandedCode = macroProcessor.process(input, errors, mnt, mdt);

        System.out.println("\n--- MNT (Macro Name Table) ---");
        for (Map.Entry<String, MacroDescriptor> entry : mnt.entrySet()) {
            MacroDescriptor d = entry.getValue();
            System.out.println("  " + entry.getKey() + " -> MDT[" + d.getStart() + ".." + d.getEnd() + "]  Params: " + d.getFormalParams());
        }

        System.out.println("\n--- MDT (Macro Definition Table) ---");
        for (int i = 0; i < mdt.size(); i++) {
            System.out.println("  [" + i + "] " + mdt.get(i));
        }

        System.out.println("\n--- Expanded Code ---");
        String[] expLines = expandedCode.split("\n");
        for (int i = 0; i < expLines.length; i++) {
            if (!expLines[i].trim().isEmpty()) {
                System.out.println("  " + (i + 1) + ": " + expLines[i]);
            }
        }

        // ========== PHASE 2: COMPILATION (IR GENERATION) ==========
        System.out.println("\n================================================");
        System.out.println("    PHASE 2: COMPILATION (IR GENERATION)");
        System.out.println("================================================");

        MiniCompiler compiler = new MiniCompiler();
        List<String> irCode = compiler.compile(expandedCode, errors, symbolTable);

        System.out.println("\n--- Intermediate Representation (3AC) ---");
        for (int i = 0; i < irCode.size(); i++) {
            System.out.println("  " + (i + 1) + ": " + irCode.get(i));
        }

        System.out.println("\n--- Symbol Table ---");
        System.out.println("  Variable   Value");
        System.out.println("  ----------------------");
        for (Map.Entry<String, Integer> entry : symbolTable.entrySet()) {
            String val = entry.getValue() != null ? String.valueOf(entry.getValue()) : "runtime";
            System.out.println("  " + entry.getKey() + "          " + val);
        }

        // ========== PHASE 3: ASSEMBLY GENERATION ==========
        System.out.println("\n================================================");
        System.out.println("      PHASE 3: ASSEMBLY GENERATION");
        System.out.println("================================================");

        MiniAssembler assembler = new MiniAssembler();
        List<String> assembly = assembler.generateAssembly(irCode, errors);

        System.out.println("\n--- Assembly Code ---");
        for (int i = 0; i < assembly.size(); i++) {
            System.out.println("  " + (i + 1) + ": " + assembly.get(i));
        }

        // ========== ERRORS ==========
        System.out.println("\n================================================");
        System.out.println("              ERROR REPORT");
        System.out.println("================================================");

        if (errors.isEmpty()) {
            System.out.println("  [OK] No errors detected.");
        } else {
            for (Error e : errors) {
                System.out.println("  [" + e.getType() + "] Line " + e.getLine() + ": " + e.getMessage());
            }
        }

        System.out.println("\n=============== TEST COMPLETE ===============");
    }
}
