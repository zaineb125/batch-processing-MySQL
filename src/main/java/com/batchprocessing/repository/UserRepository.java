package com.batchprocessing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.batchprocessing.model.Customer;

@Repository 
public interface UserRepository extends JpaRepository<Customer,Integer>{

}
