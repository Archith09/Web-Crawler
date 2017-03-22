package edu.upenn.cis455.storage;

import com.sleepycat.persist.model.*;

@Persistent
public class AddressInformation {
	public int portNum;
	public String node;
	public String directory;
	public int isHttps = 0;

	// constructor for address information. retrieve host name and the directory where file exists
	public AddressInformation(String address){

		int temp = 0;
		int length = 8;
		
		if(address.equals("") || address == null){
			return;
		}
		address = address.trim();
		if((!(address.startsWith("http://") || address.startsWith("https://"))) || (address.length() < length)){
			return;
		}
		if(address.startsWith("http://")){
			isHttps = 0;
			address = address.substring(7);
		} else if(address.startsWith("https://")){
			isHttps = 1;
			address = address.substring(8);
		}

		while(temp < address.length()){
			char index = address.charAt(temp);
			if(index == '/'){
				break;
			}
			temp++;
		}

		String path = address.substring(0, temp);
		
		if(temp == address.length()){
			directory = "/";
		} else {
			directory = address.substring(temp);
		}

		if(path.equals("") || path.equals("/")){
			return;
		}
		if(path.indexOf(':') != -1){
			String[] partPath = path.split(":", 2);
			node = partPath[0].trim();
			try{
				portNum = Integer.parseInt(partPath[1].trim());
			} catch(NumberFormatException e){
				portNum = 80;
			}
		} else{
			node = path;
			portNum = 80;
		}
	}

	public AddressInformation(){

	}

	public AddressInformation(String node, String directory){
		this.node = node;
		this.directory = directory;
		this.portNum = 8080;
	}

	public AddressInformation(String node, int portNum, String directory){
		this.node = node;
		this.directory = directory;
		this.portNum = portNum;
	}

	public String fetchNode(){
		return node;
	}

	public void updateNode(String data){
		node = data;
	}

	public int fetchPortNum(){
		return portNum;
	}

	public void updatePortNum(int portNo){
		portNum = portNo;
	}

	public String fetchDirectory(){
		return directory;
	}

	public void updateDirectory(String dir){
		directory = dir;
	}

	public int fetchIsHttps(){
		return isHttps;
	}
}
