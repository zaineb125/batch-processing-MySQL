package com.batchprocessing.config;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.dsl.Jms;

import com.batchprocessing.mapper.CustomerRowMapper;
import com.batchprocessing.model.Customer;

import lombok.AllArgsConstructor;

@Configuration
@EnableBatchIntegration
@EnableBatchProcessing
@AllArgsConstructor
public class ManagerConfiguration {
	
		@Autowired
    	public DataSource dataSource;
    	
    	@Autowired
	    private RemoteChunkingManagerStepBuilderFactory managerStepBuilderFactory;
    	
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
        
        /*
         * Declare the QueueChannel for replies
         */
        @Bean
        public QueueChannel replies() {
            return new QueueChannel();
        }
        
        
        /*
         * Declare theoutboundFlow for requests
         */
        @Bean
        public IntegrationFlow outboundFlow(ActiveMQConnectionFactory connectionFactory) {
            return IntegrationFlows
                    .from(requests())
                    .handle(Jms.outboundAdapter(connectionFactory).destination("requests"))
                    .get();
        }
        
        /*
         * Declare inboundFlow for replies
         */
        @Bean
        public IntegrationFlow inboundFlow(ActiveMQConnectionFactory connectionFactory) {
            return IntegrationFlows
                    .from(Jms.messageDrivenChannelAdapter(connectionFactory).destination("replies"))
                    .channel(replies())
                    .get();
        }

        /*
         * Declare the itemReader
         */
        @Bean
    	public ItemStreamReader<Customer> itemReader() {
    		JdbcCursorItemReader<Customer> reader = new JdbcCursorItemReader<Customer>();
    		reader.setDataSource(dataSource);
    		reader.setSql("SELECT CustomerID,Genre,Age,Annual_Income,Spending_Score FROM customer");
    		reader.setRowMapper(new CustomerRowMapper());
    		return reader;
    	}
        /*
         * Configure the ChunkMessageChannelItemWriter
         */
        @Bean
        public ItemWriter<Customer> itemWriter() {
            MessagingTemplate messagingTemplate = new MessagingTemplate();
            messagingTemplate.setDefaultChannel(requests());
            messagingTemplate.setReceiveTimeout(2000);
            ChunkMessageChannelItemWriter<Customer> chunkMessageChannelItemWriter
                    = new ChunkMessageChannelItemWriter<>();
            chunkMessageChannelItemWriter.setMessagingOperations(messagingTemplate);
            chunkMessageChannelItemWriter.setReplyChannel(replies());
            return chunkMessageChannelItemWriter;
        }
        
        /*
         * Declare the managerStep
         */
        @Bean
        public TaskletStep managerStep() {
            return this.managerStepBuilderFactory.get("managerStep")
                       .chunk(100)
                       .reader(itemReader())
                       .outputChannel(requests()) // requests sent to workers
                       .inputChannel(replies())   // replies received from workers
                       .build();
        }
        
        @Bean
    	public Job exportCustomerJob(Customer customer) throws Exception {
    	
    		return jobBuilderFactory.get("exportCustomerJob")
    								.start(managerStep())
    								.build();
    						       
    	}
        
        

}


