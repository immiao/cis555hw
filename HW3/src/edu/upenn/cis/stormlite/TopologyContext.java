/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.upenn.cis.stormlite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import edu.upenn.cis.stormlite.routers.StreamRouter;
import edu.upenn.cis455.mapreduce.worker.DbEnv;

/**
 * Information about the execution of a topology, including
 * the stream routers
 * 
 * @author zives
 *
 */
public class TopologyContext {
	Topology topology;
	
	Queue<Runnable> taskQueue;
	
	public static enum STATE {IDLE, MAP, WAITING, REDUCE};
	
	STATE state = STATE.IDLE;
	
	int mapOutputs = 0;
	
	int reduceOutputs = 0;
	
	Map<String, Integer> sendOutputs = new HashMap<>();
	
	/**
	 * Mappings from stream IDs to routers
	 */
	Map<String,StreamRouter> next = new HashMap<>();
	
	DbEnv m_dbEnv;
	
	public AtomicInteger keysRead = new AtomicInteger();
	public AtomicInteger keysWritten = new AtomicInteger();
	public ArrayList<String> results = new ArrayList<String>();

	public TopologyContext(Topology topo, Queue<Runnable> theTaskQueue, DbEnv dbEnv) {
		topology = topo;
		taskQueue = theTaskQueue;
		m_dbEnv = dbEnv;
	}
	
	public DbEnv getDbEnv() {
		return m_dbEnv;
	}
	
	public Topology getTopology() {
		return topology;
	}
	
	public void setTopology(Topology topo) {
		this.topology = topo;
	}
	
	public void addStreamTask(Runnable next) {
		taskQueue.add(next);
	}

	public STATE getState() {
		return state;
	}

	public void setState(STATE state) {
		this.state = state;
	}

	public int getMapOutputs() {
		return mapOutputs;
	}

	public void incMapOutputs(String key) {
		this.mapOutputs++;
	}

	public int getReduceOutputs() {
		return reduceOutputs;
	}

	public void incReduceOutputs(String key) {
		this.reduceOutputs++;
	}
	
	public void incSendOutputs(String key) {
		 if (!sendOutputs.containsKey(key))
			 sendOutputs.put(key, new Integer(0));
		 
		 sendOutputs.put(key,  new Integer(sendOutputs.get(key) + 1));
	}
	
	public Map<String, Integer> getSendOutputs() {
		return sendOutputs;
	}
	
}
