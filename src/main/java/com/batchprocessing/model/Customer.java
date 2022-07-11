package com.batchprocessing.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="customer")
public class Customer {
	
	@Id
	
	
	public String CustomerID ;
	
	public String Genre ;
	
	public String Age ;
	
	public String Annual_Income ;

	public String Spending_Score ;
	
	
	public String getCustomerID() {
		return CustomerID;
	}
	public void setCustomerID(String customerID) {
		CustomerID = customerID;
	}
	public String getGenre() {
		return Genre;
	}
	public void setGenre(String genre) {
		Genre = genre;
	}
	public String getAge() {
		return Age;
	}
	public void setAge(String age) {
		Age = age;
	}
	public String getAnnual_Income() {
		return Annual_Income;
	}
	public void setAnnual_Income(String annual_Income) {
		Annual_Income = annual_Income;
	}
	public String getSpending_Score() {
		return Spending_Score;
	}
	public void setSpending_Score(String spending_Score) {
		Spending_Score = spending_Score;
	}
	
	
}
