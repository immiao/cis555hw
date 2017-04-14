package edu.upenn.cis455.mapreduce.worker;

import static spark.Spark.setPort;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleepycat.je.DatabaseException;

import edu.upenn.cis.stormlite.Config;
import edu.upenn.cis.stormlite.DistributedCluster;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.distributed.WorkerHelper;
import edu.upenn.cis.stormlite.distributed.WorkerJob;
import edu.upenn.cis.stormlite.routers.StreamRouter;
import edu.upenn.cis.stormlite.tuple.Tuple;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

/**
 * Simple listener for worker creation
 * 
 * @author zives
 *
 */

class ShutDownThread extends Thread{
	@Override
	public void run() {
		try {
			Thread.sleep(2000);
			WorkerServer.shutdown();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

public class WorkerServer {
	static Logger log = Logger.getLogger(WorkerServer.class);

	static public DistributedCluster cluster = new DistributedCluster();

//	List<TopologyContext> contexts = new ArrayList<>();

	static int myPort;

//	static List<String> topologies = new ArrayList<>();

	TopologyContext crtContext = null;
	String crtJob = null;
	
	static public String storeDir = null;
	static public DbEnv m_dbEnv = new DbEnv();
	
	static String masterAddr = null;
	
	private String getWorkerState() {
		if (crtContext == null)
			return "IDLE";
		else if (crtContext.getState() == TopologyContext.STATE.IDLE)
			return "IDLE";
		else if (crtContext.getState() == TopologyContext.STATE.MAP)
			return "MAP";
		else if (crtContext.getState() == TopologyContext.STATE.WAITING)
			return "WAITING";
		else if (crtContext.getState() == TopologyContext.STATE.REDUCE)
			return "REDUCE";
		return null;
	}

	private int getKeysRead() {
		if (crtContext == null)
			return 0;
		return crtContext.keysRead.get();
	}

	private int getKeysWritten() {
		if (crtContext == null)
			return 0;
		return crtContext.keysWritten.get();
	}

	private String getResults() {
		if (crtContext == null)
			return "No Results";
		synchronized (crtContext.results) {
			return crtContext.results.toString();
		}
	}

	public WorkerServer(String masterAddr, String dir, int myPort) throws MalformedURLException, DatabaseException, NoSuchAlgorithmException {
		storeDir = dir;
		this.myPort = myPort;
		this.masterAddr = masterAddr;
		// clear database
		File envHome = new File(WorkerServer.storeDir + "/");
		File[] files = envHome.listFiles();
		for (File file : files) {
			file.delete();
			System.out.println("delete:" + file.getAbsolutePath());
		}
		envHome.mkdirs();
		m_dbEnv.setup(envHome, false);
		
		TimerTask timerTask = new TimerTask() {

			@Override
			public void run() {

				try {
					String[] s = masterAddr.split(":", 2);
					String masterIP = s[0];
					int masterPort = Integer.parseInt(s[1]);
					String strURL = "http://" + masterIP + ":" + masterPort + "/workerstatus";
					strURL += "?port=" + myPort;
					strURL += "&status=" + getWorkerState();
					strURL += "&job=" + URLEncoder.encode((crtJob == null ? "No Job" : crtJob), "UTF-8");
					strURL += "&keysRead=" + getKeysRead();
					strURL += "&keysWritten=" + getKeysWritten();
					strURL += "&results=" + URLEncoder.encode(getResults(), "UTF-8");
					URL respURL = new URL(strURL);
					HttpURLConnection conn = (HttpURLConnection) respURL.openConnection();
					conn.setDoOutput(true);
					conn.setRequestMethod("GET");
					
//					OutputStream os = conn.getOutputStream();
//					os.flush();
					System.out.println(respURL.toString());
					if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
						System.out.println("Response Not OK : " + conn.getResponseCode());
					}

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};

		Timer timer = new Timer("MyTimer");
		timer.scheduleAtFixedRate(timerTask, 30, 10000);
		log.info("Creating server listener at socket " + myPort);

		setPort(myPort);
		final ObjectMapper om = new ObjectMapper();
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		Spark.post(new Route("/definejob") {

			@Override
			public Object handle(Request arg0, Response arg1) {
				// log.info("define job");
				WorkerJob workerJob;
				try {
					workerJob = om.readValue(arg0.body(), WorkerJob.class);

					try {
//						log.info("Processing job definition request" + workerJob.getConfig().get("job") + " on machine "
//								+ workerJob.getConfig().get("workerIndex"));
						// each job has its topology
//						contexts.add(cluster.submitTopology(workerJob.getConfig().get("jobname"), workerJob.getConfig(),
//								workerJob.getTopology()));
//						crtContext = contexts.get(contexts.size() - 1);
						crtJob = workerJob.getConfig().get("jobname");
						crtContext = cluster.submitTopology(crtJob, workerJob.getConfig(),
								workerJob.getTopology());
						
						//System.out.println("add new contexts : " + contexts.size());
//						synchronized (topologies) {
//							topologies.add(workerJob.getConfig().get("jobname"));
//						}
						// directly start job here
						//cluster.startTopology();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (DatabaseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return "Job launched";
				} catch (IOException e) {
					e.printStackTrace();

					// Internal server error
					arg1.status(500);
					return e.getMessage();
				}

			}

		});

		Spark.post(new Route("/runjob") {

			@Override
			public Object handle(Request arg0, Response arg1) {
				log.info("Starting job!");
				cluster.startTopology();

				return "Started";
			}
		});

		Spark.post(new Route("/pushdata/:stream") {

			@Override
			public Object handle(Request arg0, Response arg1) {
				// log.info("pushdata");
				try {
					String stream = arg0.params(":stream");
					Tuple tuple = om.readValue(arg0.body(), Tuple.class);

					// Find the destination stream and route to it
					// directly call the up stream's router to execute
					StreamRouter router = cluster.getStreamRouter(stream);

//					if (contexts.isEmpty())
//						log.error("No topology context -- were we initialized??");

					// increase the
					if (!tuple.isEndOfStream()) {
						// log.info("111Worker received: " + tuple + " for " +
						// stream + "\n222Worker received: " + tuple.getValues()
						// + " for " + stream + "\n"
						// + "333Worker received: " +
						// router.getKey(tuple.getValues()));
						//contexts.get(contexts.size() - 1).incSendOutputs(router.getKey(tuple.getValues()));
						crtContext.incSendOutputs(router.getKey(tuple.getValues()));
					}
					//System.out.println("HERE stream : " + stream + ". Context size : " + contexts.size());
					if (crtContext == null)
						System.out.println("NULLLLLLLLLL");
					if (router == null)
						System.out.println("Router NULLLLL");
					if (tuple.isEndOfStream())
						router.executeEndOfStreamLocally(crtContext);
					else
						router.executeLocally(tuple, crtContext);
					
//					if (tuple.isEndOfStream())
//						router.executeEndOfStreamLocally(contexts.get(contexts.size() - 1));
//					else
//						router.executeLocally(tuple, contexts.get(contexts.size() - 1));

					return "OK";
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

					arg1.status(500);
					return e.getMessage();
				}

			}

		});
		
		Spark.get(new Route("/shutdown") {

			@Override
			public Object handle(Request arg0, Response arg1) {
				new ShutDownThread().start();
				return "";
			}
		});
	}

	// public static void createWorker(Map<String, String> config) {
	// if (!config.containsKey("workerList"))
	// throw new RuntimeException("Worker spout doesn't have list of worker IP
	// addresses/ports");
	//
	// if (!config.containsKey("workerIndex"))
	// throw new RuntimeException("Worker spout doesn't know its worker ID");
	// else {
	// String[] addresses = WorkerHelper.getWorkers(config);
	// String myAddress = addresses[Integer.valueOf(config.get("workerIndex"))];
	//
	// log.debug("Initializing worker " + myAddress);
	// // log.info("Initializing worker " + myAddress);
	// URL url;
	// try {
	// url = new URL(myAddress);
	//
	// new WorkerServer(url.getPort());
	// } catch (MalformedURLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }

	public static void createWorker(String masterAddr, String dir, int port) throws MalformedURLException, DatabaseException, NoSuchAlgorithmException {
		new WorkerServer(masterAddr, dir, port);
	}

	public static void shutdown() {
		
//		synchronized (topologies) {
//			for (String topo : topologies)
//				cluster.killTopology(topo);
//		}

//		cluster.shutdown();
//
		String strURL = "http://" + masterAddr + "/informshutdown";
		strURL += "?port=" + myPort;
		URL respURL = null;
		try {
			respURL = new URL(strURL);
			HttpURLConnection conn = (HttpURLConnection) respURL.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				System.out.println("Inform Shutdown Response Not OK : " + conn.getResponseCode());
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}
}
