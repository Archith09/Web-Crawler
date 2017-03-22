package edu.upenn.cis455.storage;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

public class Service {

	PrimaryIndex<String, SaveData> primaryIndex;
	PrimaryIndex<String, DatabaseAddress> address;
	PrimaryIndex<String, UserDetails> details;

	// constructor to set primary index, url and user info
	public Service(EntityStore entityObj)
	throws DatabaseException{
		primaryIndex = entityObj.getPrimaryIndex(String.class, SaveData.class);
		address = entityObj.getPrimaryIndex(String.class, DatabaseAddress.class);
		details = entityObj.getPrimaryIndex(String.class, UserDetails.class);
	}

	// return primary index
	public PrimaryIndex<String, SaveData> returnPrimaryIndex(){
		return primaryIndex;
	}

	// return url
	public PrimaryIndex<String, DatabaseAddress> returnAddress(){
		return address;
	}

	// return user info
	public PrimaryIndex<String, UserDetails> returnDetails(){
		return details;
	}
}
