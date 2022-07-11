package com.batchprocessing.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.batchprocessing.model.JobExecutions;

public interface JobExecutionRepository extends JpaRepository<JobExecutions,JobExecutions>{

}
