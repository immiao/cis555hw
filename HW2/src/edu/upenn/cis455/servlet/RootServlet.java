package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class RootServlet extends HttpServlet {
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		/* TODO: Implement user interface for XPath engine here */
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		out.println("<html><body>");

		HttpSession session = request.getSession(false);

		if (session == null) {
			out.println("<form action=\"/register.jsp\" method=\"post\" />");
			out.println("Username: <input type=\"text\" name=\"usr\" /><br/>");
			out.println("Password: <input type=\"password\" name=\"psw\" /><br/>");
			out.println("<input type=\"submit\" name=\"login_button\" value=\"Login\" />");
			out.println("<input type=\"submit\" name=\"create_button\" value=\"Create New Account\" />");
			out.println("</form>");
		} else {
			out.println("<p>" + session.getAttribute("username") + "</p>");
			out.println("<form action=\"/register.jsp\" method=\"post\" />");
			out.println("<input type=\"submit\" name=\"logout_button\" value=\"Logout\" />");
			out.println("</form>");
		}

		out.println("</body></html>");
	}
}
