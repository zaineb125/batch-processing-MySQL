package com.batchprocessing.model;


import java.io.Serializable;
import java.time.LocalTime;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.stereotype.Component;


@Entity
@Table(name="customer")
@Component
public class Customer implements Serializable {
	
	//private static final long serialVersionUID = 1L;


	/**
	 * 
	 */
	private static final long serialVersionUID = 6843003181192210758l;


	public byte getUpdated() {
		return updated;
	}
	public void setUpdated(byte updated) {
		this.updated = updated;
	}
	public LocalTime getUpdateDate() {
		return update_date;
	}
	public void setUpdateDate(LocalTime updateDate) {
		this.update_date = updateDate;
	}
	@Id
	public String CustomerID ;
	
	public String Genre ;
	
	public String Age ;
	
	public String Annual_Income ;

	public String Spending_Score ;
	
	public byte updated ;
	
	public LocalTime update_date ;
	
		
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
