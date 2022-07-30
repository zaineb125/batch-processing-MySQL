package com.batchprocessing.config;


import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.jms.JMSException;
import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.integration.chunk.RemoteChunkingWorkerBuilder;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.item.ItemProcessor;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.dsl.Jms;
import com.batchprocessing.model.Customer;
import com.batchprocessing.model.NewCustomer;
import com.batchprocessing.repository.CustomerRepository;


@Configuration
@EnableBatchIntegration
@EnableIntegration
@EnableBatchProcessing
@Import(ChannelConfiguration.class)
@Profile(value="slave")
public class WorkerConfiguration {
	

	@Autowired
	private RemoteChunkingWorkerBuilder<Customer, Customer> remoteChunkingWorkerBuilder;
	
	@Autowired
	private DirectChannel managerRequests ;
	
	@Autowired
	private QueueChannel managerReplies ;
	
	@Autowired
	private DirectChannel workerReplies ;
	
	@Autowired
	private CustomerRepository customerRepository ;
		


	Logger logger = LoggerFactory.getLogger(WorkerConfiguration.class);
	
	
	@Bean
	public IntegrationFlow inboundFlow(ActiveMQConnectionFactory connectionFactory) throws JMSException {
		return IntegrationFlows.from(Jms.messageDrivenChannelAdapter(connectionFactory).destination("managerRequests"))
				.channel(managerRequests).get();
	}

	
	@Bean
	public IntegrationFlow outboundFlow(ActiveMQConnectionFactory connectionFactory) throws JMSException {
		return IntegrationFlows.from(workerReplies).handle(Jms.outboundAdapter(connectionFactory).destination("managerReplies"))
				.get();
	}

	
	@Bean
	public ItemProcessor<Customer, Customer> itemProcessor() {
		return customer -> {
			logger.trace("processing item " + customer.CustomerID);
			return customer;
		};
	}

	@Bean
	public ItemWriter<Customer> itemWriter() {
		return customers -> {
			for (Customer customer : customers) {
				logger.trace("writing item " + customer.CustomerID);
				customer.setUpdated((byte) 1);
				customer.setUpdateDate(LocalTime.now());
				customerRepository.save(customer);
				
			}
		};
	}

	@Bean
	public IntegrationFlow workerIntegrationFlow() throws Exception {
		return this.remoteChunkingWorkerBuilder.itemProcessor(itemProcessor()).itemWriter(itemWriter())
				.inputChannel(managerRequests).outputChannel(managerReplies).build();
	}
	

}
	   
	
	


