package edu.upenn.cis455.mapreduce.master;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class MasterServlet extends HttpServlet {

  static final long serialVersionUID = 455555001;

  public void doGet(HttpServletRequest request, HttpServletResponse response) 
       throws java.io.IOException
  {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println("<html><head><title>Master</title></head>");
    out.println("<body>Hi, I am the master!</body></html>");
  }
}
  
