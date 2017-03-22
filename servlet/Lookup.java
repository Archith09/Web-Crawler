package edu.upenn.cis455.servlet;

import java.io.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.DatabaseAddress;
import edu.upenn.cis455.storage.Parameter;
import edu.upenn.cis455.storage.SaveData;

public class Lookup extends HttpServlet{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res){
		
		PrintWriter pw = null;
		String DBPath = getServletContext().getInitParameter("BDBstore");
		String htmlContent = "<html><body><h3>Page Not Found.</h3></body></html>";
		File content = new File(DBPath);
		if(!content.exists()){
			content.mkdir();
			content.setWritable(true);
			content.setReadable(true);
		}
		DBWrapper database = new DBWrapper(content);
		try {
			if(req.getParameterMap() != null){
				String destinationAddress = req.getParameter("url");
				pw = res.getWriter();
				if(database.service.returnAddress().contains(destinationAddress)){
					DatabaseAddress databaseAddress = database.service.returnAddress().get(destinationAddress);
					String crc = databaseAddress.fetchCRC();
					if(database.service.returnPrimaryIndex().contains(crc)){
						SaveData saveData = database.service.returnPrimaryIndex().get(crc);
						Parameter parameter = saveData.fetchParameter();
						res.setContentType(parameter.contentType);
						String data = parameter.Body;
						pw.println(data);
						pw.flush();
					} else {
						pw.println(htmlContent);
					}
				} else {
					pw.println(htmlContent);
				}
			} else {
				pw.println(htmlContent);
			}
		} catch(Exception e){
			// System.out.println("ERROR: " + e);
			pw.println(htmlContent);
		}
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res){

	}
}
