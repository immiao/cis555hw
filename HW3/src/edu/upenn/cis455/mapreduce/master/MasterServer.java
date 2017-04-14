package edu.upenn.cis455.mapreduce.master;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import edu.upenn.cis.stormlite.distributed.WorkerHelper;
import edu.upenn.cis455.mapreduce.worker.WorkerServer;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class MasterServer {	
	MasterServlet servlet = new MasterServlet();
	
	public MasterServer(int port) throws ServletException {
		
		servlet.init();
		Spark.setPort(port);
		
		Spark.get(new Route("/workerstatus") {

			@Override
			public Object handle(Request arg0, Response arg1) {
				try {
					servlet.service(arg0.raw(), arg1.raw());
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return "";
			}
		});
		
		Spark.get(new Route("/status") {

			@Override
			public Object handle(Request arg0, Response arg1) {
				try {
					servlet.service(arg0.raw(), arg1.raw());
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return "";
			}
		});
		
		Spark.get(new Route("/shutdown") {

			@Override
			public Object handle(Request arg0, Response arg1) {
				try {
					servlet.service(arg0.raw(), arg1.raw());
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return "";
			}
		});
		
		Spark.get(new Route("/informshutdown") {

			@Override
			public Object handle(Request arg0, Response arg1) {
				try {
					servlet.service(arg0.raw(), arg1.raw());
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return "";
			}
		});
	}
	
	public static void createMaster(int port) throws ServletException {
		new MasterServer(port);
	}
	
}
