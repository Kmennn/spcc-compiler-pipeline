package com.example.pipeline.model;

public class Error {

    private int line;
    private String message;
    private String type;

    public Error() {}

    public Error(int line, String message, String type) {
        this.line = line;
        this.message = message;
        this.type = type;
    }

    // Getters
    public int getLine() {
        return line;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    // Setters
    public void setLine(int line) {
        this.line = line;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(String type) {
        this.type = type;
    }
}
