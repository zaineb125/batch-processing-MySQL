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
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.jms.annotation.EnableJms;

import lombok.AllArgsConstructor;

@Configuration
@EnableBatchIntegration
@EnableIntegration
@EnableBatchProcessing
@EnableJms
public class WorkerConfiguration {
	

	@Autowired
	private RemoteChunkingWorkerBuilder<Integer, Integer> remoteChunkingWorkerBuilder;

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
	public DirectChannel requests() {
		return new DirectChannel();
	}

	@Bean
	public IntegrationFlow inboundFlow() throws JMSException {
		return IntegrationFlows.from(Jms.messageDrivenChannelAdapter(connectionFactory()).destination("requests"))
				.channel(requests()).get();
	}

	
	@Bean
	public QueueChannel replies() {
		return new QueueChannel();
	}

	@Bean
	public IntegrationFlow outboundFlow() throws JMSException {
		return IntegrationFlows.from(replies()).handle(Jms.outboundAdapter(connectionFactory()).destination("replies"))
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
