package com.batchprocessing.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;

@Entity
@Table(name="new_batch_job_execution")
public class NewJobExecution {

	
	
	
	@Id
	public String InsertedCustomerID ;
	
	public String Genre ;
	
	public String Age ;
	
	public String Annual_Income ;

	public String Spending_Score ;
	
	
	public String getInsertedCustomerID() {
		return InsertedCustomerID;
	}

	public void setInsertedCustomerID(String insertedCustomerID) {
		InsertedCustomerID = insertedCustomerID;
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
