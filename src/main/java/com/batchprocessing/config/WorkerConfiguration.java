package com.batchprocessing.config;

import javax.sql.DataSource;

import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.step.item.ChunkProcessor;
import org.springframework.batch.core.step.item.SimpleChunkProcessor;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.chunk.ChunkMessageChannelItemWriter;
import org.springframework.batch.integration.chunk.ChunkProcessorChunkHandler;
import org.springframework.batch.integration.chunk.RemoteChunkingManagerStepBuilderFactory;
import org.springframework.batch.integration.chunk.RemoteChunkingWorkerBuilder;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.dsl.Jms;

import com.batchprocessing.mapper.CustomerRowMapper;
import com.batchprocessing.model.Customer;
import com.batchprocessing.processor.CustomerItemProcessor;

import lombok.AllArgsConstructor;

@Configuration
@EnableBatchIntegration
@EnableBatchProcessing
@AllArgsConstructor
public class WorkerConfiguration {
	
	    	@Autowired
	    	public DataSource dataSource;
	    	
	        @Autowired
	        private RemoteChunkingWorkerBuilder workerBuilder;
	        
	        @Autowired
	    	public JobBuilderFactory jobBuilderFactory;
		        
	       
	        @Bean
	        public org.apache.activemq.ActiveMQConnectionFactory connectionFactory() {
	            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
	            factory.setBrokerURL("tcp://localhost:61616");
	            return factory;
	        }
	        /*
	         * Declare the DirectChannel for requests
	         */
	        @Bean
	        public DirectChannel requests() {
	            return new DirectChannel();
	        }
	        @Bean
	        public IntegrationFlow inboundFlow(ActiveMQConnectionFactory connectionFactory) {
	            return IntegrationFlows
	                    .from(Jms.messageDrivenChannelAdapter(connectionFactory).destination("requests"))
	                    .channel(requests())
	                    .get();
	        }
	        
	        /*
	         * Declare the QueueChannel for replies
	         */
	        @Bean
	        public QueueChannel replies() {
	            return new QueueChannel();
	        }
	        @Bean
	        public IntegrationFlow outboundFlow(ActiveMQConnectionFactory connectionFactory) {
	            return IntegrationFlows
	                    .from(replies())
	                    .handle(Jms.outboundAdapter(connectionFactory).destination("replies"))
	                    .get();
	        }
	        /*
	         * Configure the ChunkProcessorChunkHandler
	         */
	        @Bean
	        @ServiceActivator(inputChannel = "requests", outputChannel = "replies")
	        public ChunkProcessorChunkHandler<Customer> chunkProcessorChunkHandler() {
	            ChunkProcessor<Customer> chunkProcessor
	                    = new SimpleChunkProcessor<>(itemProcessor(), itemWriter());
	            ChunkProcessorChunkHandler<Customer> chunkProcessorChunkHandler
	                    = new ChunkProcessorChunkHandler<>();
	            chunkProcessorChunkHandler.setChunkProcessor(chunkProcessor);
	            return chunkProcessorChunkHandler;
	        }
	        @Bean
	    	public ItemWriter<Customer> itemWriter() {
	    		JdbcBatchItemWriter<Customer> writer = new JdbcBatchItemWriter<Customer>();
	    		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Customer>());
	    		writer.setSql("INSERT INTO new_batch_job_execution (inserted_customerid,genre,age,annual_income,spending_score) VALUES (:CustomerID,:Genre,:Age,:Annual_Income,:Spending_Score)");
	    		writer.setDataSource(dataSource);
	    		return writer;
	    	}
	        /*
	         * Configure the itemProcessor
	         */
	        @Bean
	    	public CustomerItemProcessor itemProcessor() {
	    		return new CustomerItemProcessor();
	    	}
	        /*
	         * Configure the workerFlow
	         */
	        @Bean
	        public IntegrationFlow workerFlow() {
	            return this.workerBuilder
	                       .itemProcessor(itemProcessor())
	                       .itemWriter(itemWriter())
	                       .inputChannel(requests()) // requests received from the manager
	                       .outputChannel(replies()) // replies sent to the manager
	                       .build();
	        }
	   

}
