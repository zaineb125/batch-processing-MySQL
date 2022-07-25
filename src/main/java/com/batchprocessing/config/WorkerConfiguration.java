package com.batchprocessing.config;

import javax.jms.JMSException;
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
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.dsl.Jms;

import lombok.AllArgsConstructor;

@Configuration
@EnableBatchIntegration
@EnableIntegration
@EnableBatchProcessing
public class WorkerConfiguration {
	

	@Autowired
	private RemoteChunkingWorkerBuilder<Integer, Integer> remoteChunkingWorkerBuilder;
	

	
	@Bean
	public ActiveMQConnectionFactory connectionFactory() throws JMSException {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		connectionFactory.setBrokerURL("tcp://localhost:61616");
		return connectionFactory;
	}

	
	@Bean
	public DirectChannel requests() {
		return new DirectChannel();
	}

	@Bean
	public IntegrationFlow inboundFlow(ActiveMQConnectionFactory connectionFactory) {
		return IntegrationFlows.from(Jms.messageDrivenChannelAdapter(connectionFactory).destination("requests"))
				.channel(requests()).get();
	}

	
	@Bean
	public QueueChannel replies() {
		return new QueueChannel();
	}

	@Bean
	public IntegrationFlow outboundFlow(ActiveMQConnectionFactory connectionFactory) {
		return IntegrationFlows.from(replies()).handle(Jms.outboundAdapter(connectionFactory).destination("replies"))
				.get();
	}

	
	@Bean
	public ItemProcessor<Integer, Integer> itemProcessor() {
		return item -> {
			System.out.println("processing item " + item);
			return item;
		};
	}

	@Bean
	public ItemWriter<Integer> itemWriter() {
		return items -> {
			for (Integer item : items) {
				System.out.println("writing item " + item);
			}
		};
	}

	@Bean
	public IntegrationFlow workerIntegrationFlow() {
		return this.remoteChunkingWorkerBuilder.itemProcessor(itemProcessor()).itemWriter(itemWriter())
				.inputChannel(requests()).outputChannel(replies()).build();
	}
	   

}
