package com.example.pipeline.processor;

import com.example.pipeline.model.Error;
import com.example.pipeline.model.MacroDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MacroProcessor {

    public String process(
            String inputCode,
            List<Error> errors,
            Map<String, MacroDescriptor> mnt,
            List<String> mdt
    ) {
        StringBuilder expandedCode = new StringBuilder();
        String[] lines = inputCode.split("\\r?\\n");

        boolean inMacro = false;
        String currentMacroName = null;
        int currentMacroStart = -1;
        List<String> currentFormalParams = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                if (!inMacro) {
                    expandedCode.append("\n");
                }
                continue;
            }

            if (line.startsWith("#MACRO")) {
                if (inMacro) {
                    errors.add(new Error(i + 1, "Nested macros are not allowed", "SYNTAX_ERROR"));
                }
                inMacro = true;
                currentMacroStart = mdt.size();

                String[] parts = line.split("\\s+", 2);
                if (parts.length < 2) {
                    errors.add(new Error(i + 1, "Invalid macro definition", "SYNTAX_ERROR"));
                    currentMacroName = "UNKNOWN_MACRO_" + i;
                    currentFormalParams = new ArrayList<>();
                } else {
                    String declaration = parts[1];
                    String[] declParts = declaration.split("\\s+", 2);
                    currentMacroName = declParts[0];

                    if (currentMacroName == null || currentMacroName.isEmpty()) {
                        errors.add(new Error(i + 1, "Invalid macro name", "SYNTAX_ERROR"));
                        currentMacroName = "UNKNOWN_MACRO_" + i;
                    } else if (mnt.containsKey(currentMacroName)) {
                        errors.add(new Error(i + 1, "Duplicate macro definition: " + currentMacroName, "SEMANTIC_ERROR"));
                    }

                    currentFormalParams = new ArrayList<>();
                    if (declParts.length > 1) {
                        String[] params = declParts[1].split(",");
                        for (String param : params) {
                            currentFormalParams.add(param.trim());
                        }
                    }
                }
            } else if (line.equals("#MEND")) {
                if (!inMacro) {
                    errors.add(new Error(i + 1, "Unexpected #MEND outside of macro", "SYNTAX_ERROR"));
                } else {
                    int currentMacroEnd = mdt.size() - 1;
                    if (currentMacroEnd < currentMacroStart) {
                        // Empty macro -> mark as invalid range
                        currentMacroEnd = currentMacroStart - 1;
                    }
                    mnt.put(currentMacroName, new MacroDescriptor(currentMacroStart, currentMacroEnd, currentFormalParams));

                    inMacro = false;
                    currentMacroName = null;
                    currentMacroStart = -1;
                    currentFormalParams = new ArrayList<>();
                }
            } else {
                if (inMacro) {
                    mdt.add(line);
                } else {
                    if (line.startsWith("@CALL")) {
                        String expanded = expandMacro(line, i + 1, errors, mnt, mdt);
                        expandedCode.append(expanded);
                    } else {
                        // Append original line including preceding whitespace if needed (using lines[i] preserves indent)
                        expandedCode.append(lines[i]).append("\n");
                    }
                }
            }
        }

        if (inMacro) {
            errors.add(new Error(lines.length, "Missing #MEND for macro " + currentMacroName, "SYNTAX_ERROR"));
            int currentMacroEnd = mdt.size() - 1;
            if (currentMacroEnd < currentMacroStart) {
                currentMacroEnd = currentMacroStart - 1;
            }
            mnt.put(currentMacroName, new MacroDescriptor(currentMacroStart, currentMacroEnd, currentFormalParams));
        }

        return expandedCode.toString();
    }

    private String expandMacro(String callLine, int lineNo, List<Error> errors, Map<String, MacroDescriptor> mnt, List<String> mdt) {
        String trimmedCall = callLine.trim();
        int callIndex = trimmedCall.indexOf("@CALL");
        String remaining = trimmedCall.substring(callIndex + "@CALL".length()).trim();

        int openParen = remaining.indexOf('(');
        int closeParen = remaining.lastIndexOf(')');

        if (openParen == -1 || closeParen == -1 || closeParen < openParen) {
            errors.add(new Error(lineNo, "Invalid CALL syntax", "SYNTAX_ERROR"));
            return "";
        }

        String macroName = remaining.substring(0, openParen).trim();
        String argsString = remaining.substring(openParen + 1, closeParen).trim();

        if (!mnt.containsKey(macroName)) {
            errors.add(new Error(lineNo, "Undefined macro " + macroName, "SEMANTIC_ERROR"));
            return "";
        }

        MacroDescriptor desc = mnt.get(macroName);

        if (desc.getStart() > desc.getEnd()) {
            return "";
        }

        List<String> actualParams = new ArrayList<>();
        if (!argsString.isEmpty()) {
            String[] args = argsString.split(",");
            for (String arg : args) {
                if (arg.trim().isEmpty()) {
                    errors.add(new Error(lineNo, "Empty argument in CALL", "SYNTAX_ERROR"));
                    return "";
                }
                actualParams.add(arg.trim());
            }
        }

        if (actualParams.size() != desc.getFormalParams().size()) {
            errors.add(new Error(lineNo, macroName + " expects " + desc.getFormalParams().size() + " args, got " + actualParams.size(), "SEMANTIC_ERROR"));
            return "";
        }

        // Build ALA (Argument List Array)
        Map<String, String> ala = new HashMap<>();
        for (int i = 0; i < actualParams.size(); i++) {
            ala.put(desc.getFormalParams().get(i), actualParams.get(i));
        }

        StringBuilder expanded = new StringBuilder();

        // Sort formal params by length descending to avoid prefix substitution issues (e.g. &A vs &AB)
        List<String> formalParamsDesc = new ArrayList<>(desc.getFormalParams());
        formalParamsDesc.sort((a, b) -> Integer.compare(b.length(), a.length()));

        for (int i = desc.getStart(); i <= desc.getEnd(); i++) {
            if (i < 0 || i >= mdt.size()) break;
            String bodyLine = mdt.get(i);

            for (String fp : formalParamsDesc) {
                bodyLine = bodyLine.replace(fp, ala.get(fp));
            }
            expanded.append(bodyLine).append("\n");
        }

        return expanded.toString();
    }
}
