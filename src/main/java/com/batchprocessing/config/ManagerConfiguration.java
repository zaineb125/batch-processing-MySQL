package com.batchprocessing.config;


import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.batchprocessing.model.Department;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;



@Component
public class ManagerConfiguration {
	
	 @Value("${spring.activemq.topic}")
	    String topic;

	    @Autowired
	    JmsTemplate jmsTemplate;

	    
	    public void sendToTopic() throws JsonProcessingException {
	        try {
	            Department department = new Department(1,"HR",100);
	            String jsonObj = new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(department);
	            jmsTemplate.send(topic, messageCreator -> {
	                TextMessage message = messageCreator.createTextMessage();
	                message.setText(jsonObj);
	                return message;
	            });
	        }
	        catch (Exception ex) {
	            System.out.println("ERROR in sending message to queue");
	        }
	    }

	}


