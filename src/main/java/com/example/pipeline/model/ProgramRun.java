package com.example.pipeline.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "program_runs")
public class ProgramRun {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String inputCode;

    @Column(columnDefinition = "TEXT")
    private String fullStateJson;

    private boolean hasErrors;

    private LocalDateTime executedAt;

    @PrePersist
    protected void onCreate() {
        executedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getInputCode() {
        return inputCode;
    }

    public String getFullStateJson() {
        return fullStateJson;
    }

    public boolean isHasErrors() {
        return hasErrors;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setInputCode(String inputCode) {
        this.inputCode = inputCode;
    }

    public void setFullStateJson(String fullStateJson) {
        this.fullStateJson = fullStateJson;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
}
