package edu.upenn.cis455.mapreduce;

import edu.upenn.cis.stormlite.spout.FileSpout;
import edu.upenn.cis455.mapreduce.worker.WorkerServer;

public class MyFileSpout extends FileSpout {

	@Override
	public String getFilename() {
		// TODO Auto-generated method stub
		return WorkerServer.storeDir;
	}

}
