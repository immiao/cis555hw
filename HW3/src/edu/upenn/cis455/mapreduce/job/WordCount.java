package edu.upenn.cis455.mapreduce.job;

import java.util.Iterator;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;

public class WordCount implements Job {

	public void map(String key, String value, Context context) {
		// Your map function for WordCount goes here
		context.write(key, "");
	}

	public void reduce(String key, Iterator<String> values, Context context) {
		// Your reduce function for WordCount goes here
		int sum = 0;
		while (values.hasNext()) {
			sum += Integer.parseInt(values.next());
		}
		context.write(key, Integer.toString(sum));
	}

}
