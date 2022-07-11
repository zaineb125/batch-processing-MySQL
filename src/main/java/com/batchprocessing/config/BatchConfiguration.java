package com.batchprocessing.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
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
import org.springframework.jdbc.core.RowMapper;



import com.batchprocessing.model.Customer;
import com.batchprocessing.processor.CustomerItemProcessor;
import com.batchprocessing.repository.CustomerRepository;

import lombok.AllArgsConstructor;


@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class BatchConfiguration {

	//private Resource outputResource = new FileSystemResource("output/customer.csv");
	
	@Autowired
	public JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	public StepBuilderFactory stepBuilderFactory ;
	
	@Autowired
	public DataSource dataSource;
	
	@Autowired
	public CustomerRepository customerRepository ;
	

	//Cursor for Reading data from dataBase 
	
	@Bean
	public JdbcCursorItemReader<Customer> reader(){
		
		JdbcCursorItemReader<Customer> reader = new JdbcCursorItemReader<Customer>();
		reader.setDataSource(dataSource);
		reader.setSql("SELECT CustomerID,Genre,Age,Annual_Income,Spending_Score FROM customer");
		reader.setRowMapper(new CustomerRowMapper());
		
		return reader ;
	}

	//Processor to process data 
	
	@Bean
	public CustomerItemProcessor processor() {
		
		return new CustomerItemProcessor();
	}
	/*
	@Bean
	public FlatFileItemWriter<Customer> writer() throws Exception{
		
		DBWriter dbWriter = new DBWriter(customerRepository);
		dbWriter.writeInDB();
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
	*/
	@Bean
	public Step step1() throws Exception {
		return stepBuilderFactory.get("step1")
								  .<Customer,Customer> chunk(10)
								  .reader(reader())
								  .processor(processor())
								  .writer(new DBWriter(customerRepository))
								  .build();
								  
	}
	
	@Bean
	public Job exportCustomerJob() throws Exception {
		
		return jobBuilderFactory.get("exportCustomerJob")
								.incrementer(new RunIdIncrementer())
								.start(step1())
								.build();
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
	
	public class DBWriter implements ItemWriter<Customer> {
		
		private CustomerRepository customerRepository;
		
		@Autowired
		public DBWriter(CustomerRepository customerRepository) {
			this.customerRepository = customerRepository;
		}
		
		
		public void writeInDB()throws Exception{
			Customer customer = new Customer();
			customer.setCustomerID("206");
			customer.setGenre("Female");
			customer.setAge("22");
			customer.setAnnual_Income("20000");
			customer.setSpending_Score("35");
			System.out.println("Insert a customer");
			customerRepository.save(customer);
		}


		@Override
		public void write(List<? extends Customer> items) throws Exception {
			writeInDB();
		}
		
	}
}
