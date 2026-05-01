package com.example.pipeline.model;

import java.util.List;

public class MacroDescriptor {

    private int start;
    private int end;
    private List<String> formalParams;

    public MacroDescriptor() {}

    public MacroDescriptor(int start, int end, List<String> formalParams) {
        this.start = start;
        this.end = end;
        this.formalParams = formalParams;
    }

    // Getters
    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public List<String> getFormalParams() {
        return formalParams;
    }

    // Setters
    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setFormalParams(List<String> formalParams) {
        this.formalParams = formalParams;
    }
}
