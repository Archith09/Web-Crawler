package edu.upenn.cis455.servlet;

import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Register extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) 
			throws ServletException, IOException
	{
		PrintWriter pw = null;
		try{
			pw = res.getWriter();
			pw.println("<html><head><title>Login</title></head><body><form action=\"/confirmRegistration\" method=\"post\">");
			pw.println("<div class=\"container\" style=\"background-color:#C0C0C0\"<label><b>User Name</b></label>");
			pw.println("<input type=\"text\" placeholder=\"Please enter username\" name=\"user\" required><label><b>Password</b></label>");
			pw.println("<input type=\"password\" placeholder=\"Please enter password\" name=\"pass\" required><button type=\"submit\">Submit</button></form></div></body></html>");
		} catch(IOException e){
			// System.out.println("ERROR: " + e);
		}
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res){
		
	}
}
