package edu.upenn.cis455.servlet;

// import javax.servlet.http.*;
import java.io.*;
import javax.servlet.http.*;
//import javax.servlet.Servlet;
//import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {
	
	/* TODO: Implement user interface for XPath engine here */
	
	/* You may want to override one or both of the following methods */

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
    {
		/* TODO: Implement user interface for XPath engine here */
	}

	// Web Interface to take input from users
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException
	{
		/* TODO: Implement user interface for XPath engine here */
		PrintWriter pw = null;
		try{
			pw = response.getWriter();
			pw.println("<html><head><title>Login</title></head><body><form action=\"/login\" method=\"post\">");
			pw.println("<div class=\"container\" style=\"background-color:#C0C0C0\"<label><b>User Name</b></label>");
			pw.println("<input type=\"text\" placeholder=\"Please enter username:\" name=\"user\" required><label><b>Password</b></label>");
			pw.println("<input type=\"password\" placeholder=\"Please enter password\" name=\"pass\" required><button type=\"submit\">Login</button></form>");
			pw.println("<a href=\"/register\"><span><input type=\"button\" value=\"Register\"></span></a></div></body></html>");
		} catch(IOException e){
			// System.out.println("ERROR: " + e);
		}
	}

}
