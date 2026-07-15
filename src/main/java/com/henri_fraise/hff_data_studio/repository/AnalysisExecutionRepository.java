package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.AnalysisExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AnalysisExecutionRepository extends JpaRepository<AnalysisExecution, UUID> {

}
