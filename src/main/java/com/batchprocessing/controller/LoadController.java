package com.batchprocessing.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.batchprocessing.config.BatchConfiguration;
import com.batchprocessing.model.Customer;
import com.batchprocessing.model.JobExecutions;
import com.batchprocessing.repository.CustomerRepository;
import com.batchprocessing.repository.JobExecutionRepository;



@Controller
@RequestMapping("/load")
public class LoadController {
	
	@Autowired
	JobLauncher jobLauncher ; 
	
	@Autowired
	Job job ; 
	
	@Autowired
	JobExecutionRepository jobExecutionRepository ;
	
	
	
	
	public BatchStatus load() throws JobParametersInvalidException,JobRestartException , JobExecutionAlreadyRunningException,JobInstanceAlreadyCompleteException{
	
		
		Map<String,JobParameter>maps = new HashMap<>();
		maps.put("time",new JobParameter(System.currentTimeMillis()));
		
		JobParameters parameters = new JobParameters(maps);
		JobExecution jobExecution = jobLauncher.run(job, parameters);
		
		
		System.out.println("JobExecution Id : "+jobExecution.getJobId()+"  JobExecution Status :  "+jobExecution.getStatus());
		
		System.out.println("Batch is Running ... ");
		
		while(jobExecution.isRunning()) {
			System.out.println("...");
		}
		
		return jobExecution.getStatus();
	}
	
	@GetMapping
	public String getJobs(Model model) throws JobParametersInvalidException, JobRestartException, JobExecutionAlreadyRunningException, JobInstanceAlreadyCompleteException  {
		load() ;
		
		List<JobExecutions> jobList = jobExecutionRepository.findAll();
		
		model.addAttribute("jobs",jobList);
	
		return "jobs";
	}
	
	/*@PostMapping("/addCustomer")
	public String addCustomer(@ModelAttribute("customer") Customer customer) throws JobParametersInvalidException, JobRestartException, JobExecutionAlreadyRunningException, JobInstanceAlreadyCompleteException {
		
		return "insertJob";
	}
	
	@GetMapping("/formCustomer")
	public String formCustomer() {
		return "insertjob";
	}*/
	
}