package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import edu.upenn.cis455.webserver.HttpServer;
import edu.upenn.cis455.webserver.utils.ChannelInfo;
import edu.upenn.cis455.webserver.utils.PageInfo;
import edu.upenn.cis455.webserver.utils.UserInfo;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {

	/* TODO: Implement user interface for XPath engine here */
	static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
	/* You may want to override one or both of the following methods */

	private void displayChannel(PrintWriter out, ChannelInfo channel) {
		out.println("<html><body>");
		out.println("<div class=\"channelheader\">");
		out.println("<p>Channel name: " + channel.name + ", created by: " + channel.userName);
		out.println("</div>");

		for (String url : channel.url) {
			PageInfo info = HttpServer.dbEnv.getPageInfo(url);
			Date date = new Date(info.m_crawledDate);
			out.println("<p>Crawled on: " + format.format(date) + "</p>");
			out.println("<p>Location: " + url + "</p>");
			out.println("<div class=\"document\">");
			out.println(HttpServer.dbEnv.getPageContent(url));
			out.println("</div>");
		}
		out.println("</body></html>");
		out.println("<a href=\"/\">Back to main page.</a>");
	}

	private void HtmlMessage(PrintWriter out, String[] msg) {
		out.println("<html><body>");
		for (String s : msg) {
			out.println("<p>" + s + "</p>");
		}
		out.println("<a href=\"/\">Back to main page.</a>");
		out.println("</body></html>");
	}

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
			response.sendRedirect("http://" + url.getHost() + ":" + url.getPort() + "/register.jsp");
			return;
		} else if (m.containsKey("login_button")) {
			String usr = request.getParameter("usr");
			String psw = request.getParameter("psw");

			if (usr == null || psw == null) {
				response.sendRedirect("http://" + url.getHost() + ":" + url.getPort() + "/register.jsp");
				return;
			}
			String dbPsw = HttpServer.dbEnv.getPsw(usr);
			if (dbPsw != null && psw.equals(dbPsw)) {
				// create a new session
				HttpSession session = request.getSession();
				session.setAttribute("username", usr);
				response.sendRedirect("http://" + url.getHost() + ":" + url.getPort() + "/");
			}
			else
				response.sendRedirect("http://" + url.getHost() + ":" + url.getPort() + "/register.jsp");
			// if (psw.equals(HttpServer.account.get(usr))) {
			// // create a new session
			// HttpSession session = request.getSession();
			// session.setAttribute("username", usr);
			// }
			
		} else if (m.containsKey("create_button")) {
			String usr = request.getParameter("usr");
			String psw = request.getParameter("psw");

			if (usr == null || psw == null) {
				response.sendRedirect("http://" + url.getHost() + ":" + url.getPort() + "/register.jsp");
				return;
			}
			HttpServer.dbEnv.insertAccount(usr, psw);
			// HttpServer.account.put(usr, psw);
			response.sendRedirect("http://" + url.getHost() + ":" + url.getPort() + "/register.jsp");
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HashMap<String, Vector<String>> m = (HashMap<String, Vector<String>>) request.getParameterMap();

		PrintWriter out = response.getWriter();
		String path = request.getServletPath();
		// System.out.println(request.getServletPath() + " HAHA");
		if (m.containsKey("url")) {
			String url = m.get("url").get(0);
			String content = HttpServer.dbEnv.getPageContent(url);
			out.println(content);
			return;
		}

		HttpSession session = request.getSession(false);

		if (session == null) {
			if (path.equals("/register.jsp")) {
				out.println("<html><body>");
				out.println("<form action=\"/register.jsp\" method=\"post\" />");
				out.println("Username: <input type=\"text\" name=\"usr\" /><br/>");
				out.println("Password: <input type=\"password\" name=\"psw\" /><br/>");
				out.println("<input type=\"submit\" name=\"login_button\" value=\"Login\" />");
				out.println("<input type=\"submit\" name=\"create_button\" value=\"Create New Account\" />");
				out.println("</form>");
				out.println("</body></html>");
			} else {
				response.sendError(401);
			}
		} else {
			String usr = session.getAttribute("username").toString();
			// System.out.println("PATH: " + path);
			if (path.equals("/")) {
				out.println("<html>");
				out.println("<style>.inline{display: inline;margin-left: 10px;}</style>");
				out.println("<body>");
				out.println("<div>");
				out.println("<p class=\"inline\">Username: " + usr + "</p>");
				out.println("<form class=\"inline\" action=\"/register.jsp\" method=\"post\" />");
				out.println("<input type=\"submit\" name=\"logout_button\" value=\"Logout\" />");
				out.println("</form>");
				out.println("</div>");
				out.println("</body></html>");
				
				//out.println("<p class=\"inline\">asdasdasd</p><a class=\"inline\" href=\"/create?name=hello&xpath=/foo\">Visit W3Schools</a>");
				ArrayList<ChannelInfo> channels = HttpServer.dbEnv.getAllChannel();
				UserInfo info = HttpServer.dbEnv.getUserInfo(usr);
				for (ChannelInfo channel : channels) {
					out.println("<div>");
					out.println("<p class=\"inline\">" + channel.name + "</p>");
					if (info.subscribed.contains(channel.name)) {
						out.println("<a class=\"inline\" href=\"/show?name=" + channel.name + "\">" + "Show</a>");
						out.println("<a class=\"inline\" href=\"/unsubscribe?name=" + channel.name + "\">" + "Unsubscribe</a>");
					} else {
						out.println("<a class=\"inline\" href=\"/subscribe?name=" + channel.name + "\">" + "Subscribe</a>");
					}
					
					System.out.println("CHANNEL USERNAME: " + channel.userName);
					if (channel.userName.equals(usr)) {
						out.println("<a class=\"inline\" href=\"/delete?name=" + channel.name + "\">" + "Delete</a>");
					}
					
					out.println("</div>");
				}
				
				//out.println("<a class=\"inline\" href=\"/subscribe?name=test\">Subscribe</a>");
				
				out.println("<form action=\"/create\" method=\"get\" />");
				out.println("XPathName: <input type=\"text\" name=\"name\" /><br/>");
				out.println("XPath: <input type=\"text\" name=\"xpath\" /><br/>");
				out.println("<input type=\"submit\" value=\"Create New XPath\" />");
				out.println("</form>");
			} else if (path.equals("/create")) {
				//HttpServer.dbEnv.updateChannelInfo("test", new ChannelInfo());
				String name = request.getParameter("name");
				String xpath = request.getParameter("xpath");
				if (!HttpServer.dbEnv.insertChannel(usr, name, xpath)) {
					HtmlMessage(out, new String[] { "Create channel failed!", "The channel already exists."});
					response.sendError(409);
				} else {
					HtmlMessage(out, new String[] { "Create channel succeeded!", "Name: " + name, "XPath: " + xpath });
				}
				//HttpServer.dbEnv.subscribe(usr, name);
			} else if (path.equals("/subscribe")) {
				String name = request.getParameter("name");
				//System.out.println("ASDASD" + usr + " : " + name);
				int status = HttpServer.dbEnv.subscribe(usr, name);
				
				if (status == -1) {
					HtmlMessage(out, new String[] { "Subscribe channel failed!", "Channel not found."});
					response.sendError(404);
				}
				else if (status == -2) {
					HtmlMessage(out, new String[] { "Subscribe channel failed!", "Already subscribed the channel."});
					response.sendError(409);
				}
				else if (status == 0)
					HtmlMessage(out, new String[] { "Subscribe channel succeeded!", "Name: " + name });
				
			} else if (path.equals("/unsubscribe")) {
				String name = request.getParameter("name");
				if (!HttpServer.dbEnv.unsubscribe(usr, name)) {
					HtmlMessage(out, new String[] { "Unsubscribe channel failed!", "Channel not found."});
					response.sendError(404);
				}
				else
					HtmlMessage(out, new String[] { "Unsubscribe channel succeeded!", "Name: " + name });
			} else if (path.equals("/delete")) {
				String name = request.getParameter("name");
				int status = HttpServer.dbEnv.delete(usr, name);
				if (status == -1) {
					HtmlMessage(out, new String[] { "Delete channel failed!", "Channel not found."});
					response.sendError(404);
				}
				else if (status == -2) {
					HtmlMessage(out, new String[] { "Delete channel failed!", "The channel is not created by current user."});
					response.sendError(403);
				}
				else if (status == 0)
					HtmlMessage(out, new String[] { "Delete channel succeeded!", "Name: " + name });
			} else if (path.equals("/show")) {
				String name = request.getParameter("name");
				ChannelInfo channel = HttpServer.dbEnv.show(usr, name);
				if (channel == null) {
					HtmlMessage(out, new String[] { "Show channel failed!", "Channel not found."});
					response.sendError(404);
				} else {
					displayChannel(out, channel);
				}
			} else {
				out.println("<html><body>");
				out.println("<p>Username: " + usr + "</p>");
				out.println("<form action=\"/register.jsp\" method=\"post\" />");
				out.println("<input type=\"submit\" name=\"logout_button\" value=\"Logout\" />");
				out.println("</form>");
				out.println("</body></html>");
			}
		}

	}

}
