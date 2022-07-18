package com.batchprocessing.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Flow;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.batchprocessing.mapper.CustomerRowMapper;
import com.batchprocessing.model.Customer;
import com.batchprocessing.model.NewJobExecution;
import com.batchprocessing.processor.CustomerItemProcessor;
import com.batchprocessing.processor.CustomerItemProcessor2;
import com.batchprocessing.repository.CustomerRepository;
import com.batchprocessing.repository.NewJobExecutionRepository;

import lombok.AllArgsConstructor;


@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class BatchConfiguration {

	private Resource outputResource = new FileSystemResource("output/customers.csv");
	
	@Autowired
	public JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	public StepBuilderFactory stepBuilderFactory ;
	
	@Autowired
	public DataSource dataSource;
	
	@Autowired
	public CustomerRepository customerRepository ;
	
	@Autowired
	public NewJobExecutionRepository newJobExecutionRepository ;
	
	


//**********************************************Step1***************************************************************************
	
	@Bean
	public CustomerItemProcessor processor1() {
		return new CustomerItemProcessor();
	}
	
	
	@Bean
	public ItemStreamReader<Customer>reader1() {
		JdbcCursorItemReader<Customer> reader = new JdbcCursorItemReader<Customer>();
		reader.setDataSource(dataSource);
		reader.setSql("SELECT CustomerID,Genre,Age,Annual_Income,Spending_Score FROM customer");
		reader.setRowMapper(new CustomerRowMapper());
		return reader;
	}
	
	public class CustomerRowMapper implements RowMapper<Customer>{

		@Override
		public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
			Customer customer = new Customer();
			customer.setGenre(rs.getString("Genre"));
			customer.setAge(rs.getString("Age"));
			customer.setCustomerID(rs.getString("CustomerID"));
			customer.setAnnual_Income(rs.getString("Annual_Income"));
			customer.setSpending_Score(rs.getString("Spending_Score"));
			
			return customer ;
		}
		
	}
	
	@Bean
	public ItemWriter<Customer> writer1() {
		JdbcBatchItemWriter<Customer> writer = new JdbcBatchItemWriter<Customer>();
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Customer>());
		writer.setSql("INSERT INTO new_batch_job_execution (inserted_customerid,genre,age,annual_income,spending_score) VALUES (:CustomerID,:Genre,:Age,:Annual_Income,:Spending_Score)");
		writer.setDataSource(dataSource);
		return writer;
	}
	
	@Bean
	public Step step1(Customer customer) throws Exception {
		System.out.println("************** Step1 initiated ***************");
		return stepBuilderFactory.get("step1")
								  .<Customer,Customer> chunk(10)
								  .reader(reader1())
								  .processor(processor1())
								  .writer(writer1())
								  .build();
								  
	}
	
	
	
	//**********************************************Step2***************************************************************************
	
	@Bean
	public JdbcCursorItemReader<Customer> reader2(){
		
		JdbcCursorItemReader<Customer> reader = new JdbcCursorItemReader<Customer>();
		reader.setDataSource(dataSource);
		reader.setSql("SELECT CustomerID,Genre,Age,Annual_Income,Spending_Score FROM customer");
		reader.setRowMapper(new CustomerRowMapper());
		
		return reader ;
	}
	@Bean
	public CustomerItemProcessor2 processor2() {
		return new CustomerItemProcessor2();
	}
	@Bean
	public FlatFileItemWriter<Customer> writer2() throws Exception{
		FlatFileItemWriter<Customer> writer = new FlatFileItemWriter<Customer>();
		writer.setResource(outputResource);
		
		DelimitedLineAggregator<Customer> lineAggregator = new DelimitedLineAggregator<Customer>();
		lineAggregator.setDelimiter(",");
		
		BeanWrapperFieldExtractor<Customer> fieldExtractor = new BeanWrapperFieldExtractor<Customer>();
		fieldExtractor.setNames(new String[] {"CustomerID","Genre","Age","Annual_Income","Spending_Score"});
		lineAggregator.setFieldExtractor(fieldExtractor);
		
		writer.setLineAggregator(lineAggregator);
		return writer ; 

	}
	
	@Bean
	public Step step2(Customer customer) throws Exception {
		System.out.println("************** Step2 initiated ***************");
		return stepBuilderFactory.get("step2")
								  .<Customer,Customer> chunk(10)
								  .reader(reader2())
								  .processor(processor2())
								  .writer(writer2())
								  .build();
								  
	}
	//*************************************************Flow*************************************************************************************
	@Bean
			public SimpleFlow splitFlow() throws Exception {
			    return new FlowBuilder<SimpleFlow>("splitFlow")
			        .split(taskExecutor())
			        .add(flow1(),flow2())
			        .build();
			}
		
			@Bean
			public SimpleFlow flow1() throws Exception {
				System.out.println("************** Step1 Beguin ***************");
				Customer customer =new Customer();
			    return new FlowBuilder<SimpleFlow>("flow1")
			        .start(step1(customer))
			        .build();
			}
			@Bean
			public SimpleFlow flow2() throws Exception {
				System.out.println("**************Step2 Beguin ***************");
				Customer customer =new Customer();
			    return new FlowBuilder<SimpleFlow>("flow2")
			        .start(step2(customer))
			        .build();
			}
			
		
			@Bean
			public TaskExecutor taskExecutor() {
			    return new SimpleAsyncTaskExecutor("spring_batch");
			}
	//*****************************************************Job*********************************************************************************
	
	@Bean
	public Job exportCustomerJob(Customer customer) throws Exception {
	
		return jobBuilderFactory.get("exportCustomerJob")
								.start(splitFlow())
								.build()
						        .build();     
	}
	
	
	
	//**************************************************WriterClass**************************************************
	
	public class DBWriter implements ItemWriter<Customer> {
		
		private NewJobExecutionRepository newJobExecutionRepository;
		
		@Autowired
		public DBWriter(NewJobExecutionRepository newJobExecutionRepository) {
			this.newJobExecutionRepository = newJobExecutionRepository;
			
		}
		
		
		public void writeInDB()throws Exception{
			NewJobExecution newJobExecution =new NewJobExecution();
			Customer customer = new Customer();
			customer.setCustomerID("210");
			customer.setGenre("Male");
			customer.setAge("22");
			customer.setAnnual_Income("6000");
			customer.setSpending_Score("200");
			customerRepository.save(customer);
			
			System.out.println("Insert a customer");
			
			newJobExecution.setInsertedCustomerID(customer.getCustomerID());
			newJobExecution.setGenre(customer.getGenre());
			newJobExecution.setAge(customer.getAge());
			newJobExecution.setAnnual_Income(customer.getAnnual_Income());
			newJobExecution.setSpending_Score(customer.getSpending_Score());
			newJobExecution.setInsertedCustomerID(customer.getCustomerID());
			newJobExecutionRepository.save(newJobExecution);
		}


		@Override
		public void write(List<? extends Customer> items) throws Exception {
			writeInDB();
		}
		
	}
}
