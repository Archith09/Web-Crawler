package edu.upenn.cis455.crawler;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.HttpsURLConnection;
import edu.upenn.cis455.storage.*;
import edu.upenn.cis455.crawler.CloseApplication;
import edu.upenn.cis455.crawler.info.RobotsTxtInfo;

public class CrawlerFrontier {

	public static HashMap<String, Long> slowTimeTable;
	public HashMap<String, Integer> nodeId = new HashMap<String, Integer>();
	MultiThreading multiThreading;
	public RobotsTxtInfo robotsTxtInfo = new RobotsTxtInfo();
	public static InbuiltTempClass inbuiltTempClass;
	public static LinkedBlockingQueue<String> taskList;
	String ua;
	String beginAddress;
	public static int terminateApp = 0;
	public static DBWrapper database;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public CrawlerFrontier(DBWrapper database, String address) {
		// TODO Auto-generated constructor stub
		CrawlerFrontier.database = database;
		this.beginAddress = address;
		taskList = new LinkedBlockingQueue<String>();
		ua = "cis455crawler";
		multiThreading = new MultiThreading(9);
		slowTimeTable = new HashMap();
		startFrontier();
	}

	public void startFrontier(){
		AddressInformation addressInformation = new AddressInformation(beginAddress);
		int status = 0;
		long slowTime = 0;
		StringBuilder content = fetchRobots(addressInformation);
		ArrayList<String> approve = new ArrayList<String>();
		ArrayList<String> location = new ArrayList<String>();
		String[] contentList = content.toString().split("\n");
		ArrayList<String> ignore = new ArrayList<String>();

		for(int len = 0; len < contentList.length; len++){
			try{
				if(contentList[len].toLowerCase().trim().startsWith("user-agent")){
					String[] validateName = contentList[len].split(":", 2);
					try{
						if(validateName[1].trim().equals(ua)){
							int temp = len + 1;
							status = 1;
							while(!contentList[temp].equals("")){
								try{
									if(contentList[temp].trim().startsWith("Disallow")){
										String[] ignoreAddress = contentList[temp].split(":", 2);
										robotsTxtInfo.addDisallowedLink(addressInformation.fetchNode(), ignoreAddress[1].trim());
									} else if(contentList[temp].trim().startsWith("Allow")){
										String[] approveAddress = contentList[temp].split(":", 2);
										robotsTxtInfo.addAllowedLink(addressInformation.fetchNode(), approveAddress[1].trim());
									} else if(contentList[temp].trim().startsWith("Crawl-delay")){
										String[] fetchSlow = contentList[temp].split(":");
										slowTime = Integer.parseInt(fetchSlow[1].trim());
										robotsTxtInfo.addCrawlDelay(addressInformation.fetchNode(), Integer.parseInt(fetchSlow[1].trim()));
									} else if(contentList[temp].trim().startsWith("Sitemap")){
										String[] locationAddress = contentList[temp].split(":", 2);
										robotsTxtInfo.addSitemapLink(locationAddress[1]);
									}
									temp++;
									if(temp >= contentList.length){
										break;
									}
								} catch(Exception e){
									// System.out.println("ERROR: " + e);
								}
							}
						} else if(validateName[1].trim().equals("*") && status == 0){
							int temp = len + 1;
							// status = 1;
							while(!contentList[temp].equals("")){
								try{
									if(contentList[temp].trim().startsWith("Disallow")){
										String[] ignoreAddress = contentList[temp].split(":", 2);
										ignore.add(ignoreAddress[1].trim());
									} else if(contentList[temp].trim().startsWith("Allow")){
										String[] approveAddress = contentList[temp].split(":", 2);
										approve.add(approveAddress[1].trim());
									} else if(contentList[temp].trim().startsWith("Crawl-delay")){
										String[] fetchSlow = contentList[temp].split(":");
										slowTime = Integer.parseInt(fetchSlow[1].trim());
										robotsTxtInfo.addCrawlDelay(addressInformation.fetchNode(), Integer.parseInt(fetchSlow[1].trim()));
									} else if(contentList[temp].trim().startsWith("Sitemap")){
										String[] locationAddress = contentList[temp].split(":", 2);
										location.add(locationAddress[1]);
									}
									temp++;
									if(temp >= contentList.length){
										break;
									}
								} catch(Exception e){
									// System.out.println("ERROR: " + e);
								}
							}
						}
					} catch(Exception e){
						// System.out.println("ERROR: " + e);
					}
				}
			} catch(Exception e){
				// System.out.println("ERROR: " + e);
			}
		}

		if(status == 0){
			for(int size = 0; size < ignore.size(); size++){
				robotsTxtInfo.addDisallowedLink(addressInformation.fetchNode(), ignore.get(size));
			}
			for(int size = 0; size < approve.size(); size++){
				robotsTxtInfo.addAllowedLink(addressInformation.fetchNode(), approve.get(size));
			}
			for(int size = 0; size < location.size(); size++){
				robotsTxtInfo.addSitemapLink(addressInformation.fetchNode());
			}
		}

		Parameter parameter = new Parameter(addressInformation, beginAddress, slowTime);
//		DatabaseAddress databaseAddress = new DatabaseAddress();
		if(DBWrapper.service.returnAddress().contains(beginAddress)){
			parameter.verifyOther = 1;
		}
		MultiThreading.multiThreading.get(0).urlList.put(parameter);
		long present = System.currentTimeMillis();
		parameter.updateBegin(present + (parameter.slowTime * 1000));
		synchronized(slowTimeTable){
			slowTimeTable.put(addressInformation.fetchNode(), present + (parameter.slowTime * 1000 * 2));
		}
		nodeId.put(addressInformation.fetchNode(), 0);
		inbuiltTempClass = new InbuiltTempClass();
		inbuiltTempClass.start();
		CloseApplication closeApplication = new CloseApplication();
		closeApplication.start();
	}
	
	public static void terminateWorker(){
		inbuiltTempClass.interrupt();
	}

	public StringBuilder fetchRobots(AddressInformation addressInformation){
		Socket client = null;
		StringBuilder content = new StringBuilder();
		HttpsURLConnection link = null;
		InputStreamReader isr;
		DataOutputStream dos = null;
		BufferedReader br = null;

		content.append("GET /robots.txt HTTP/1.1\r\n" + "Host: " + addressInformation.fetchNode() + "\r\n");
		content.append("User-Agent: cis455crawler\r\n" + "Connection: close\r\n\r\n");

		if(addressInformation.fetchIsHttps() == 0){
			try{
				client = new Socket(addressInformation.fetchNode(), addressInformation.fetchPortNum());
				dos = new DataOutputStream(client.getOutputStream());
				isr = new InputStreamReader(client.getInputStream());
				br = new BufferedReader(isr);
			} catch(UnknownHostException e){
				System.out.println("ERROR: " + e);
			} catch(IOException e){
				System.out.println("ERROR: " + e);
			}
		} else if(addressInformation.fetchIsHttps() == 1){
			URLConnection addressLink = null;
			URL address;
			try{
				address = new URL(addressInformation.fetchNode());
				addressLink = address.openConnection();
				link = (HttpsURLConnection) addressLink;
				dos = new DataOutputStream(link.getOutputStream());
				isr = new InputStreamReader(link.getInputStream());
				br = new BufferedReader(isr);
			} catch(IOException e){
				System.out.println("ERROR: " + e);
			}
		} else {
			return null;
		}

		try{
			StringBuilder outgoing = new StringBuilder();
			StringBuilder data = new StringBuilder();
			String[] outgoingSep;
			String read = "";
			int dataSize = 0;
			
			dos.write(content.toString().getBytes());
			dos.flush();

			while((read = br.readLine()) != null){
				if(read.equals("")){
					break;
				}
				outgoing.append(read + "\n");
			}
			outgoingSep = outgoing.toString().split("\n");
			for(int len = 0; len < outgoingSep.length; len++){
				if(outgoingSep[len].trim().toLowerCase().startsWith("content-length")){
					String[] part = outgoingSep[len].split(":", 2);
					dataSize = Integer.parseInt(part[1].trim());
					if(dataSize > 0){
						char input;
						while((input = (char)br.read()) != -1){
							data.append((char) input);
							if(data.length() == dataSize){
								break;
							}
						}
					}
				}
			}
			return data;
		} catch(Exception e){
			return null;
		} finally {
			if(addressInformation.fetchIsHttps() == 0){
				try{
					client.close();
				} catch(IOException e){
					System.out.println("ERROR: " + e);
				}
			} else if(addressInformation.fetchIsHttps() == 1){
				link.disconnect();
			}
		}
	}

	class InbuiltTempClass extends Thread{
		
		public void run(){
			while(terminateApp == 0){
				try{
					String curAddress = taskList.take();
					int ignoreStatus = 0;
					AddressInformation addressInformation = new AddressInformation(curAddress);
					ArrayList<String> ignoreUrls = null;
					if(nodeId.containsKey(addressInformation.fetchNode())){
						int workerId = nodeId.get(addressInformation.fetchNode());
						if(!DBWrapper.service.returnAddress().contains(curAddress)){
							ignoreUrls = robotsTxtInfo.getDisallowedLinks(addressInformation.fetchNode());
							if(ignoreUrls != null){
								for(int size = 0; size < ignoreUrls.size(); size++){
									if(addressInformation.fetchDirectory().startsWith(ignoreUrls.get(size).trim())){
										ignoreStatus = 1;
										break;
									}
								}
							}
							if(ignoreStatus == 1){
								System.out.println(curAddress + " ------> Not authorized to download.");
								continue;
							} else {
								long now = System.currentTimeMillis();
								long slow = robotsTxtInfo.getCrawlDelay(addressInformation.fetchNode());
								Parameter parameter = new Parameter(addressInformation, curAddress, slow);
								parameter.updateBegin(now + slow);
								synchronized(slowTimeTable){
									if(CrawlerFrontier.slowTimeTable.get(parameter.addressInformation.fetchNode()) > System.currentTimeMillis()){
										parameter.updateBegin(CrawlerFrontier.slowTimeTable.get(parameter.addressInformation.fetchNode()));
										CrawlerFrontier.slowTimeTable.put(parameter.addressInformation.fetchNode(), CrawlerFrontier.slowTimeTable.get(parameter.addressInformation.fetchNode()) + (parameter.slowTime * 1000));
									} else {
										parameter.updateBegin(System.currentTimeMillis());
										CrawlerFrontier.slowTimeTable.put(parameter.addressInformation.fetchNode(), System.currentTimeMillis() + (parameter.slowTime * 1000));
									}
								}
								synchronized(MultiThreading.multiThreading){
									MultiThreading.multiThreading.get(workerId).urlList.put(parameter);
								}
							}
						} else {
							int fetchSlow = robotsTxtInfo.getCrawlDelay(addressInformation.fetchNode());
							Parameter parameter = new Parameter(addressInformation, curAddress, fetchSlow);
							parameter.verifyOther = 1;
							synchronized(slowTimeTable){
								if(CrawlerFrontier.slowTimeTable.get(parameter.addressInformation.fetchNode()) > System.currentTimeMillis()){
									parameter.updateBegin(CrawlerFrontier.slowTimeTable.get(parameter.addressInformation.fetchNode()));
									CrawlerFrontier.slowTimeTable.put(parameter.addressInformation.fetchNode(), CrawlerFrontier.slowTimeTable.get(parameter.addressInformation.fetchNode()) + (parameter.slowTime * 1000));
								} else {
									parameter.updateBegin(System.currentTimeMillis());
									CrawlerFrontier.slowTimeTable.put(parameter.addressInformation.fetchNode(), System.currentTimeMillis() + (parameter.slowTime * 1000));
								}
							}
							synchronized(MultiThreading.multiThreading){
								MultiThreading.multiThreading.get(workerId).urlList.put(parameter);
							}
						}
					} else {
						ArrayList<String> approve = new ArrayList<String>();
						StringBuilder unknownUrl = fetchRobots(addressInformation);
						ArrayList<String> location = new ArrayList<String>();
						long slowTime = 0;
						ArrayList<String> ignore = new ArrayList<String>();
						String[] unknownList = unknownUrl.toString().split("\n");
						int status = 0;
						for(int len = 0; len < unknownList.length; len++){
							try{
								if(unknownList[len].toLowerCase().trim().startsWith("user-agent")){
									status = 1;
									String[] validateName = unknownList[len].split(":", 2);
									if(validateName[1].trim().equals(ua)){
										int temp = len + 1;
										while(!unknownList[temp].equals("")){
											if(unknownList[temp].trim().startsWith("Disallow")){
												String[] ignoreAddress = unknownList[temp].split(":", 2);
												robotsTxtInfo.addDisallowedLink(addressInformation.fetchNode(), ignoreAddress[1].trim());
											} else if(unknownList[temp].trim().startsWith("Allow")){
												String[] approveAddress = unknownList[temp].split(":", 2);
												robotsTxtInfo.addAllowedLink(addressInformation.fetchNode(), approveAddress[1].trim());
											} else if(unknownList[temp].trim().startsWith("Crawl-delay")){
												String[] fetchSlow = unknownList[temp].split(":");
												robotsTxtInfo.addCrawlDelay(addressInformation.fetchNode(), Integer.parseInt(fetchSlow[1].trim()));
											} else if(unknownList[temp].trim().startsWith("Sitemap")){
												String[] locationAddress = unknownList[temp].split(":", 2);
												robotsTxtInfo.addSitemapLink(locationAddress[1]);
											}
											temp++;
											if(temp >= unknownList.length){
												break;
											}
										}
									}
								} else if(unknownList[1].trim().equals("*") && status == 0){
									int temp = len + 1;
									while(!unknownList[temp].equals("")){
										try{
											if(unknownList[temp].trim().startsWith("Disallow")){
												String[] ignoreAddress = unknownList[temp].split(":", 2);
												ignore.add(ignoreAddress[1].trim());
											} else if(unknownList[temp].trim().startsWith("Allow")){
												String[] approveAddress = unknownList[temp].split(":", 2);
												approve.add(approveAddress[1].trim());
											} else if(unknownList[temp].trim().startsWith("Crawl-delay")){
												String[] fetchSlow = unknownList[temp].split(":");
												slowTime = Integer.parseInt(fetchSlow[1].trim());	//check if this is correct
												robotsTxtInfo.addCrawlDelay(addressInformation.fetchNode(), Integer.parseInt(fetchSlow[1].trim()));
											} else if(unknownList[temp].trim().startsWith("Sitemap")){
												String[] locationAddress = unknownList[temp].split(":", 2);
												location.add(locationAddress[1]);
											}
											temp++;
											if(temp >= unknownList.length){
												break;
											}
										} catch(Exception e){
												System.out.println("ERROR: " + e);
										}
									}
								}
							} catch(Exception e){
								// System.out.println("ERROR: " + e);
							}
						}

						if(status == 0){
							for(int size = 0; size < ignore.size(); size++){
								robotsTxtInfo.addDisallowedLink(addressInformation.fetchNode(), ignore.get(size));
							}
							for(int size = 0; size < approve.size(); size++){
								robotsTxtInfo.addDisallowedLink(addressInformation.fetchNode(), approve.get(size));
							}
							for(int size = 0; size < ignore.size(); size++){
								robotsTxtInfo.addDisallowedLink(addressInformation.fetchNode(), location.get(size));
							}
						}

						ignoreUrls = robotsTxtInfo.getDisallowedLinks(addressInformation.fetchNode());
						if(ignoreUrls != null){
							for(int size = 0; size < ignoreUrls.size(); size++){
								if(addressInformation.fetchDirectory().startsWith(ignoreUrls.get(size).trim())){
									ignoreStatus = 1;
									break;
								}
							}
						}

						if(ignoreStatus == 1){
							System.out.println(curAddress + " : Not authorized to download.");
							continue;
						} else {
							long slow = robotsTxtInfo.getCrawlDelay(addressInformation.fetchNode());
							Parameter parameter = new Parameter(addressInformation, curAddress, slow);
							int workerId = freeWorker();
							synchronized(slowTimeTable){
								if(CrawlerFrontier.slowTimeTable.get(parameter.addressInformation.fetchNode()) > System.currentTimeMillis()){
									parameter.updateBegin(CrawlerFrontier.slowTimeTable.get(parameter.addressInformation.fetchNode()));
									CrawlerFrontier.slowTimeTable.put(parameter.addressInformation.fetchNode(), CrawlerFrontier.slowTimeTable.get(parameter.addressInformation.fetchNode()) + (parameter.slowTime * 1000));
								} else {
									parameter.updateBegin(System.currentTimeMillis());
									CrawlerFrontier.slowTimeTable.put(parameter.addressInformation.fetchNode(), System.currentTimeMillis() + (parameter.slowTime * 1000));
								}
							}
							synchronized(MultiThreading.multiThreading){
								MultiThreading.multiThreading.get(workerId).urlList.put(parameter);
							}
						}
					}
				} catch(InterruptedException e){
					// System.out.println("ERROR: " + e);
				}
			}
		}

		public int freeWorker(){
			int smallest = Integer.MAX_VALUE;
			int worker = 0;
			synchronized(MultiThreading.multiThreading){
				for(int thread = 0; thread < MultiThreading.multiThreading.size(); thread++){
					if(MultiThreading.multiThreading.get(thread).urlList.isEmpty()){
						worker = thread;
						break;
					}
					if(MultiThreading.multiThreading.get(thread).urlList.size() < smallest){
						worker = thread;
						smallest = MultiThreading.multiThreading.get(thread).urlList.size();
					}
				}
			}
			return worker;
		}
	}

}
