package com.example.pipeline.controller;

import com.example.pipeline.model.PipelineResponse;
import com.example.pipeline.model.ProgramRun;
import com.example.pipeline.service.PipelineService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pipeline")
@CrossOrigin("*")
public class PipelineController {

    private final PipelineService service;

    public PipelineController(PipelineService service) {
        this.service = service;
    }

    @PostMapping("/process")
    public PipelineResponse process(@RequestBody Map<String, String> body) {
        return service.execute(body.get("code"));
    }

    @GetMapping("/history")
    public List<ProgramRun> getHistory() {
        return service.getRecentRuns();
    }
}
