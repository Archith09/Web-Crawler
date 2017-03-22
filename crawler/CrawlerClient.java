package edu.upenn.cis455.crawler;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.DelayQueue;
import java.security.*;
// import org.w3c.dom.Document;
import org.w3c.dom.*;
import org.w3c.tidy.Tidy;
import edu.upenn.cis455.storage.*;

public class CrawlerClient extends Thread {

	Parameter parameter;
	HashSet<String> nodes = new HashSet<String>();
	DelayQueue<Parameter> urlList = new DelayQueue<Parameter>();

	public void run(){
		while(CrawlerFrontier.terminateApp == 0){
			try{
				String crc = "";
				int size = 1024;
				parameter = urlList.take();
				if(DBWrapper.service.returnAddress().contains(parameter.address)){
					parameter.incomingType = 0;
					parameter.verifyOther = 1;
				}
				nodes.add(parameter.addressInformation.fetchNode());
				if(parameter.incomingType == 0){
					if(parameter.verifyOther == 1){
						try{
							parameter.recentChange = CrawlerFrontier.database.service.returnAddress().get(parameter.address).fetchRecentChange();
						} catch(NullPointerException e){
							// System.out.println("ERROR: " + e);
						}
					}
					parameter.dispatchHeader();
					if(parameter.verifyOther == 1){
						if(parameter.position.startsWith("HTTP/1.1 304")){
							System.out.println(parameter.address + " ------> Not Modified");
							continue;
						} else if(parameter.position.startsWith("HTTP/1.1 200") &&
							(parameter.contentType.endsWith("xml") ||
								parameter.contentType.endsWith("html")) &&
							parameter.contentLength < XPathCrawler.fileSize*size*size){
							synchronized(CrawlerFrontier.slowTimeTable){
								if(CrawlerFrontier.slowTimeTable.get(parameter.addressInformation.fetchNode()) > System.currentTimeMillis()){
									parameter.updateBegin(CrawlerFrontier.slowTimeTable.get(parameter.addressInformation.fetchNode()) + (parameter.slowTime * 1000));
									parameter.incomingType = 1;
									CrawlerFrontier.slowTimeTable.put(parameter.addressInformation.fetchNode(), CrawlerFrontier.slowTimeTable.get(parameter.addressInformation.fetchNode()) + (parameter.slowTime * 1000));
									urlList.put(parameter);
								} else {
									parameter.updateBegin((parameter.slowTime * 1000) + System.currentTimeMillis());
									parameter.incomingType = 1;
									CrawlerFrontier.slowTimeTable.put(parameter.addressInformation.fetchNode(), System.currentTimeMillis());
									urlList.put(parameter);
								}
							}
						}
					} else if(parameter.position.startsWith("HTTP/1.1 200") &&
						(parameter.contentType.endsWith("xml") ||
							parameter.contentType.endsWith("html")) &&
						parameter.contentLength < XPathCrawler.fileSize*size*size){
						synchronized(CrawlerFrontier.slowTimeTable){
							if(CrawlerFrontier.slowTimeTable.get(parameter.addressInformation.fetchNode()) > System.currentTimeMillis()){
								parameter.updateBegin(CrawlerFrontier.slowTimeTable.get(parameter.addressInformation.fetchNode()));
								parameter.incomingType = 1;
								CrawlerFrontier.slowTimeTable.put(parameter.addressInformation.fetchNode(), CrawlerFrontier.slowTimeTable.get(parameter.addressInformation.fetchNode()) + (parameter.slowTime * 1000));
								urlList.put(parameter);
							} else {
								parameter.updateBegin(System.currentTimeMillis());
								parameter.incomingType = 1;
								CrawlerFrontier.slowTimeTable.put(parameter.addressInformation.fetchNode(), System.currentTimeMillis() + (parameter.slowTime * 1000));
								urlList.put(parameter);
							}
						}
					} else if(parameter.position.startsWith("HTTP/1.1 301") || parameter.position.startsWith("HTTP/1.1 302")){
						System.out.println(parameter.address + " ------> Unable to download. Redirecting.");
						String[] decodePath = parameter.otherReply.split("\n");
						for(int index = 0; index < decodePath.length; index++){
							if(decodePath[index].trim().startsWith("Location")){
								String[] path = decodePath[index].split(":", 2);
								if(path[1].trim().startsWith("http://") || path[1].trim().startsWith("https://")){
									CrawlerFrontier.taskList.put(path[1].trim());
								}else{
									StringBuilder createAddress = new StringBuilder("http://");
									createAddress.append(parameter.addressInformation.fetchNode());
									createAddress.append(parameter.addressInformation.fetchDirectory());
									createAddress.append(path[1]);
									CrawlerFrontier.taskList.put(createAddress.toString().trim());
								}
							}
						}
						if(!parameter.recentChange.equals("")){
							DatabaseAddress databaseAddress = new DatabaseAddress();
							databaseAddress.updatePrimaryKey(parameter.address);
							databaseAddress.updateRecentChange(parameter.recentChange);
							databaseAddress.updateCRC(crc);
							CrawlerFrontier.database.service.returnAddress().put(databaseAddress);
						} else {
							Date date = new Date();
							DateFormat dateFormat = null;
							dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
							dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
							String recChange = dateFormat.format(date);
							DatabaseAddress databaseAddress = new DatabaseAddress();
							databaseAddress.updatePrimaryKey(parameter.address);
							databaseAddress.updateRecentChange(parameter.recentChange);
							databaseAddress.updateCRC(crc);
							CrawlerFrontier.database.service.returnAddress().put(databaseAddress);
						}
					} else if(parameter.position.startsWith("HTTP/1.1 404")){
						continue;
					}
				} else if(parameter.incomingType == 1){
					if(parameter.position.startsWith("HTTP/1.1 200") &&
						(parameter.contentType.endsWith("xml") ||
						parameter.contentType.endsWith("html")) &&
						parameter.contentLength < XPathCrawler.fileSize * size * size){
						
						System.out.println(parameter.address + "  ------> Downloading");
						parameter.dispatchGet();
						crc = evaluateCRC(parameter.Body);
						if(!DBWrapper.service.returnPrimaryIndex().contains(crc)){
							SaveData saveData = new SaveData();
							saveData.updatePrimaryKey(crc);
							saveData.updateParameter(parameter);
							DBWrapper.service.returnPrimaryIndex().put(saveData);
							ByteArrayInputStream bais = new ByteArrayInputStream(parameter.Body.getBytes());
							if(parameter.contentType.endsWith("html")){
								analyzeHTML(bais);
							}
						}  else {
							System.out.println(parameter.address + "  ------> Content seen");
							continue;
						}
					} else {
							System.out.println(parameter.address + " ------> Unable to download");
					}

					if(!parameter.recentChange.equals("")){
						DatabaseAddress databaseAddress = new DatabaseAddress();
						databaseAddress.updatePrimaryKey(parameter.address);
						databaseAddress.updateRecentChange(parameter.recentChange);
						databaseAddress.updateCRC(crc);
						CrawlerFrontier.database.service.returnAddress().put(databaseAddress);
					} else {
						Date date = new Date();
						DateFormat dateFormat = null;
						dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
						dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
						String recChange = dateFormat.format(date);
						DatabaseAddress databaseAddress = new DatabaseAddress();
						databaseAddress.updatePrimaryKey(parameter.address);
						databaseAddress.updateRecentChange(parameter.recentChange);
						databaseAddress.updateCRC(crc);
						CrawlerFrontier.database.service.returnAddress().put(databaseAddress);
					}
				} 
			} catch(InterruptedException e){
					// System.out.println("ERROR: " + e);
			}
		}
	}

	public void analyzeHTML(ByteArrayInputStream bais){
		
		Tidy parser = new Tidy();
		parser.setQuiet(true);
		parser.setShowWarnings(false);
		Document document = null;
		document = (Document)parser.parseDOM(bais, null);
		NodeList nodeList = document.getElementsByTagName("a");
		for(int temp = 0; temp < nodeList.getLength(); temp++){
			String newValue = null;
			final org.w3c.dom.Node node = nodeList.item(temp);
			NamedNodeMap properties = node.getAttributes();
			Node newUrl = properties.getNamedItem("href");

			if(newUrl != null){
				newValue = newUrl.getNodeValue();
			}

			try{
				if(newValue.startsWith("http://") || newValue.startsWith("https://")){
					CrawlerFrontier.taskList.put(newValue.trim());
				} else {
					StringBuilder createAddress = new StringBuilder("http://");
					createAddress.append(parameter.addressInformation.fetchNode());
					createAddress.append(parameter.addressInformation.fetchDirectory());
					createAddress.append(newValue);
					CrawlerFrontier.taskList.put(createAddress.toString().trim());
				}
			} catch(InterruptedException e){
				// System.out.println("ERROR: " + e);
			}
		}
	}

	public String evaluateCRC(String data){
		
		StringBuffer stringBuffer = new StringBuffer();
		StringBuffer converted = new StringBuffer();		
		
		int hex = 16;
		int starting = 1;
		
		MessageDigest messageDigest = null;

		try{
			messageDigest = MessageDigest.getInstance("SHA-256");
		} catch(NoSuchAlgorithmException e){
			System.out.println("ERROR: " + e);
		}
		messageDigest.update(data.getBytes());
		byte previous[] = messageDigest.digest();
		for(int len = 0; len < previous.length; len++){
			stringBuffer.append(Integer.toString((previous[len] & 0xff) + 0x100, hex).substring(starting));
		}

		for(int len = 0; len < previous.length; len++){
			String convertedString = Integer.toHexString(0xff & previous[len]);
			if(convertedString.length() == starting){
				converted.append('0');
			}
			converted.append(convertedString);
		}
		return converted.toString();
	}

	public CrawlerClient(String address) {
		// TODO Auto-generated constructor stub
	}

	public CrawlerClient() {
		// TODO Auto-generated constructor stub
	}

	public Document convertToDOM(String address) {
		// TODO Auto-generated method stub
		return null;
	}

//	public void interrupt() {
//		// TODO Auto-generated method stub
//		
//	}

}
