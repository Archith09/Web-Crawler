package edu.upenn.cis455.storage;

import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.StoreConfig;
import java.io.*;

public class DBWrapper {
	
//	private static String envDirectory = null;
	
	private static Environment myEnv;
	private static EntityStore store;
	
	/* TODO: write object store wrapper for BerkeleyDB */
	public static Service service;
	private static boolean yes = true;
	File content;

	// constructor for DBWrapper class
	public DBWrapper(File content){
		this.content = content;
		DBSetup();
	}

	// method to setup database
	public void DBSetup(){
		try{
			EnvironmentConfig myDBConfig = new EnvironmentConfig();
			myDBConfig.setAllowCreate(yes);
			myDBConfig.setTransactional(yes);
			myEnv = new Environment(content, myDBConfig);
			StoreConfig myStoreConfig = new StoreConfig();
			myStoreConfig.setAllowCreate(yes);
			myStoreConfig.setTransactional(yes);
			store = new EntityStore(myEnv, "EntityStore", myStoreConfig);
			service = new Service(store);
		} catch(DatabaseException e){
			System.err.println("Database ERROR: " + e.toString());
		}
	}

	// method to close entity store and database environment
	public void close(){
		if(store == null){

		} else {
			try{
				store.close();
			} catch(DatabaseException e){
				System.err.println("Unable to close store. ERROR: " + e.toString());
				System.exit(-1);
			}
		}
		if(myEnv == null){

		} else {
			try{
				myEnv.close();
			} catch(DatabaseException e){
				System.err.println("Unable to close Database Environment. ERROR: " + e.toString());
				System.exit(-1);
			}
		}
	}
}
