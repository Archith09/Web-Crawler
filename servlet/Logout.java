package edu.upenn.cis455.servlet;

import java.io.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Logout extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res){
		PrintWriter pw = null;
		try{
			pw = res.getWriter();
			pw.println("<html><body><p>Logout Successful!</p><body></html>");
			res.sendRedirect("/xpath");
		} catch(Exception e){
			// System.out.println("ERROR: " + e);
		}
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res){

	}
}
