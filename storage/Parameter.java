package edu.upenn.cis455.storage;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import java.text.*;
import com.sleepycat.persist.model.Persistent;

@Persistent
public class Parameter implements Delayed {
//	@SuppressWarnings("unused")
	public String address;
	public long begin;
	public AddressInformation addressInformation;
	public String contentType;
	public long contentLength;
	public String position;
	public String Body;
	public String otherReply;
	public String recentChange = "";
	public int verifyOther = 0;
	public long slowTime = 0;
	public int incomingType = 0;

	@SuppressWarnings("unused")
	private Parameter(){

	}

	public Parameter(AddressInformation addressInformation, String address, long slowTime){
		this.addressInformation = addressInformation;
		this.address = address;
		this.slowTime = slowTime;
		this.begin = System.currentTimeMillis() + slowTime;
	}

	@Override
	public int compareTo(Delayed delayed){
		if(this.begin < ((Parameter) delayed).begin){
			return -1;
		}
		if(this.begin > ((Parameter) delayed).begin){
			return 1;
		}
		return 0;
	}

	public void dispatchHeader(){
		Socket client = null;
		HttpsURLConnection httpCon = null;
		StringBuilder frame = new StringBuilder();
		BufferedReader br = null;

		frame.append("HEAD " + addressInformation.fetchDirectory());
		frame.append(" HTTP/1.1\r\n" + "Host: " + addressInformation.fetchNode());
		frame.append("\r\nUserAgent: cis455crawler\r\n");
		if(verifyOther == 1){
			frame.append("If-Modified-Since: ");
			frame.append(recentChange);
			frame.append("\r\n");
		}
		frame.append("Connection: close\r\n");
		frame.append("\r\n");

		try{
			client = new Socket(addressInformation.fetchNode(), addressInformation.fetchPortNum());
			DataOutputStream dos = null;
			InputStreamReader isr;
			if(addressInformation.fetchIsHttps() == 0){
				try{
					client = new Socket(addressInformation.fetchNode(), addressInformation.fetchPortNum());
					dos = new DataOutputStream(client.getOutputStream());
					isr = new InputStreamReader(client.getInputStream());
					br = new BufferedReader(isr);
				} catch(UnknownHostException e) {
					System.out.println("Unable to connect to host. ERROR: " + e);
				} catch(IOException e){
					System.out.println("ERROR: " + e);
				}
			} else if(addressInformation.fetchIsHttps() == 1){
				URL url;
				try{
					StringBuilder incomingAddress = new StringBuilder();
					incomingAddress.append("https://");
					incomingAddress.append(addressInformation.fetchNode());
					incomingAddress.append(":");
					incomingAddress.append(String.valueOf(addressInformation.fetchPortNum()));
					incomingAddress.append(addressInformation.fetchDirectory());
					url = new URL(incomingAddress.toString());
					URLConnection addressCon = null;
					addressCon =  url.openConnection();
					httpCon = (HttpsURLConnection) addressCon;
					dos = new DataOutputStream(httpCon.getOutputStream());
					isr = new InputStreamReader(httpCon.getInputStream());
					br = new BufferedReader(isr);
				} catch(IOException e){
					System.out.println("ERROR: " + e);
				}
			} else return;
			dos.write(frame.toString().getBytes());
			dos.flush();
		} catch(UnknownHostException e){
			System.out.println("Unable to connect to host. ERROR: " + e);
		} catch(IOException e){
			System.out.println("ERROR: " + e);
		}

		String data = "";
		StringBuilder outgoing = new StringBuilder();
		String[] outgoingDivided;
		try{
			while((data = br.readLine()) != null){
				if(data.equals("")){
					break;
				}
				outgoing.append(data);
				outgoing.append("\n");
			}
		} catch(IOException e){
			System.out.println("ERROR: " + e);
		} finally {
			if(addressInformation.fetchIsHttps() == 0){
				try{
					client.close();
				} catch(IOException e){
					System.out.println("ERROR: " + e);
				}
			} else if(addressInformation.fetchIsHttps() == 1){
				httpCon.disconnect();
			}
		}
		otherReply = outgoing.toString();
		outgoingDivided = outgoing.toString().split("\n");
		for(int temp = 0; temp < outgoingDivided.length; temp++){
			try{
				if(outgoingDivided[temp].trim().toLowerCase().startsWith("content-length")){
					String[] dataSize = outgoingDivided[temp].split(":", 2);
					contentLength = Long.parseLong(dataSize[1].trim());
				} else if(outgoingDivided[temp].trim().toLowerCase().startsWith("content-type")){
					String[] dataKind = outgoingDivided[temp].split(":", 2);
					contentType = dataKind[1].trim();
				} else if(outgoingDivided[temp].trim().toLowerCase().startsWith("last-modified")){
					try{
						String[] recChange = outgoingDivided[temp].split(":", 2);
						recentChange = recChange[1].trim();
					}catch(Exception e){
						System.out.println("ERROR: " + e);
					}
				} else if (temp == 0){
					position = outgoingDivided[0].trim();
				}
			} catch(Exception e){
				System.out.println("ERROR: " + e);
			}
		}
	}

	public void updateBegin(long l){
		begin = l;
	}
	
	public void dispatchGet(){
		Socket client = null;
		HttpsURLConnection httpCon = null;
		StringBuilder frame = new StringBuilder();
		BufferedReader br = null;
		StringBuilder content = new StringBuilder();

		frame.append("GET " + addressInformation.fetchDirectory());
		frame.append(" HTTP/1.1\r\n" + "Host: " + addressInformation.fetchNode());
		frame.append("\r\nUserAgent: cis455crawler\r\n" + "Connection: close\r\n\r\n");

		try{
			client = new Socket(addressInformation.fetchNode(), addressInformation.fetchPortNum());
			DataOutputStream dos = null;
			InputStreamReader isr;
			if(addressInformation.fetchIsHttps() == 0){
				try{
					client = new Socket(addressInformation.fetchNode(), addressInformation.fetchPortNum());
					dos = new DataOutputStream(client.getOutputStream());
					isr = new InputStreamReader(client.getInputStream());
					br = new BufferedReader(isr);
				} catch(UnknownHostException e) {
					System.out.println("Unable to connect to host. ERROR: " + e);
				} catch(IOException e){
					System.out.println("ERROR: " + e);
				}
			} else if(addressInformation.fetchIsHttps() == 1){
				URL url;
				try{
					StringBuilder incomingAddress = new StringBuilder();
					incomingAddress.append("https://");
					incomingAddress.append(addressInformation.fetchNode());
					incomingAddress.append(":");
					incomingAddress.append(String.valueOf(addressInformation.fetchPortNum()));
					incomingAddress.append(addressInformation.fetchDirectory());
					url = new URL(incomingAddress.toString());
					URLConnection addressCon = null;
					addressCon =  url.openConnection();
					httpCon = (HttpsURLConnection) addressCon;
					dos = new DataOutputStream(httpCon.getOutputStream());
					isr = new InputStreamReader(httpCon.getInputStream());
					br = new BufferedReader(isr);
				} catch(IOException e){
					System.out.println("ERROR: " + e);
				}
			} else return;
			dos.write(frame.toString().getBytes());
			dos.flush();
		} catch(UnknownHostException e){
			System.out.println("Unable to connect to host. ERROR: " + e);
		} catch(IOException e){
			System.out.println("ERROR: " + e);
		}

		String data = "";
//		int dataLen = 0;
		StringBuilder outgoing = new StringBuilder();
		String[] outgoingDivided;
		try{
			while((data = br.readLine()) != null){
				if(data.equals("")){
					break;
				}
				outgoing.append(data);
				outgoing.append("\n");
			}
			outgoingDivided = outgoing.toString().split("\n");
			for(int temp = 0; temp < outgoingDivided.length; temp++){
				if(outgoingDivided[temp].trim().toLowerCase().startsWith("content-length")){
					String[] dataSize = outgoingDivided[temp].split(":", 2);
					try{
						contentLength = Integer.parseInt(dataSize[1].trim());
					} catch(Exception e){
						// System.out.println("ERROR: " + e);
					}
					if(contentLength > 0){
						char input;
						while((input = (char)br.read()) != -1){
							content.append((char) input);
							if(content.length() == contentLength){
								break;
							}
						}
					}
				}
			}
			Body = content.toString();
		} catch(IOException e){
			// System.out.println("ERROR: " + e);
		} finally {
			if(addressInformation.fetchIsHttps() == 0){
				try{
					client.close();
				} catch(IOException e){
					System.out.println("ERROR: " + e);
				}
			} else if(addressInformation.fetchIsHttps() == 1){
				httpCon.disconnect();
			}
		}	
	}

	public String fetchDate(){
		Date date = new Date();
		DateFormat dateFormat = null;
		dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(date);
	}
	
//	@Override
	public long getDelay(TimeUnit unit) {
		// TODO Auto-generated method stub
		long offset = begin - System.currentTimeMillis();
		return unit.convert(offset, TimeUnit.MILLISECONDS);
	}
	

}
