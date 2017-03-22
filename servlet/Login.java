package edu.upenn.cis455.servlet;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import edu.upenn.cis455.storage.DBWrapper;

public class Login extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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

			boolean no = false;
			boolean yes = true;

			if(database.service.returnDetails().contains(name[1].trim())){
				if(database.service.returnDetails().get(name[1].trim()).fetchIdentification().equals(identification[1].trim())){
					if(req.getSession(no) == null){
						pw.println("<html><body><p>Login Successful.</p></body>Username: " + name[1].trim() + "</p>");
						pw.println("<a href=\"/logout\"><span><input type=\"button\" value=\"Logout\"></span></a></p></html>");
						HttpSession hs = req.getSession(yes);
						hs.setMaxInactiveInterval(120);
					} else {
						pw.println("<html><body><p>User already logged in!</p></body>Username: " + name[1].trim() + "</p>");
						pw.println("<a href=\"/logout\"><span><input type=\"button\" value=\"Logout\"></span></a></p></html>");
					}
				} else {
					pw.println("<html><body><p>Incorrect username or password.</p></body>");
					pw.println("<a href=\"/xpath\"><span><input type=\"button\" value=\"Return to login page\"></span></a></p></html>");
				}
			} else {
				pw.println("<html><body><p>Incorrect username or password.</p></body>");
				pw.println("<a href=\"/xpath\"><span><input type=\"button\" value=\"Return to login page\"></span></a></p></html>");
			}
			database.close();
		} catch(IOException e){
			// System.out.println("ERROR: " + e);
		}
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) 
			throws ServletException, IOException
	{
		res.sendRedirect("/xpath");
	}

}
