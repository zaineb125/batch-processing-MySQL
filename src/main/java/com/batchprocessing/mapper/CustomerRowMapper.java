package com.batchprocessing.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.batchprocessing.model.Customer;

public class CustomerRowMapper implements RowMapper<Customer> {

	@Override
	public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
		Customer customer = new Customer();
		
		customer.setAge(rs.getString("Age"));
		customer.setGenre(rs.getString("Genre"));
		customer.setCustomerID(rs.getString("CustomerID"));
		customer.setAnnual_Income(rs.getString("Annual_Income"));
		customer.setSpending_Score(rs.getString("Spending_Score"));
		
		return customer;
	}
}
