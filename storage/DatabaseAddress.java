package edu.upenn.cis455.storage;

import com.sleepycat.persist.model.*;
//import org.apache.commons.lang3.SystemUtils;

@Entity
public class DatabaseAddress {
	
	@PrimaryKey
	private String primaryKey;

	public String recentChange = "";
	public String crc = "";

	// set primary key
	public void updatePrimaryKey(String pk){
		primaryKey = pk;
	}

	// return primary key for the object
	public String fetchPrimaryKey(){
		return primaryKey;
	}

	// set last modified time
	public void updateRecentChange(String s){
		recentChange = s;
	}

	// return last modified time
	public String fetchRecentChange(){
		return recentChange;
	}

	// setting up checksum
	public void updateCRC(String arg0){
		crc = arg0;
	}

	// method to return checksum
	public String fetchCRC(){
		return crc;
	}
}
