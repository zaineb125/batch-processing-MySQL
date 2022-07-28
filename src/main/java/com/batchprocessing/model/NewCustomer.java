package com.batchprocessing.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.stereotype.Component;

@Entity
@Table(name="new_customer")
@Component
public class NewCustomer {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public String Id ;

	public String Genre ;
	
	public String Age ;
	

	public void setId(String id) {
		this.Id = id;
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
	
	public String getId() {
		return Id;
	}

	
}
