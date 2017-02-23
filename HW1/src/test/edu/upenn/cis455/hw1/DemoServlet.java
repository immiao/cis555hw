package test.edu.upenn.cis455.hw1;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.Enumeration;

public class DemoServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<HTML><HEAD><TITLE>Simple Servlet</TITLE></HEAD><BODY>");
		out.println("<P>Hello!</P>");
		out.println("</BODY></HTML>");	
		//System.out.println(response.encodeURL("http://www.google.com/ @*(#&"));
		//response.sendRedirect("http://www.google.com");
	}
	
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		
		PrintWriter out = response.getWriter();
		out.println("<HTML><HEAD><TITLE>This is POST</TITLE></HEAD><BODY>");
		out.println("<P>Output all parameters!</P>");
		Enumeration<String> e = request.getParameterNames();
		while (e.hasMoreElements()) {
			String name = e.nextElement();
			out.println("<P>" + name + "=" + request.getParameter(name) + "</P>");
		}
		out.println("</BODY></HTML>");		
	}
}
