package edu.upenn.cis455.webserver.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.sleepycat.bind.tuple.ByteBinding;
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

import edu.upenn.cis455.crawler.PageInfo;

import com.sleepycat.bind.ByteArrayBinding;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;

public class MyDbEnv {
	private Environment m_env;

	private Database m_accountDb;
	private Database m_fileDb;
	private Database m_contentHashDb;
	private MessageDigest m_messageDigest;
	private StoredClassCatalog m_classCatalog;
	private Database m_classCatalogDb;
	private Database m_pageInfoDb;
	//private long m_fakeRabinFingerprint = 0;
	
	public MyDbEnv() {

	}

	public void setup(File envHome, boolean readOnly) throws DatabaseException, NoSuchAlgorithmException {
		EnvironmentConfig myEnvConfig = new EnvironmentConfig();
		DatabaseConfig myDbConfig = new DatabaseConfig();
		
		// this can enable storing duplicated values
		//myDbConfig.setSortedDuplicates(true);
		
		myEnvConfig.setReadOnly(readOnly);
		myDbConfig.setReadOnly(readOnly);

		myEnvConfig.setAllowCreate(!readOnly);
		myDbConfig.setAllowCreate(!readOnly);

		myEnvConfig.setTransactional(!readOnly);
		myDbConfig.setTransactional(!readOnly);

		m_env = new Environment(envHome, myEnvConfig);
		m_accountDb = m_env.openDatabase(null, "AccountDB", myDbConfig);
		m_fileDb = m_env.openDatabase(null, "FileDB", myDbConfig);
		m_contentHashDb = m_env.openDatabase(null, "ContentHashDB", myDbConfig);
		m_pageInfoDb = m_env.openDatabase(null, "PageInfoDB", myDbConfig);
		
		// class catalog
		m_classCatalogDb = m_env.openDatabase(null, "ClassCatalogDB", myDbConfig);
		m_classCatalog = new StoredClassCatalog(m_classCatalogDb);
		
		m_messageDigest = MessageDigest.getInstance("MD5");
	}
	
	// store a MD5 digest for content-seen test
	public boolean insertContent(String content) throws UnsupportedEncodingException {
		//System.out.println(content.length());
		if (isContentExist(content))
			return false;
		byte[] key = m_messageDigest.digest(content.getBytes());
		//key = m_messageDigest.digest("SADASDAS".getBytes());
		//System.out.println(Arrays.toString(key));
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry valueEntry = new DatabaseEntry();
		ByteArrayBinding byteArrayBinding = new ByteArrayBinding();
		
		byteArrayBinding.objectToEntry(key, keyEntry);
		byteArrayBinding.objectToEntry(key, valueEntry);
		Transaction txn = m_env.beginTransaction(null, null);
		OperationStatus status = m_contentHashDb.put(txn, keyEntry, valueEntry);
		if (status != OperationStatus.SUCCESS) {
			throw new RuntimeException("ContentHashDB Data insertion got status " + status);
		}
		txn.commit();
		return true;
	}
	
	public boolean isContentExist(String content) {
		byte[] key = m_messageDigest.digest(content.getBytes());
		//System.out.println(Arrays.toString(key));
		DatabaseEntry searchKey = new DatabaseEntry();
		DatabaseEntry foundVal = new DatabaseEntry();
		ByteArrayBinding byteArrayBinding = new ByteArrayBinding();
		byteArrayBinding.objectToEntry(key, searchKey);
		
		Cursor cursor = m_contentHashDb.openCursor(null, null);
		OperationStatus retVal = cursor.getSearchKey(searchKey, foundVal, LockMode.DEFAULT);
		if (retVal == OperationStatus.SUCCESS) {
			return true;
		}
		return false;
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

		StringBinding.stringToEntry(usr, keyEntry);
		StringBinding.stringToEntry(psw, dataEntry);
		Transaction txn = m_env.beginTransaction(null, null);
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
	
	// private function, only called by insertPage()
	private void insertPageInfo(String key, PageInfo info) {
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();
		
		Transaction txn = m_env.beginTransaction(null, null);
        EntryBinding dataBinding = new SerialBinding(m_classCatalog, PageInfo.class);
		StringBinding.stringToEntry(key, keyEntry);
		dataBinding.objectToEntry(info, dataEntry);
		OperationStatus status = m_pageInfoDb.put(txn, keyEntry, dataEntry);
		if (status != OperationStatus.SUCCESS) {
			throw new RuntimeException("FileDB Data insertion got status " + status);
		}
		txn.commit();
	}

	public PageInfo getPageInfo(String url) {
		UUID key = UUID.nameUUIDFromBytes(url.getBytes());
		//System.out.println(key);
		DatabaseEntry searchKey = new DatabaseEntry();
		DatabaseEntry foundVal = new DatabaseEntry();
        EntryBinding dataBinding = new SerialBinding(m_classCatalog, PageInfo.class);
		StringBinding.stringToEntry(key.toString(), searchKey);
		Cursor cursor = m_pageInfoDb.openCursor(null, null);
		OperationStatus retVal = cursor.getSearchKey(searchKey, foundVal, LockMode.DEFAULT);
		if (retVal == OperationStatus.SUCCESS) {
			PageInfo p = (PageInfo)dataBinding.entryToObject(foundVal);
			//System.out.println(p.getLastModified());
			return p;
		}
		return null;
	}
	
	public void insertPage(String url, String content, PageInfo info) {
		UUID keyId = UUID.nameUUIDFromBytes(url.getBytes());
		String key = keyId.toString();
		
		// insert PageInfo
		insertPageInfo(key, info);
	
		// insert content
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();
		Transaction txn = m_env.beginTransaction(null, null);
		StringBinding.stringToEntry(key, keyEntry);
		StringBinding.stringToEntry(content, dataEntry);
		OperationStatus status = m_fileDb.put(txn, keyEntry, dataEntry);
		if (status != OperationStatus.SUCCESS) {
			throw new RuntimeException("FileDB Data insertion got status " + status);
		}
		txn.commit();
		//System.out.println("SUCCESS");
		
	}
	
	public String getPageContent(String url) {
		UUID key = UUID.nameUUIDFromBytes(url.getBytes());
		//System.out.println(key);
		DatabaseEntry searchKey = new DatabaseEntry();
		DatabaseEntry foundVal = new DatabaseEntry();
		StringBinding.stringToEntry(key.toString(), searchKey);
		Cursor cursor = m_fileDb.openCursor(null, null);
		OperationStatus retVal = cursor.getSearchKey(searchKey, foundVal, LockMode.DEFAULT);
//		cursor.getNext(searchKey, foundVal, LockMode.DEFAULT);
//		cursor.getNext(searchKey, foundVal, LockMode.DEFAULT);
		if (retVal == OperationStatus.SUCCESS)
			return StringBinding.entryToString(foundVal);
		return null;
	}
}
