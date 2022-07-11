package com.batchprocessing.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="batch_job_execution")
public class JobExecutions {

	@Id
	Integer JobExecutionId ;
	
	Date CreateTime;
	
	Date StartTime;
	
	Date EndTime ;
	
	String Status ;
	
	String ExitCode ;
	
	String ExitMessage;

	public Integer getJobExecutionId() {
		return JobExecutionId;
	}

	public void setJobExecutionId(Integer jobExecutionId) {
		JobExecutionId = jobExecutionId;
	}

	public Date getCreateTime() {
		return CreateTime;
	}

	public void setCreateTime(Date createTime) {
		CreateTime = createTime;
	}

	public Date getStartTime() {
		return StartTime;
	}

	public void setStartTime(Date startTime) {
		StartTime = startTime;
	}

	public Date getEndTime() {
		return EndTime;
	}

	public void setEndTime(Date endTime) {
		EndTime = endTime;
	}

	public String getStatus() {
		return Status;
	}

	public void setStatus(String status) {
		Status = status;
	}

	public String getExitCode() {
		return ExitCode;
	}

	public void setExitCode(String exitCode) {
		ExitCode = exitCode;
	}

	public String getExitMessage() {
		return ExitMessage;
	}

	public void setExitMessage(String exitMessage) {
		ExitMessage = exitMessage;
	}
}
