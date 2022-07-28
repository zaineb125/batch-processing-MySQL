package com.batchprocessing.config;

import java.util.Arrays;

import javax.sql.DataSource;

import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.chunk.ChunkMessageChannelItemWriter;
import org.springframework.batch.integration.chunk.RemoteChunkingManagerStepBuilderFactory;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.jms.annotation.EnableJms;

import com.batchprocessing.mapper.CustomerRowMapper;
import com.batchprocessing.model.Customer;

@Configuration
@EnableBatchIntegration
@EnableIntegration
@EnableBatchProcessing
@Import(ChannelConfiguration.class)
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

			
			/*@Bean
			public ListItemReader<Integer> itemReader() {
				return new ListItemReader<>(Arrays.asList(1, 2, 3, 4, 5, 6,7,8,9));
			}*/
			
			@Bean
			public JdbcCursorItemReader<Customer> itemReader(){
				
				JdbcCursorItemReader<Customer> reader = new JdbcCursorItemReader<Customer>();
				reader.setDataSource(dataSource);
				reader.setSql("SELECT CustomerID,Genre,Age,Annual_Income,Spending_Score FROM customer");
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


