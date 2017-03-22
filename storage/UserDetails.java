package edu.upenn.cis455.storage;

import com.sleepycat.persist.model.*;

@Entity
public class UserDetails {
	
	// username must be unique
	@PrimaryKey
	private String name;

	private String identification = "";

	// set username
	public void updateName(String userName){
		name = userName;
	}

	// return username
	public String fetchName(){
		return name;
	}

	// set password
	public void updateIdentification(String password){
		identification = password;
	}

	// return password for authentication
	public String fetchIdentification(){
		return identification;
	}
}
