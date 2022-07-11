package com.batchprocessing.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.batchprocessing.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer,Customer> {
	
}


