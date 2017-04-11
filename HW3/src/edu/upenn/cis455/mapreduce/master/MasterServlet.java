package edu.upenn.cis455.mapreduce.master;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class MasterServlet extends HttpServlet {

	static final long serialVersionUID = 455555001;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
		// response.setContentType("text/html");
		// PrintWriter out = response.getWriter();
		// out.println("<html><head><title>Master</title></head>");
		// out.println("<body>Hi, I am the master!</body></html>");
		// System.out.println(request.getRequestURI());
		if (request.getRequestURI().equals("/status")) {
			response.setContentType("text/html");
			OutputStream out = response.getOutputStream();
			String content = new String();
			content += "<table>";
			content += "<tr><th>IP:port</th><th>status</th><th>job</th><th>keys read</th><th>keys written</th></tr>";
			content += "</table>";
			out.write(content.getBytes());
		}
		// content +=
		// out.write("<html><head><title>Master</title></head>".getBytes());
		// out.write("<body>Hi, I am the master!</body></html>".getBytes());
	}
}
