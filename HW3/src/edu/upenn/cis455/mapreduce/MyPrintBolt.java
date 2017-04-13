package edu.upenn.cis455.mapreduce;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.StreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis455.mapreduce.worker.WorkerServer;
import test.edu.upenn.cis.stormlite.PrintBolt;

public class MyPrintBolt implements IRichBolt {

	static Logger log = Logger.getLogger(PrintBolt.class);

	Fields myFields = new Fields();

	String executorId = UUID.randomUUID().toString();

	TopologyContext context = null;
	FileWriter fw = null;
	BufferedWriter bw = null;
	
	@Override
	public void cleanup() {
		// Do nothing
		try {
			bw.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void execute(Tuple input) {
		if (!input.isEndOfStream()) {
			synchronized (bw) {
				List<Object> output = input.getValues();
				try {
					bw.write((String)output.get(0));
					bw.write(", ");
					bw.write((String)output.get(1));
					bw.write("\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println(getExecutorId() + ": " + input.toString());
			synchronized (context.results) {
				
				if (context.results.size() < 100)
					context.results.add(input.toString());
			}
		}
	}

	@Override
	public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
		// Do nothing
		this.context = context;
		String outputDir = WorkerServer.storeDir + "/" + stormConf.get("outputdir") + "/output.txt";
		System.out.println("OUTPUT DIR: " + outputDir);
		try {
			fw = new FileWriter(outputDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bw = new BufferedWriter(fw);
	}

	@Override
	public String getExecutorId() {
		return executorId;
	}

	@Override
	public void setRouter(StreamRouter router) {
		// Do nothing
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(myFields);
	}

	@Override
	public Fields getSchema() {
		return myFields;
	}

}
