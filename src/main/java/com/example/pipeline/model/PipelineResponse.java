package com.example.pipeline.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipelineResponse {

    private String originalCode;
    private String expandedCode;

    private List<String> irCode = new ArrayList<>();
    private List<String> assemblyCode = new ArrayList<>();

    private Map<String, MacroDescriptor> mnt = new HashMap<>();
    private List<String> mdt = new ArrayList<>();

    private Map<String, Integer> symbolTable = new HashMap<>();

    private List<Error> errors = new ArrayList<>();
    
    private List<String> executionTimeline = new ArrayList<>();

    // === Assembler Mode Fields ===
    private boolean assemblerMode = false;
    private List<Map<String, Object>> intermediateCode = new ArrayList<>();    // IC lines with LC
    private List<Map<String, Object>> asmSymtab = new ArrayList<>();           // SYMTAB entries
    private List<Map<String, Object>> littab = new ArrayList<>();              // LITTAB entries
    private List<Integer> pooltab = new ArrayList<>();                         // POOLTAB entries
    private List<String> machineCode = new ArrayList<>();                      // Final machine code

    // Getters
    public List<String> getExecutionTimeline() {
        return executionTimeline;
    }
    public String getOriginalCode() {
        return originalCode;
    }

    public String getExpandedCode() {
        return expandedCode;
    }

    public List<String> getIrCode() {
        return irCode;
    }

    public List<String> getAssemblyCode() {
        return assemblyCode;
    }

    public Map<String, MacroDescriptor> getMnt() {
        return mnt;
    }

    public List<String> getMdt() {
        return mdt;
    }

    public Map<String, Integer> getSymbolTable() {
        return symbolTable;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public boolean isAssemblerMode() {
        return assemblerMode;
    }

    public List<Map<String, Object>> getIntermediateCode() {
        return intermediateCode;
    }

    public List<Map<String, Object>> getAsmSymtab() {
        return asmSymtab;
    }

    public List<Map<String, Object>> getLittab() {
        return littab;
    }

    public List<Integer> getPooltab() {
        return pooltab;
    }

    public List<String> getMachineCode() {
        return machineCode;
    }

    // Setters
    public void setOriginalCode(String originalCode) {
        this.originalCode = originalCode;
    }

    public void setExpandedCode(String expandedCode) {
        this.expandedCode = expandedCode;
    }

    public void setIrCode(List<String> irCode) {
        this.irCode = irCode;
    }

    public void setAssemblyCode(List<String> assemblyCode) {
        this.assemblyCode = assemblyCode;
    }

    public void setMnt(Map<String, MacroDescriptor> mnt) {
        this.mnt = mnt;
    }

    public void setMdt(List<String> mdt) {
        this.mdt = mdt;
    }

    public void setSymbolTable(Map<String, Integer> symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    public void setExecutionTimeline(List<String> executionTimeline) {
        this.executionTimeline = executionTimeline;
    }

    public void setAssemblerMode(boolean assemblerMode) {
        this.assemblerMode = assemblerMode;
    }

    public void setIntermediateCode(List<Map<String, Object>> intermediateCode) {
        this.intermediateCode = intermediateCode;
    }

    public void setAsmSymtab(List<Map<String, Object>> asmSymtab) {
        this.asmSymtab = asmSymtab;
    }

    public void setLittab(List<Map<String, Object>> littab) {
        this.littab = littab;
    }

    public void setPooltab(List<Integer> pooltab) {
        this.pooltab = pooltab;
    }

    public void setMachineCode(List<String> machineCode) {
        this.machineCode = machineCode;
    }
}
