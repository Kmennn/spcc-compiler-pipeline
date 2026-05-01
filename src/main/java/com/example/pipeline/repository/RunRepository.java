package com.example.pipeline.repository;

import com.example.pipeline.model.ProgramRun;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RunRepository extends JpaRepository<ProgramRun, Long> {
    List<ProgramRun> findTop3ByOrderByIdDesc();
}
