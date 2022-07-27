package com.batchprocessing.config;

import java.util.Arrays;

import javax.jms.JMSException;
import javax.sql.DataSource;
import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.chunk.ChunkMessageChannelItemWriter;
import org.springframework.batch.integration.chunk.RemoteChunkingManagerStepBuilderFactory;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import lombok.AllArgsConstructor;

@Configuration
@EnableBatchIntegration
@EnableIntegration
@EnableBatchProcessing
@EnableJms
public class ManagerConfiguration {
	
		
			@Autowired
			private JobBuilderFactory jobBuilderFactory;

			@Autowired
			private RemoteChunkingManagerStepBuilderFactory managerStepBuilderFactory;


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
			public IntegrationFlow outboundFlow(ActiveMQConnectionFactory connectionFactory) {
				return IntegrationFlows.from(requests()).handle(Jms.outboundAdapter(connectionFactory).destination("requests"))
						.get();
			}

			
			@Bean
			public QueueChannel replies() {
				return new QueueChannel();
			}

			@Bean
			public IntegrationFlow inboundFlow(ActiveMQConnectionFactory connectionFactory) {
				return IntegrationFlows.from(Jms.messageDrivenChannelAdapter(connectionFactory).destination("replies"))
						.channel(replies()).get();
			}

			
			@Bean
			public ListItemReader<Integer> itemReader() {
				return new ListItemReader<>(Arrays.asList(1, 2, 3, 4, 5, 6,7,8,9));
			}

			@Bean
			public TaskletStep managerStep() {
				return this.managerStepBuilderFactory.get("managerStep").<Integer, Integer>chunk(3).reader(itemReader())
						.outputChannel(requests()).inputChannel(replies()).build();
			}

			@Bean
			public Job remoteChunkingJob() {
				return this.jobBuilderFactory.get("remoteChunkingJob").start(managerStep()).build();
			}
        

}


