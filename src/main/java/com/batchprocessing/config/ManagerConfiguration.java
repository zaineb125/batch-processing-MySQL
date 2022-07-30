package com.batchprocessing.config;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.chunk.RemoteChunkingManagerStepBuilderFactory;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.dsl.Jms;
import com.batchprocessing.mapper.CustomerRowMapper;
import com.batchprocessing.model.Customer;
import com.batchprocessing.repository.CustomerRepository;
import com.batchprocessing.repository.NewCustomerRepository;

@Configuration
@EnableBatchIntegration
@EnableIntegration
@EnableBatchProcessing
@Import(ChannelConfiguration.class)
@Profile(value="master")
public class ManagerConfiguration {
	
		
			@Autowired
			private JobBuilderFactory jobBuilderFactory;

			@Autowired
			private RemoteChunkingManagerStepBuilderFactory managerStepBuilderFactory;
			
			@Autowired
			DataSource dataSource ;

			@Autowired
			private DirectChannel managerRequests ;
			
			@Autowired
			private QueueChannel managerReplies ;
		
			
			@Bean
			public IntegrationFlow outboundFlow(ActiveMQConnectionFactory connectiontFactory) {
				return IntegrationFlows.from(managerRequests).handle(Jms.outboundAdapter(connectiontFactory).destination("managerRequests"))
						.get();
			}

			
			
			@Bean
			public IntegrationFlow inboundFlow(ActiveMQConnectionFactory connectionFactory) {
				return IntegrationFlows.from(Jms.messageDrivenChannelAdapter(connectionFactory).destination("managerReplies"))
						.channel(managerReplies).get();
			}

			
			
			@Bean
			public JdbcCursorItemReader<Customer> itemReader(){
				
				JdbcCursorItemReader<Customer> reader = new JdbcCursorItemReader<Customer>();
				reader.setDataSource(dataSource);
				reader.setSql("SELECT CustomerID,Genre,Age,Annual_Income,Spending_Score,updated,update_Date FROM customer");
				reader.setRowMapper(new CustomerRowMapper());
				
				return reader ;
			}
			 

			@SuppressWarnings("unused")
			@Bean
			public TaskletStep managerStep() {
				return this.managerStepBuilderFactory.get("managerStep").<Customer, Customer>chunk(103).reader(itemReader())
						.outputChannel(managerRequests).inputChannel(managerReplies).build();
			}

			@Bean
			public Job remoteChunkingJob() {
				return this.jobBuilderFactory.get("remoteChunkingJob").start(managerStep()).build();
			}
}


