package com.batchprocessing.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.step.skip.NonSkippableReadException;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.chunk.RemoteChunkingManagerStepBuilderFactory;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.orm.JpaNativeQueryProvider;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.batchprocessing.model.Customer;
import com.batchprocessing.model.JobExecutions;
import com.batchprocessing.repository.CustomerRepository;
import com.batchprocessing.repository.JobExecutionRepository;

@Configuration
@EnableBatchIntegration
@EnableIntegration
@EnableBatchProcessing
@Import(ChannelConfiguration.class)
@Controller
@Profile(value="master")
public class ManagerConfiguration {
	

			@Autowired
			JobLauncher jobLauncher ; 
	
		
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
			
			@Autowired
			JobExecutionRepository jobExecutionRepository ;
			
			@Autowired
			CustomerRepository customerRepository ;
			
			@Autowired
			EntityManagerFactory entityManagerFactory ;
			
			
		
			
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
			@StepScope()
			/*
			public JdbcCursorItemReader<Customer> itemReader(){
				
				JdbcCursorItemReader<Customer> reader = new JdbcCursorItemReader<Customer>();
				reader.setDataSource(dataSource);
				reader.setSql("SELECT CustomerID,Genre,Age,Annual_Income,Spending_Score,updated,update_date FROM customer");
				reader.setRowMapper(new CustomerRowMapper());
				
				return reader ;
			}
			*/
			
			/*public ListItemReader<Customer> itemReader() {
				List<Customer> response = (List<Customer>) customerRepository.findAll();
				return new ListItemReader<>(response) ;
				
		    }*/
			
			
		
			public JpaPagingItemReader<Customer> itemReader() throws Exception{
			    JpaPagingItemReader<Customer> reader = new JpaPagingItemReader<Customer>();
			    String sqlQuery = "SELECT CustomerID,Genre,Age,Annual_Income,Spending_Score,updated,update_date FROM customer";
			    JpaNativeQueryProvider<Customer> queryProvider = new JpaNativeQueryProvider<Customer>();
			    queryProvider.setSqlQuery(sqlQuery);
			    queryProvider.setEntityClass(Customer.class);
			    reader.setEntityManagerFactory(entityManagerFactory);
			    reader.setQueryProvider(queryProvider);
			 
			    reader.setSaveState(true);
			    return reader;
			    
			}
			 
			@SuppressWarnings("unused")
			@Bean
			public TaskletStep managerStep() throws Exception {
				
				return this.managerStepBuilderFactory.get("managerStep").<Customer, Customer>chunk(103).skip(RuntimeException.class).reader(itemReader())
						.outputChannel(managerRequests).inputChannel(managerReplies).build();
			}

		
		
			public BatchStatus remoteChunkingJob() throws Exception {
				Map<String,JobParameter>maps = new HashMap<>();
				maps.put("time",new JobParameter(System.currentTimeMillis()));
				
				JobParameters parameters = new JobParameters(maps);
				JobExecution jobExecution = jobLauncher.run(this.jobBuilderFactory.get("remoteChunkingJob").start(managerStep()).build(), parameters);
				System.out.println("JobExecution Id : "+jobExecution.getJobId()+"  JobExecution Status :  "+jobExecution.getStatus());
				
				System.out.println("Batch is Running ... ");
				
				while(jobExecution.isRunning()) {
					System.out.println("...");
				}
				
				return jobExecution.getStatus();
				
			}
			@GetMapping("/load")
			 public String getJobs(Model model) throws Exception  {
				
				remoteChunkingJob() ;
				
				List<JobExecutions> jobList = jobExecutionRepository.findAll();
				
				model.addAttribute("jobs",jobList);
			
				return "jobs";
			}
}


