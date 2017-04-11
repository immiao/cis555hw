package edu.upenn.cis455.mapreduce.worker;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

public class DbEnv {
	private Environment m_env;

	private HashMap<String, Database> m_reduceBoltDb = new HashMap<String, Database>();
	private EnvironmentConfig m_envConfig = new EnvironmentConfig();
	private DatabaseConfig m_dbConfig = new DatabaseConfig();
	private StoredClassCatalog m_classCatalog;
	private Database m_classCatalogDb;

	public DbEnv() {

	}

	public void setup(File envHome, boolean readOnly, int totalReduceBolt)
			throws DatabaseException, NoSuchAlgorithmException {
		if (envHome.exists()) 
			envHome.delete();
		envHome.mkdir();
		
		m_envConfig.setReadOnly(readOnly);
		m_dbConfig.setReadOnly(readOnly);

		m_envConfig.setAllowCreate(!readOnly);
		m_dbConfig.setAllowCreate(!readOnly);

		m_envConfig.setTransactional(!readOnly);
		m_dbConfig.setTransactional(!readOnly);
		
		m_env = new Environment(envHome, m_envConfig);
		
		m_classCatalogDb = m_env.openDatabase(null, "ClassCatalogDB", m_dbConfig);
		m_classCatalog = new StoredClassCatalog(m_classCatalogDb);
	}

	public void addReduceBoltDb(String id) {
		Database newDb = m_env.openDatabase(null, id, m_dbConfig);
		m_reduceBoltDb.put(id, newDb);
	}

	public void addValue(String id, String key, String value) {
		Database crtDb = m_reduceBoltDb.get(id);
		
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();
		StringBinding.stringToEntry(key, keyEntry);
		EntryBinding dataBinding = new SerialBinding(m_classCatalog, SerializedList.class);
		Cursor cursor = crtDb.openCursor(null, null);
		OperationStatus status = cursor.getSearchKey(keyEntry, dataEntry, LockMode.DEFAULT);
		cursor.close();
		
		if (status == OperationStatus.SUCCESS) {
			SerializedList values = (SerializedList)dataBinding.entryToObject(dataEntry);
			values.arr.add(value);
			
			dataBinding.objectToEntry(values, dataEntry);
			Transaction txn = m_env.beginTransaction(null, null);
			status = crtDb.put(txn, keyEntry, dataEntry);
			if (status != OperationStatus.SUCCESS) {
				txn.abort();
				System.out.println("Insert Failed");
			}
			txn.commit();
		} else {
			SerializedList values = new SerializedList();
			values.arr.add(value);
			
			dataBinding.objectToEntry(values, dataEntry);
			Transaction txn = m_env.beginTransaction(null, null);
			status = crtDb.put(txn, keyEntry, dataEntry);
			if (status != OperationStatus.SUCCESS) {
				txn.abort();
				System.out.println("Insert Failed");
			}
			txn.commit();
		}
		
		
		
		// use putNoDupData to put duplicated keys
		// System.out.println("DU?: " + crtDb.getConfig().getSortedDuplicates());
//		OperationResult status = crtDb.put(txn, keyEntry, dataEntry, Put.CURRENT, null);
//		if (status == null) {
//			txn.abort();
//			throw new RuntimeException("Reduce Bolt DB Data insertion got status " + status);
//		}
		
//		OperationStatus status = crtDb.put(txn, keyEntry, dataEntry);
//		if (status != OperationStatus.SUCCESS) {
//			txn.abort();
//			throw new RuntimeException("Reduce Bolt DB Data insertion got status " + status);
//		}
//		txn.commit();
	}

//	public ArrayList<String> getValueList(String id, String key) {
//		ArrayList<String> valueList = new ArrayList<String>();
//		Database crtDb = m_reduceBoltDb.get(id);
//		DatabaseEntry searchKey = new DatabaseEntry();
//		DatabaseEntry foundVal = new DatabaseEntry();
//		StringBinding.stringToEntry(key, searchKey);
//		Cursor cursor = crtDb.openCursor(null, null);
//
//		while (cursor.getNextDup(searchKey, foundVal, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
//			String value = StringBinding.entryToString(foundVal);
//			valueList.add(value);
//		}
//		cursor.close();
//
//		return valueList;
//	}
	
	public HashMap<String, ArrayList<String>> getAllGroup(String id) {
		Database crtDb = m_reduceBoltDb.get(id);
		HashMap<String, ArrayList<String>> groups = new HashMap<String, ArrayList<String>>();
		Cursor cursor = crtDb.openCursor(null, null);
		DatabaseEntry foundKey = new DatabaseEntry();
		DatabaseEntry foundVal = new DatabaseEntry();
		EntryBinding dataBinding = new SerialBinding(m_classCatalog, SerializedList.class);
		
		while (cursor.getNext(foundKey, foundVal, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			String key = StringBinding.entryToString(foundKey);
			SerializedList values = (SerializedList)dataBinding.entryToObject(foundVal);
			groups.put(key, values.arr);
			//String value = StringBinding.entryToString(foundVal);
			
//			ArrayList<String> values = groups.get(key);
//			if (values == null) {
//				values = new ArrayList<String>();
//				values.add(value);
//				groups.put(key, values);
//			} else {
//				System.out.println("SAME KEY");
//				values.add(value);
//			}
		}
		cursor.close();
		return groups;
	}
}
