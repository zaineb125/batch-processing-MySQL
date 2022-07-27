package com.batchprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.batchprocessing.config.ManagerConfiguration;
import com.batchprocessing.controller.LoadController;
import com.fasterxml.jackson.core.JsonProcessingException;

@SpringBootApplication
@EnableAutoConfiguration
public class BatchProcessingApplication extends SpringBootServletInitializer {

	public static void main(String[] args) throws JsonProcessingException {
		ConfigurableApplicationContext appContext = SpringApplication.run(BatchProcessingApplication.class, args);
		
		ManagerConfiguration managerConfiguration = appContext.getBean(ManagerConfiguration.class);
		managerConfiguration.sendToTopic();
	}
	
	
}
