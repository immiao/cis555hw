package edu.upenn.cis455.mapreduce;

import javax.servlet.ServletException;

import org.apache.log4j.PropertyConfigurator;

import edu.upenn.cis455.mapreduce.master.MasterServer;
import edu.upenn.cis455.mapreduce.master.MasterServlet;

public class MapReduce {
	public static void main(String[] args) throws ServletException {
		PropertyConfigurator.configure("log4j.properties");
		MasterServlet servlet = new MasterServlet();
		servlet.init();
		MasterServer.createMaster(servlet, 8080);
	}
}
