package edu.upenn.cis455.crawler;

import java.io.*;
//import com.sleepycat.je.*;
//import com.sleepycat.persist.*;
import edu.upenn.cis455.storage.*;

public class XPathCrawler {

	public static String databaseLocation;
	public static long fileSize;
	public static int limit;
	public static boolean yes = true;

	public static void main(String args[]) {
		/* TODO: Implement crawler */
		String address = args[0];
		databaseLocation = args[1];
		fileSize = Integer.parseInt(args[2]);

		// creating a directory to store data
		File data = new File(databaseLocation);
		if(!data.exists()){
			data.mkdir();
			data.setReadable(yes);
			data.setWritable(yes);
		}

		// check if valid number of arguments are passed
		if(args.length < 3){
			System.out.println("Name: Archith Shivanagere Muralinath\n" + "PennKey: archith\n");
			return;
		}

		// set database environment
//		System.out.println(data.isDirectory() + "..." + data.getPath());
		DBWrapper database = new DBWrapper(data);
		System.out.println("Archith Shivangere Muralinath's Crawler Started. Please wait few seconds to start downloading.");
		@SuppressWarnings("unused")
		CrawlerFrontier crawlerFrontier = new CrawlerFrontier(database, address);
		return;
	}

}
