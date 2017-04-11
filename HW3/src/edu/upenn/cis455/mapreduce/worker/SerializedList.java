package edu.upenn.cis455.mapreduce.worker;

import java.io.Serializable;
import java.util.ArrayList;

public class SerializedList implements Serializable {
	public ArrayList<String> arr = new ArrayList<String>();
}
