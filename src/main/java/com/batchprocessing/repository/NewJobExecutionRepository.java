package com.batchprocessing.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.batchprocessing.model.NewJobExecution;


public interface NewJobExecutionRepository extends JpaRepository<NewJobExecution,NewJobExecution>{

}
