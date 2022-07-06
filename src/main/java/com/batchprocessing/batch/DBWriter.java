package com.batchprocessing.batch;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.batchprocessing.model.Customer;
import com.batchprocessing.repository.UserRepository;

@Component
public class DBWriter implements ItemWriter<Customer> {
	
	private UserRepository userRepository;
	
	@Autowired
	public DBWriter(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	@Override
	public void write(List<? extends Customer> users)throws Exception{
		System.out.println("Data Saved for Users :" + users);
		userRepository.saveAll(users);
	}
	
}
