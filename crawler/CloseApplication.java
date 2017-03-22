package edu.upenn.cis455.crawler;

//import java.io.IOException;
//import java.util.*;
//import java.net.*;

public class CloseApplication extends Thread {
	public static boolean yes = true;
	public void run(){
		int sleepTime = 5000;
		while(yes){
			int status = 0;
			if(CrawlerFrontier.taskList.isEmpty()){
				synchronized(MultiThreading.multiThreading){
					for(int thread = 0; thread < MultiThreading.multiThreading.size(); thread++){
						if(!MultiThreading.multiThreading.get(thread).urlList.isEmpty()){
							status = 1;
							break;
						}
					}
				}
			} else {
				try {
					Thread.sleep(sleepTime);
					continue;
				} catch (InterruptedException e){
					System.out.println("ERROR: " + e);
				}
			}

			if(status == 0){
				try{
					Thread.sleep(sleepTime*2);
				} catch(InterruptedException e){
					System.out.println("ERROR: " + e);
				}
				status = 0;
				if(CrawlerFrontier.taskList.isEmpty()){
					for(int thread = 0; thread < MultiThreading.multiThreading.size(); thread++){
						if(!MultiThreading.multiThreading.get(thread).urlList.isEmpty()){
							status = 1;
							break;
						}
					}
				} else {
					continue;
				}

				if(status == 0){
					synchronized(MultiThreading.multiThreading){
						CrawlerFrontier.terminateApp = 1;
						for(int thread1 = 0; thread1 < MultiThreading.multiThreading.size(); thread1++){
							MultiThreading.multiThreading.get(thread1).interrupt();
						}
						CrawlerFrontier.terminateWorker();
						CrawlerFrontier.database.close();
						break;
					}
				}
			} else {
				try{
					Thread.sleep(sleepTime);
				} catch(InterruptedException e){
					// System.out.println("ERROR: " + e);
				}
			}
		}
	}
}
