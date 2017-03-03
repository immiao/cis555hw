package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.webserver.HttpServer;

public class RegisterServlet extends HttpServlet {
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HashMap<String, Vector<String>> m = (HashMap<String, Vector<String>>) request.getParameterMap();
		// for (String k : m.keySet()) {
		// System.out.println(k + " : " + m.get(k).get(0));
		// }
		String usr = request.getParameter("usr");
		String psw = request.getParameter("psw");
		URL url = new URL(request.getRequestURL().toString());
		
		if (usr == null || psw == null) {
			response.sendRedirect("http://" + url.getHost() + ":" + url.getPort());
			return;
		}

		if (m.containsKey("login_button")) {
			System.out.println(psw + "----" + HttpServer.account.get(usr));
			if (psw.equals(HttpServer.account.get(usr))) {
				HttpSession session = request.getSession(); // create a new session
				session.setAttribute("username", usr);
			}
		} else if (m.containsKey("create_button")) {
			HttpServer.account.put(usr, psw);
		} else if (m.containsKey("logout_button")) {
			HttpSession session = request.getSession(false); // normally, getSession(false) should not be null
			if (session != null) {
				session.invalidate();
			}
		}
		response.sendRedirect("http://" + url.getHost() + ":" + url.getPort());
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}
}
