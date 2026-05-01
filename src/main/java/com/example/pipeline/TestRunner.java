package com.example.pipeline;

/**
 * Standalone test runner (pre-Spring Boot).
 * Use the REST API (POST /api/v1/pipeline/process) for full pipeline testing.
 * This class is retained for reference only.
 */
public class TestRunner {
    public static void main(String[] args) {
        System.out.println("Use the Spring Boot API for pipeline testing:");
        System.out.println("  POST /api/v1/pipeline/process");
        System.out.println("  Body: {\"code\": \"VAR X = 10\\nOUT X\"}");
    }
}
