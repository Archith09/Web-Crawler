package edu.upenn.cis455.storage;

import com.sleepycat.persist.model.*;

@Entity
public class SaveData {

	// to maintain uniqueness of primary key in the database
	@PrimaryKey
	private String myPrimaryKey;

//	private String key;
	private Parameter parameter;

	// constructor
	public SaveData(){

	}

	// set primary key for a given object
	public void updatePrimaryKey(String s){
		myPrimaryKey = s;
	}

	// return primary key for a given object
	public String fetchPrimaryKey(){
		return myPrimaryKey;
	}

	// set url string
	public void updateParameter(Parameter p){
		parameter = p;
	}

	// return url string
	public Parameter fetchParameter(){
		return parameter;
	}

}
