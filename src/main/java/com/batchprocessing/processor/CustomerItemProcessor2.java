package com.batchprocessing.processor;

import org.springframework.batch.item.ItemProcessor;

import com.batchprocessing.model.Customer;

public class CustomerItemProcessor2 implements ItemProcessor<Customer , Customer>  {
	@Override
	public Customer process(Customer customer) throws Exception {
		System.out.println("Step2");
		System.out.println("Reading from database Id :  "+ customer.getCustomerID());
		System.out.println("Reading from database Genre :  "+ customer.getGenre());
		System.out.println("Reading from database Genre :  "+ customer.getGenre());
		return customer;
	}
}
