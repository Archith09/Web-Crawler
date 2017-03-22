package edu.upenn.cis455.servlet;

import java.io.*;
//import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.UserDetails;

public class ConfirmRegistration extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) 
			throws ServletException, IOException
	{

	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res){
		try{
			String DBPath = getServletContext().getInitParameter("BDBstore");
			File data = new File(DBPath);
			if(!data.exists()){
				data.mkdir();
				data.setWritable(true);
				data.setReadable(true);
			}
			DBWrapper database = new DBWrapper(data);
			PrintWriter pw = res.getWriter();
			BufferedReader br = req.getReader();
			@SuppressWarnings("unused")
			char character;
			StringBuilder content = new StringBuilder();
			String load;
			while((load = br.readLine()) != null)
				content.append(load);
			String[] contentSep = content.toString().split("&");
			String[] name = contentSep[0].split("=");
			String[] identification = contentSep[1].split("=");

//			boolean no = false;
//			boolean yes = true;

			if(database.service.returnDetails().contains(name[1].trim())){
						pw.println("<html><body><p>Username already exists. Kindly choose a different name.</p></body>");
						pw.println("<a href=\"/xpath\"><span><input type=\"button\" value=\"Return to main page\"></span></a></p></html>");
			} else {
				UserDetails userDetails = new UserDetails();
				userDetails.updateName(name[1].trim());
				userDetails.updateIdentification(identification[1].trim());
				database.service.returnDetails().put(userDetails);
				pw.println("<html><body><p>Registration Successful!</p></body>");
				pw.println("<a href=\"/xpath\"><span><input type=\"button\" value=\"Return to main page\"></span></a></p></html>");
			}
			database.close();
		} catch(IOException e){
			// System.out.println("ERROR: " + e);
		}
	}

}