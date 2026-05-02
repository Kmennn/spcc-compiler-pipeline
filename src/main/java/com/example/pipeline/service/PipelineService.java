package com.example.pipeline.service;

import com.example.pipeline.model.Error;
import com.example.pipeline.model.MacroDescriptor;
import com.example.pipeline.model.PipelineResponse;
import com.example.pipeline.model.ProgramRun;
import com.example.pipeline.processor.MacroProcessor;
import com.example.pipeline.processor.MiniAssembler;
import com.example.pipeline.processor.MiniCompiler;
import com.example.pipeline.repository.RunRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PipelineService {

    private final RunRepository repository;
    private final ObjectMapper mapper = new ObjectMapper();

    public PipelineService(RunRepository repository) {
        this.repository = repository;
    }

    public PipelineResponse execute(String code) {
        PipelineResponse response = new PipelineResponse();
        response.setOriginalCode(code);

        List<Error> errors = new ArrayList<>();
        Map<String, MacroDescriptor> mnt = new HashMap<>();
        List<String> mdt = new ArrayList<>();

        // === STAGE 1: Macro Processing ===
        MacroProcessor macroProcessor = new MacroProcessor();
        String expandedCode = macroProcessor.process(code, errors, mnt, mdt);

        response.setExpandedCode(expandedCode);
        response.setMnt(mnt);
        response.setMdt(mdt);

        // FAIL-FAST: if macro stage produced hard errors, stop pipeline
        if (hasHardErrors(errors)) {
            response.setErrors(errors);
            saveRun(code, response, errors);
            return response;
        }

        // === STAGE 2: Compilation (IR Generation) ===
        Map<String, Integer> symbolTable = new HashMap<>();
        MiniCompiler compiler = new MiniCompiler();
        List<String> irCode = compiler.compile(expandedCode, errors, symbolTable);

        response.setIrCode(irCode);
        response.setSymbolTable(symbolTable);

        // FAIL-FAST: if compiler stage produced hard errors, stop pipeline
        if (hasHardErrors(errors)) {
            response.setErrors(errors);
            saveRun(code, response, errors);
            return response;
        }

        // === STAGE 3: Assembly (IR → V-ISA) ===
        MiniAssembler assembler = new MiniAssembler();
        List<String> assemblyCode = assembler.generateAssembly(irCode, errors);

        response.setAssemblyCode(assemblyCode);
        response.setErrors(errors);

        // === Build Execution Timeline (Value Evolution) ===
        List<String> timeline = new ArrayList<>();
        Map<String, Integer> currentValues = new HashMap<>();
        
        timeline.add("System state: READY");
        
        if (irCode != null) {
            for (String line : irCode) {
                String l = line.trim();
                if (l.contains("=")) {
                    String[] parts = l.split("=");
                    String target = parts[0].trim();
                    String expr = parts[1].trim();
                    
                    if (expr.matches("-?\\d+")) {
                        int val = Integer.parseInt(expr);
                        currentValues.put(target, val);
                        timeline.add(target + " set to " + val);
                    } else if (target.startsWith("t")) {
                        timeline.add("Computing: " + expr);
                    } else if (currentValues.containsKey(target) || symbolTable.containsKey(target)) {
                        Integer finalVal = symbolTable.get(target);
                        if (finalVal != null) {
                            timeline.add(target + " updated → " + finalVal);
                        } else {
                            timeline.add(target + " updated");
                        }
                    }
                } else if (l.startsWith("OUT")) {
                    String var = l.replace("OUT", "").trim();
                    Integer val = symbolTable.get(var);
                    timeline.add("Output: " + var + (val != null ? " is " + val : ""));
                }
            }
        }
        
        if (timeline.size() <= 1) {
            timeline.add("No execution steps recorded.");
        }
        
        timeline.add("Program completed successfully");
        response.setExecutionTimeline(timeline);

        // Persist run to database
        saveRun(code, response, errors);

        return response;
    }

    private void saveRun(String code, PipelineResponse response, List<Error> errors) {
        try {
            ProgramRun run = new ProgramRun();
            run.setInputCode(code);
            run.setFullStateJson(mapper.writeValueAsString(response));
            run.setHasErrors(hasHardErrors(errors));
            repository.save(run);
        } catch (Exception e) {
            // Log but do not crash pipeline if DB write fails
            System.err.println("Failed to save run: " + e.getMessage());
        }
    }

    private boolean hasHardErrors(List<Error> errors) {
        for (Error e : errors) {
            if (!"WARNING".equals(e.getType())) {
                return true;
            }
        }
        return false;
    }

    public List<ProgramRun> getRecentRuns() {
        return repository.findTop3ByOrderByIdDesc();
    }
}
