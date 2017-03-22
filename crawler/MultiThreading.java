package edu.upenn.cis455.crawler;

import java.util.*;
//import java.lang.*;

public class MultiThreading {
	public static ArrayList<CrawlerClient> multiThreading = new ArrayList<CrawlerClient>();
	public int noOfThreads;

	MultiThreading(int noOfThreads){
		this.noOfThreads = noOfThreads;
		for(int thread = 0; thread < noOfThreads; thread++){
			multiThreading.add(new CrawlerClient());
		}

		// create requested number of threads
		for(int thread = 0; thread < noOfThreads; thread++){
			multiThreading.get(thread).start();
		}
	}
}
