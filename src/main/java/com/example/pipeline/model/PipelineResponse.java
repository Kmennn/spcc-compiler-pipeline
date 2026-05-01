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

    // Getters
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
}
