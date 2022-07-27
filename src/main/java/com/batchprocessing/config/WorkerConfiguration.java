package com.batchprocessing.config;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Component
public class WorkerConfiguration {
	 
	   

	    @JmsListener(destination = "${spring.activemq.topic}")
	    public void receiveMessageFromTopic(final Message jsonMessage) throws JMSException {
	        String messageData = null;
	        System.out.println("Received message in 2nd topic " + jsonMessage);
	        if(jsonMessage instanceof TextMessage) {
	            TextMessage textMessage = (TextMessage)jsonMessage;
	            messageData = textMessage.getText();
	            System.out.println("messageData in 2nd listener:"+messageData);
	        }
	    }	
}
