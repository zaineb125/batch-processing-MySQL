package com.batchprocessing.config;

import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;

@Configuration
public class ChannelConfiguration {
	
	@Value("${spring.activemq.broker-url}")
    String BROKER_URL;
    @Value("${spring.activemq.user}")
    String BROKER_USERNAME;
    @Value("${spring.activemq.password}")
    String BROKER_PASSWORD;
    
    @Bean
    public ActiveMQConnectionFactory connectionFactory(){
        ActiveMQConnectionFactory connectionFactory = new  ActiveMQConnectionFactory();
        connectionFactory.setTrustAllPackages(true);
        connectionFactory.setBrokerURL(BROKER_URL);
        connectionFactory.setPassword(BROKER_USERNAME);
        connectionFactory.setUserName(BROKER_PASSWORD);
        return connectionFactory;
    }
    
    @Bean
	public DirectChannel workerRequests() {
		return new DirectChannel();
	}
    
    @Bean
	public DirectChannel workerReplies() {
		return new DirectChannel();
	}
    
    @Bean
	public DirectChannel managerRequests() {
		return new DirectChannel();
	}
    
    @Bean
	public QueueChannel managerReplies() {
		return new QueueChannel();
	}
    
}
