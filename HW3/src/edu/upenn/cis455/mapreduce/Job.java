package edu.upenn.cis455.mapreduce;

import java.util.Iterator;

import edu.upenn.cis.stormlite.Context;

public interface Job {

  void map(String key, String value, Context context);
  
  void reduce(String key, Iterator<String> values, Context context);
  
}
