package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import edu.upenn.cis455.webserver.HttpServer;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {

	/* TODO: Implement user interface for XPath engine here */

	/* You may want to override one or both of the following methods */

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HashMap<String, Vector<String>> m = (HashMap<String, Vector<String>>) request.getParameterMap();
		URL url = new URL(request.getRequestURL().toString());
		if (m.containsKey("logout_button")) {
			// normally, getSession(false) should not be null
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.invalidate();
			}
			response.sendRedirect("http://" + url.getHost() + ":" + url.getPort());
			return;
		} else if (m.containsKey("login_button")) {
			String usr = request.getParameter("usr");
			String psw = request.getParameter("psw");

			if (usr == null || psw == null) {
				response.sendRedirect("http://" + url.getHost() + ":" + url.getPort());
				return;
			}
			String dbPsw = HttpServer.dbEnv.getPsw(usr);
			if (dbPsw != null && psw.equals(dbPsw)) {
				// create a new session
				HttpSession session = request.getSession();
				session.setAttribute("username", usr);
			}
//			if (psw.equals(HttpServer.account.get(usr))) {
//				// create a new session
//				HttpSession session = request.getSession();
//				session.setAttribute("username", usr);
//			}
			
			response.sendRedirect("http://" + url.getHost() + ":" + url.getPort());
		} else if (m.containsKey("create_button")) {
			String usr = request.getParameter("usr");
			String psw = request.getParameter("psw");

			if (usr == null || psw == null) {
				response.sendRedirect("http://" + url.getHost() + ":" + url.getPort());
				return;
			}
			HttpServer.dbEnv.insertAccount(usr, psw);
			//HttpServer.account.put(usr, psw);
			response.sendRedirect("http://" + url.getHost() + ":" + url.getPort());
		} 
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HashMap<String, Vector<String>> m = (HashMap<String, Vector<String>>) request.getParameterMap();
		
		PrintWriter out = response.getWriter();
		
		if (m.containsKey("url")) {
			String url = m.get("url").get(0);
			String content = HttpServer.dbEnv.getPageContent(url);
			out.println(content);
			return;
		}
		
		HttpSession session = request.getSession(false);
		out.println("<html><body>");
		if (session == null) {
			out.println("<form action=\"/register.jsp\" method=\"post\" />");
			out.println("Username: <input type=\"text\" name=\"usr\" /><br/>");
			out.println("Password: <input type=\"password\" name=\"psw\" /><br/>");
			out.println("<input type=\"submit\" name=\"login_button\" value=\"Login\" />");
			out.println("<input type=\"submit\" name=\"create_button\" value=\"Create New Account\" />");
			out.println("</form>");
		} else {
			out.println("<p>Username: " + session.getAttribute("username") + "</p>");
			out.println("<form action=\"/register.jsp\" method=\"post\" />");
			out.println("<input type=\"submit\" name=\"logout_button\" value=\"Logout\" />");
			out.println("</form>");
		}
		out.println("</body></html>");
	}

}
