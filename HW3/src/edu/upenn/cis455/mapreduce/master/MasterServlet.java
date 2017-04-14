package edu.upenn.cis455.mapreduce.master;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis.stormlite.Config;
import edu.upenn.cis.stormlite.Topology;
import edu.upenn.cis.stormlite.TopologyBuilder;
import edu.upenn.cis.stormlite.bolt.MapBolt;
import edu.upenn.cis.stormlite.bolt.ReduceBolt;
import edu.upenn.cis.stormlite.distributed.WorkerJob;
import edu.upenn.cis.stormlite.spout.FileSpout;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis455.mapreduce.MyFileSpout;
import edu.upenn.cis455.mapreduce.MyPrintBolt;

class WorkerStatus {
	public String ip;
	public String port;
	public String status;
	public String job;
	public String keysRead;
	public String keysWritten;
	public String results;
	public long lastUpdate;
};

public class MasterServlet extends HttpServlet {

	private static final String FILE_SPOUT = "FILE_SPOUT";
	private static final String MAP_BOLT = "MAP_BOLT";
	private static final String REDUCE_BOLT = "REDUCE_BOLT";
	private static final String PRINT_BOLT = "PRINT_BOLT";

	static final long serialVersionUID = 455555001;
	private HashMap<String, WorkerStatus> workerStatus = new HashMap<String, WorkerStatus>();

	HttpURLConnection sendJob(String dest, String reqType, Config config, String job, String parameters)
			throws IOException {
		URL url = new URL("http://" + dest + "/" + job);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod(reqType);

		if (reqType.equals("POST")) {
			conn.setRequestProperty("Content-Type", "application/json");

			OutputStream os = conn.getOutputStream();
			byte[] toSend = parameters.getBytes();
			os.write(toSend);
			os.flush();
		} else
			conn.getOutputStream();

		return conn;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
		// response.setContentType("text/html");
		// PrintWriter out = response.getWriter();
		// out.println("<html><head><title>Master</title></head>");
		// out.println("<body>Hi, I am the master!</body></html>");
		// System.out.println(request.getRequestURI());
		String uri = request.getRequestURI();
		if (uri.equals("/status")) {
			
			response.setContentType("text/html");
			OutputStream out = response.getOutputStream();
			String content = new String();
			content += "<html>";
			content += "<style>table {font-family: arial, sans-serif;border-collapse: collapse;width: 100%;}";
			content += "td, th {border: 1px solid #dddddd;text-align: left;padding: 8px;}tr:nth-child(even) {";
			content += "background-color: #dddddd;}input{width:100%;}</style>";
			content += "<body>";
			content += "<table>";
			content += "<tr><th>Workers</th><th>IP:port</th><th>status</th><th>job</th><th>keys read</th><th>keys written</th><th>results</th></tr>";
			int counter = 0;
			Iterator<String> iterator = workerStatus.keySet().iterator();
			while (iterator.hasNext()) {
				WorkerStatus ws = workerStatus.get(iterator.next());
				if (new Date().getTime() - ws.lastUpdate > 30000) {
					System.out.println("Worker Expires");
					iterator.remove();
					continue;
				}
				content += "<tr>";
				content += "<th>" + (++counter) + "</th>";
				content += "<th>" + ws.ip + ":" + ws.port + "</th>";
				content += "<th>" + ws.status + "</th>";
				content += "<th>" + ws.job + "</th>";
				content += "<th>" + ws.keysRead + "</th>";
				content += "<th>" + ws.keysWritten + "</th>";
				content += "<th>" + ws.results + "</th>";
				content += "</tr>";
			}
			
			content += "</table>";
			content += "<form action=\"/status\" method=\"get\" />";
			content += "Class Name of the Job: <input type=\"text\" name=\"jobname\" value=\"edu.upenn.cis455.mapreduce.job.WordCount\"/><br/>";
			content += "Input Directory: <input type=\"text\" name=\"inputdir\" value=\"input\"/><br/>";
			content += "Output Directory: <input type=\"text\" name=\"outputdir\" value=\"output\"/><br/>";
			content += "Number of Map Executor: <input type=\"text\" name=\"mapnum\" value=\"1\"/><br/>";
			content += "Numer of Reduce Executor: <input type=\"text\" name=\"reducenum\" value=\"1\"/><br/>";
			content += "<input type=\"submit\" value=\"Submit New Job\" />";
			content += "</form>";
			content += "</body>";
			content += "</html>";
			out.write(content.getBytes());

			// form is submitted
			if (!request.getParameterMap().isEmpty()) {
				String jobName = request.getParameter("jobname");
				String inputDir = request.getParameter("inputdir");
				String outputDir = request.getParameter("outputdir");
				String mapNum = request.getParameter("mapnum");
				String reduceNum = request.getParameter("reducenum");

				Config config = new Config();

				config.put("jobname", jobName);
				config.put("inputdir", inputDir);
				config.put("outputdir", outputDir);
				config.put("mapExecutors", mapNum);
				config.put("reduceExecutors", reduceNum);
				config.put("spoutExecutors", Integer.toString(1));

				FileSpout spout = new MyFileSpout();
				MapBolt mapBolt = new MapBolt();
				ReduceBolt reduceBolt = new ReduceBolt();
				MyPrintBolt printBolt = new MyPrintBolt();

				TopologyBuilder builder = new TopologyBuilder();

				builder.setSpout(FILE_SPOUT, spout, 1);
				builder.setBolt(MAP_BOLT, mapBolt, Integer.valueOf(config.get("mapExecutors")))
						.fieldsGrouping(FILE_SPOUT, new Fields("value"));
				builder.setBolt(REDUCE_BOLT, reduceBolt, Integer.valueOf(config.get("reduceExecutors")))
						.fieldsGrouping(MAP_BOLT, new Fields("key"));
				builder.setBolt(PRINT_BOLT, printBolt, 1).firstGrouping(REDUCE_BOLT);

				Topology topo = builder.createTopology();
				WorkerJob job = new WorkerJob(topo, config);

				ObjectMapper mapper = new ObjectMapper();
				mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
				
				// put worker list
				String workerList = new String();
				int cc = 0;
				for (String worker : workerStatus.keySet()) {
					WorkerStatus ws = workerStatus.get(worker);
					String dest = ws.ip + ":" + ws.port;
					if (cc == 0)
						workerList += "[" + dest;
					else
						workerList += "," + dest;
					cc++;
				}
				workerList += "]";
				System.out.println(workerList);
				config.put("workerList", workerList);
				
				// define job
				cc = 0;
				for (String worker : workerStatus.keySet()) {
					config.put("workerIndex", String.valueOf(cc++));
					WorkerStatus ws = workerStatus.get(worker);
					String dest = ws.ip + ":" + ws.port;

					if (sendJob(dest, "POST", config, "definejob",
							mapper.writerWithDefaultPrettyPrinter().writeValueAsString(job))
									.getResponseCode() != HttpURLConnection.HTTP_OK) {
						throw new RuntimeException("Job definition request failed");
					}
				}
				// wait for completing the cluster construction
//				try {
//					Thread.sleep(3000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				// run job
				for (String worker : workerStatus.keySet()) {
					WorkerStatus ws = workerStatus.get(worker);
					String dest = ws.ip + ":" + ws.port;
					
					if (sendJob(dest, "POST", config, "runjob", "").getResponseCode() != HttpURLConnection.HTTP_OK) {
						throw new RuntimeException("Job execution request failed");
					}
				}
			}
		} else if (uri.equals("/workerstatus")) {
			//System.out.println(request.getQueryString());
			//String addr = request.getRemoteAddr() + ":" + request.getRemotePort();
			//System.out.println("ADDR: " + addr);
			String ip = request.getRemoteAddr();
			String addr = ip + ":" + request.getParameter("port");
			WorkerStatus ws = workerStatus.get(addr);
			
			if (ws == null)
				ws = new WorkerStatus();
			ws.ip = ip;
			ws.port = request.getParameter("port");
			ws.status = request.getParameter("status");
			ws.job = request.getParameter("job");
			ws.keysRead = request.getParameter("keysRead");
			ws.keysWritten = request.getParameter("keysWritten");
			ws.results = request.getParameter("results");
			ws.lastUpdate = new Date().getTime();
			workerStatus.put(addr, ws);
		} else if (uri.equals("/shutdown")) {
//			Iterator<String> iterator = workerStatus.keySet().iterator();
//			while (iterator.hasNext()) {
//				WorkerStatus ws = workerStatus.get(iterator.next());
//				if (new Date().getTime() - ws.lastUpdate > 30000) {
//					System.out.println("Worker Expires");
//					iterator.remove();
//					continue;
//				}
//			}
			OutputStream out = response.getOutputStream();
			String content = new String();
			int counter = 0;	
			for (String worker : workerStatus.keySet()) {
				WorkerStatus ws = workerStatus.get(worker);
				String strURL = "http://" + ws.ip + ":" + ws.port + "/shutdown";
				URL respURL = new URL(strURL);
				System.out.println(strURL);
				HttpURLConnection conn = (HttpURLConnection) respURL.openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("GET");
				if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					System.out.println("Shutdown Response Not OK : " + conn.getResponseCode());
				}
				counter++;
			}
			content += "Successfully Shutdown " + counter + " Workers";
			out.write(content.getBytes());
		} else if (uri.equals("/informshutdown")) {
//			System.out.println("inform shutdown IN");
			String ip = request.getRemoteAddr();
			String port = request.getParameter("port");
			String addr = ip + ":" + port;
//			System.out.println("search worker: " + addr);
//			Iterator<String> iterator = workerStatus.keySet().iterator();
//			while (iterator.hasNext()) {
//				System.out.println("ALL workers :" + iterator.next());
//			}
			if (workerStatus.remove(addr) == null)
				System.out.println("No Such Worker");
			else
				System.out.println("Shut Down Worker : " + addr);
			//response.sendRedirect("/status");
		}
		// content +=
		// out.write("<html><head><title>Master</title></head>".getBytes());
		// out.write("<body>Hi, I am the master!</body></html>".getBytes());
	}
}
