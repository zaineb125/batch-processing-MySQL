package com.batchprocessing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.batchprocessing.model.NewCustomer;


public interface NewCustomerRepository extends JpaRepository<NewCustomer,NewCustomer>{

}
