package edu.upenn.cis455.webserver.utils;

import java.io.File;
import java.util.UUID;

import com.sleepycat.bind.tuple.LongBinding;
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
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.Transaction;

public class MyDbEnv {
	private Environment m_env;

	private Database m_accountDb;
	private Database m_fileDb;
	//private long m_fakeRabinFingerprint = 0;
	
	public MyDbEnv() {

	}

	public void setup(File envHome, boolean readOnly) throws DatabaseException {
		EnvironmentConfig myEnvConfig = new EnvironmentConfig();
		DatabaseConfig myDbConfig = new DatabaseConfig();

		myEnvConfig.setReadOnly(readOnly);
		myDbConfig.setReadOnly(readOnly);

		myEnvConfig.setAllowCreate(!readOnly);
		myDbConfig.setAllowCreate(!readOnly);

		myEnvConfig.setTransactional(!readOnly);
		myDbConfig.setTransactional(!readOnly);

		m_env = new Environment(envHome, myEnvConfig);
		m_accountDb = m_env.openDatabase(null, "AccountDB", myDbConfig);
		m_fileDb = m_env.openDatabase(null, "FileDB", myDbConfig);
	}

	public void insertAccount(String usr, String psw) {

		// check if username already exists
		DatabaseEntry searchKey = new DatabaseEntry();
		DatabaseEntry foundVal = new DatabaseEntry();
		StringBinding.stringToEntry(usr, searchKey);
		Cursor cursor = m_accountDb.openCursor(null, null);
		OperationStatus retVal = cursor.getSearchKey(searchKey, foundVal, LockMode.DEFAULT);
		if (retVal == OperationStatus.SUCCESS) {
			System.out.println("Username already exists! Create failed!");
			return;
		}

		// if not, insert the new account
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();

		Transaction txn = m_env.beginTransaction(null, null);
		StringBinding.stringToEntry(usr, keyEntry);
		StringBinding.stringToEntry(psw, dataEntry);
		OperationStatus status = m_accountDb.put(txn, keyEntry, dataEntry);
		if (status != OperationStatus.SUCCESS) {
			throw new RuntimeException("AccountDB Data insertion got status " + status);
		}
		txn.commit();
	}

	public String getPsw(String usr) {
		DatabaseEntry searchKey = new DatabaseEntry();
		DatabaseEntry foundVal = new DatabaseEntry();
		StringBinding.stringToEntry(usr, searchKey);
		Cursor cursor = m_accountDb.openCursor(null, null);
		OperationStatus retVal = cursor.getSearchKey(searchKey, foundVal, LockMode.DEFAULT);
		if (retVal == OperationStatus.SUCCESS)
			return StringBinding.entryToString(foundVal);
		return null;
	}

	public void insertFile(String url, String content) {
		UUID key = UUID.fromString(url);
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();
		
		Transaction txn = m_env.beginTransaction(null, null);
		StringBinding.stringToEntry(key.toString(), keyEntry);
		StringBinding.stringToEntry(content, dataEntry);
		OperationStatus status = m_fileDb.put(txn, keyEntry, dataEntry);
		if (status != OperationStatus.SUCCESS) {
			throw new RuntimeException("FileDB Data insertion got status " + status);
		}
		txn.commit();
	}
	
	public String getFile(String url) {
		UUID key = UUID.fromString(url);
		DatabaseEntry searchKey = new DatabaseEntry();
		DatabaseEntry foundVal = new DatabaseEntry();
		StringBinding.stringToEntry(key.toString(), searchKey);
		Cursor cursor = m_fileDb.openCursor(null, null);
		OperationStatus retVal = cursor.getSearchKey(searchKey, foundVal, LockMode.DEFAULT);
		if (retVal == OperationStatus.SUCCESS)
			return StringBinding.entryToString(foundVal);
		return null;
	}
}
