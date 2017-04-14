package edu.upenn.cis455.mapreduce;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletException;

import org.apache.log4j.PropertyConfigurator;

import com.sleepycat.je.DatabaseException;

import edu.upenn.cis455.mapreduce.master.MasterServer;
import edu.upenn.cis455.mapreduce.worker.WorkerServer;

public class MapReduce {
	public static void main(String[] args) throws ServletException, NumberFormatException, IOException, DatabaseException, NoSuchAlgorithmException {
		PropertyConfigurator.configure("log4j.properties");

		if (args.length < 3) {
			MasterServer.createMaster(8000);
		} else {
			WorkerServer.createWorker(args[0], args[1], Integer.parseInt(args[2]));
		}
		System.out.println("Press [Enter] to exit...");
		(new BufferedReader(new InputStreamReader(System.in))).readLine();

		WorkerServer.shutdown();
        System.exit(0);
	}
}
