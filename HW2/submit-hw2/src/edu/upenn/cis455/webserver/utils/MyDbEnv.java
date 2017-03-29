package edu.upenn.cis455.webserver.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
import com.sleepycat.je.Durability;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.Transaction;

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
	private Database m_channelDb;
	// private long m_fakeRabinFingerprint = 0;

	public MyDbEnv() {
		
	}

	public void setup(File envHome, boolean readOnly) throws DatabaseException, NoSuchAlgorithmException {
		EnvironmentConfig myEnvConfig = new EnvironmentConfig();
		DatabaseConfig myDbConfig = new DatabaseConfig();

		// this can enable storing duplicated values
		// myDbConfig.setSortedDuplicates(true);

		myEnvConfig.setReadOnly(readOnly);
		myDbConfig.setReadOnly(readOnly);

		myEnvConfig.setAllowCreate(!readOnly);
		myDbConfig.setAllowCreate(!readOnly);
		
		myEnvConfig.setTransactional(!readOnly);
		myDbConfig.setTransactional(!readOnly);
		
		//myEnvConfig.setDurability(Durability.COMMIT_SYNC);
		//System.out.println("YYYY: " + myEnvConfig.getDurability());
		
		m_env = new Environment(envHome, myEnvConfig);
		m_accountDb = m_env.openDatabase(null, "AccountDB", myDbConfig);
		m_fileDb = m_env.openDatabase(null, "FileDB", myDbConfig);
		m_contentHashDb = m_env.openDatabase(null, "ContentHashDB", myDbConfig);
		m_pageInfoDb = m_env.openDatabase(null, "PageInfoDB", myDbConfig);
		m_channelDb = m_env.openDatabase(null, "ChannelDB", myDbConfig);

		// class catalog
		m_classCatalogDb = m_env.openDatabase(null, "ClassCatalogDB", myDbConfig);
		m_classCatalog = new StoredClassCatalog(m_classCatalogDb);

		m_messageDigest = MessageDigest.getInstance("MD5");
	}

	public ArrayList<ChannelInfo> getAllChannel() {
		Cursor cursor = m_channelDb.openCursor(null, null);
		ArrayList<ChannelInfo> channels = new ArrayList<ChannelInfo>();
		DatabaseEntry foundKey = new DatabaseEntry();
		DatabaseEntry foundVal = new DatabaseEntry();
		EntryBinding dataBinding = new SerialBinding(m_classCatalog, ChannelInfo.class);
		
		while (cursor.getNext(foundKey, foundVal, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			channels.add((ChannelInfo) dataBinding.entryToObject(foundVal));
		}
		cursor.close();
		return channels;
	}
	
	public void addUrlToChannel(String channelName, String url) {
		ChannelInfo info = getChannelInfo(channelName);
		if (!info.url.contains(url))
			info.url.add(url);
		updateChannelInfo(channelName, info);
	}
	
	private ChannelInfo getChannelInfo(String name) {
		DatabaseEntry searchKey = new DatabaseEntry();
		DatabaseEntry foundVal = new DatabaseEntry();
		StringBinding.stringToEntry(name, searchKey);
		Cursor cursor = m_channelDb.openCursor(null, null);
		OperationStatus retVal = cursor.getSearchKey(searchKey, foundVal, LockMode.DEFAULT);
		EntryBinding dataBinding = new SerialBinding(m_classCatalog, ChannelInfo.class);
		cursor.close();
		if (retVal != OperationStatus.SUCCESS) {
			System.out.println("Channel doesn't exist! Delete failed!");
			return null;
		}
		ChannelInfo channel = (ChannelInfo) dataBinding.entryToObject(foundVal);
//		System.out.println("HHHHHHHHH");
//		for (String s : channel.subscribedUserName) {
//			System.out.println(s);
//		}
//		System.out.println("NNNNNNN");
//		return channel;
		return channel;
	}
	
	public UserInfo getUserInfo(String usr) {
		DatabaseEntry searchKey = new DatabaseEntry();
		DatabaseEntry foundVal = new DatabaseEntry();
		StringBinding.stringToEntry(usr, searchKey);
		Cursor cursor = m_accountDb.openCursor(null, null);
		OperationStatus retVal = cursor.getSearchKey(searchKey, foundVal, LockMode.DEFAULT);
		cursor.close();
		EntryBinding dataBinding = new SerialBinding(m_classCatalog, UserInfo.class);

		if (retVal == OperationStatus.SUCCESS) {
			return (UserInfo) dataBinding.entryToObject(foundVal);
		} else {
			System.out.println("No such user exists! Get userinfo failed!");
			return null;
		}
	}
	
	public void updateChannelInfo(Transaction txn, String name, ChannelInfo info) {
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();
		EntryBinding dataBinding = new SerialBinding(m_classCatalog, ChannelInfo.class);

		StringBinding.stringToEntry(name, keyEntry);
		// StringBinding.stringToEntry(psw, dataEntry);
		//ChannelInfo temp = new ChannelInfo();
		//dataBinding.objectToEntry(temp, dataEntry);
		dataBinding.objectToEntry(info, dataEntry);
		//txn.setLockTimeout(50, TimeUnit.SECONDS);
		OperationStatus status = m_channelDb.put(txn, keyEntry, dataEntry);
		if (status != OperationStatus.SUCCESS) {
			txn.abort();
			throw new RuntimeException("ChannelDB Data insertion got status " + status);
		}
		//m_env.sync();
		System.out.println("Update channel info");
	}
	
	public void updateUserInfo(Transaction txn, String usr, UserInfo info) {
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();
		EntryBinding dataBinding = new SerialBinding(m_classCatalog, UserInfo.class);

		StringBinding.stringToEntry(usr, keyEntry);
		// StringBinding.stringToEntry(psw, dataEntry);
		dataBinding.objectToEntry(info, dataEntry);
		OperationStatus status = m_accountDb.put(txn, keyEntry, dataEntry);
		if (status != OperationStatus.SUCCESS) {
			txn.abort();
			throw new RuntimeException("AccountDB Data insertion got status " + status);
		}
		//m_env.sync();
		System.out.println("Update user info");
	}
	
	public void updateChannelInfo(String name, ChannelInfo info) {
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();
		EntryBinding dataBinding = new SerialBinding(m_classCatalog, ChannelInfo.class);

		StringBinding.stringToEntry(name, keyEntry);
		// StringBinding.stringToEntry(psw, dataEntry);
		//ChannelInfo temp = new ChannelInfo();
		//dataBinding.objectToEntry(temp, dataEntry);
		dataBinding.objectToEntry(info, dataEntry);
		
		Transaction txn = m_env.beginTransaction(null, null);
		//txn.setLockTimeout(50, TimeUnit.SECONDS);
		OperationStatus status = m_channelDb.put(txn, keyEntry, dataEntry);
		if (status != OperationStatus.SUCCESS) {
			txn.abort();
			throw new RuntimeException("ChannelDB Data insertion got status " + status);
		}
		txn.commit();
		//m_channelDb.sync();
		//m_env.sync();
		System.out.println("Update channel info");
	}
	
	public void updateUserInfo(String usr, UserInfo info) {
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();
		EntryBinding dataBinding = new SerialBinding(m_classCatalog, UserInfo.class);

		StringBinding.stringToEntry(usr, keyEntry);
		// StringBinding.stringToEntry(psw, dataEntry);
		dataBinding.objectToEntry(info, dataEntry);
		Transaction txn = m_env.beginTransaction(null, null);
		OperationStatus status = m_accountDb.put(txn, keyEntry, dataEntry);
		if (status != OperationStatus.SUCCESS) {
			txn.abort();
			throw new RuntimeException("AccountDB Data insertion got status " + status);
		}
		
		txn.commit();
		//m_env.sync();
		System.out.println("Update user info");
	}
	
	public ChannelInfo show(String usr, String xpathName) {
		// should succeed
		UserInfo usrInfo = getUserInfo(usr);
		if (usrInfo != null) {
			if (!usrInfo.subscribed.contains(xpathName))
				return null;
			
			return getChannelInfo(xpathName);
		} else {
			System.out.println("No such user exists! Show failed!");
			return null;
		}
	}

	public int delete(String usr, String xpathName) {
		// check if channel exists
		DatabaseEntry searchKey = new DatabaseEntry();
		DatabaseEntry foundVal = new DatabaseEntry();
		StringBinding.stringToEntry(xpathName, searchKey);
		Transaction txn = m_env.beginTransaction(null, null);
		Cursor cursor = m_channelDb.openCursor(txn, null);
		OperationStatus retVal = cursor.getSearchKey(searchKey, foundVal, LockMode.DEFAULT);
		EntryBinding dataBinding = new SerialBinding(m_classCatalog, ChannelInfo.class);
		if (retVal != OperationStatus.SUCCESS) {
			System.out.println("Channel doesn't exist! Delete failed!");
			return -1;
		}
		ChannelInfo channel = (ChannelInfo) dataBinding.entryToObject(foundVal);

		// the channel doesn't belong to this user
		if (!usr.equals(channel.userName))
			return -2;

		OperationStatus status = null;
		
		// update user's subscribed channel list
		for (String username : channel.subscribedUserName) {
			UserInfo info = getUserInfo(username);
			if (info == null) {
				System.out.println("Username doesn't exist!");
				return -1;
			}
			if (!info.subscribed.remove(channel.name)) {
				System.out.println("Subscribed list doesn't contain the channel");
				return -1;
			}
			updateUserInfo(username, info);
		}
		
		// remove channel
		status = cursor.delete();
		cursor.close();
		if (status != OperationStatus.SUCCESS) {
			txn.abort();
			throw new RuntimeException("Delete channel got status " + status);
		}
		txn.commit();
		return 0;
	}

	public boolean unsubscribe(String usr, String xpathName) {
		UserInfo usrInfo = getUserInfo(usr);
		
		// delete the subscribed channel name from the user
		if (usrInfo != null) {
			if (!usrInfo.subscribed.remove(xpathName))
				return false;
			updateUserInfo(usr, usrInfo);
		} else {
			System.out.println("No such user exists! Unsubscribe failed!");
			return false;
		}
		
		// delete user name from the subscribed username list in the channel
		ChannelInfo channelInfo = getChannelInfo(xpathName);
		if (channelInfo != null) {
			if (!channelInfo.subscribedUserName.remove(usr))
				return false;
			updateChannelInfo(channelInfo.name, channelInfo);
		}else {
			System.out.println("No such channel exists! Unsubscribed failed!");
			return false;
		}
		return true;
	}

	public int subscribe(String usr, String xpathName) {
		// check if channel exists
		//ChannelInfo howareyou = getChannelInfo(xpathName);
		ChannelInfo channel = getChannelInfo(xpathName);
//		ChannelInfo channel = new ChannelInfo();
//		channel.name = "test";
//		ChannelInfo newChannel = new ChannelInfo(channel);
//		newChannel.subscribedUserName.add(usr);
//		updateChannelInfo(channel.name, channel);
//		return 0;
		if (channel == null) {
			System.out.println("No such channel exists! Subscirbe failed!");
			return -1;
		}
		
		UserInfo usrInfo = getUserInfo(usr);
		// should succeed
		if (usrInfo != null) {
			if (usrInfo.subscribed.contains(xpathName))
				return -2;
			usrInfo.subscribed.add(xpathName);
			
			updateUserInfo(usr, usrInfo);
			channel.subscribedUserName.add(usr);
			updateChannelInfo(channel.name, channel);
			
//			Transaction txn = m_env.beginTransaction(null, null);
//			updateUserInfo(txn, usr, usrInfo);
//			channel.subscribedUserName.add(usr);
//			updateChannelInfo(txn, channel.name, channel);
//			txn.commit();
		} else {
			System.out.println("No such user exists! Subscribe failed!");
			return -1;
		}
		return 0;
	}

	// store a MD5 digest for content-seen test
	public boolean insertContent(String content) throws UnsupportedEncodingException {
		// System.out.println(content.length());
		if (isContentExist(content))
			return false;
		byte[] key = m_messageDigest.digest(content.getBytes());
		// key = m_messageDigest.digest("SADASDAS".getBytes());
		// System.out.println(Arrays.toString(key));
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry valueEntry = new DatabaseEntry();
		ByteArrayBinding byteArrayBinding = new ByteArrayBinding();

		byteArrayBinding.objectToEntry(key, keyEntry);
		byteArrayBinding.objectToEntry(key, valueEntry);
		Transaction txn = m_env.beginTransaction(null, null);
		OperationStatus status = m_contentHashDb.put(txn, keyEntry, valueEntry);
		if (status != OperationStatus.SUCCESS) {
			txn.abort();
			throw new RuntimeException("ContentHashDB Data insertion got status " + status);
		}
		txn.commit();
		return true;
	}

	public boolean isContentExist(String content) {
		byte[] key = m_messageDigest.digest(content.getBytes());
		// System.out.println(Arrays.toString(key));
		DatabaseEntry searchKey = new DatabaseEntry();
		DatabaseEntry foundVal = new DatabaseEntry();
		ByteArrayBinding byteArrayBinding = new ByteArrayBinding();
		byteArrayBinding.objectToEntry(key, searchKey);

		Cursor cursor = m_contentHashDb.openCursor(null, null);
		OperationStatus retVal = cursor.getSearchKey(searchKey, foundVal, LockMode.DEFAULT);
		cursor.close();
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
		cursor.close();
		if (retVal == OperationStatus.SUCCESS) {
			System.out.println("Username already exists! Create failed!");
			return;
		}

		// if not, insert the new account
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();
		EntryBinding dataBinding = new SerialBinding(m_classCatalog, UserInfo.class);
		UserInfo info = new UserInfo();
		info.psw = psw;

		StringBinding.stringToEntry(usr, keyEntry);
		// StringBinding.stringToEntry(psw, dataEntry);
		dataBinding.objectToEntry(info, dataEntry);
		Transaction txn = m_env.beginTransaction(null, null);
		OperationStatus status = m_accountDb.put(txn, keyEntry, dataEntry);
		if (status != OperationStatus.SUCCESS) {
			txn.abort();
			throw new RuntimeException("AccountDB Data insertion got status " + status);
		}
		txn.commit();
	}
	public boolean insertChannel(String usr, String xpathName, String xpath) {
		// check if channel already exists
		DatabaseEntry searchKey = new DatabaseEntry();
		DatabaseEntry foundVal = new DatabaseEntry();
		StringBinding.stringToEntry(xpathName, searchKey);
		Cursor cursor = m_channelDb.openCursor(null, null);
		OperationStatus retVal = cursor.getSearchKey(searchKey, foundVal, LockMode.DEFAULT);
		cursor.close();
		if (retVal == OperationStatus.SUCCESS) {
			System.out.println("Channel already exists! Create failed!");
			return false;
		}

		// insert new channel
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();
		EntryBinding dataBinding = new SerialBinding(m_classCatalog, ChannelInfo.class);
		ChannelInfo info = new ChannelInfo();
		info.userName = usr;
		info.name = xpathName;
		info.xpath = xpath;
		StringBinding.stringToEntry(xpathName, keyEntry);
		dataBinding.objectToEntry(info, dataEntry);
		Transaction txn = m_env.beginTransaction(null, null);
		OperationStatus status = m_channelDb.put(txn, keyEntry, dataEntry);
		if (status != OperationStatus.SUCCESS) {
			txn.abort();
			throw new RuntimeException("ChannelDB Data insertion got status " + status);
		}
		txn.commit();
		//m_channelDb.sync();

//		// get user info from the database
//		searchKey = new DatabaseEntry();
//		foundVal = new DatabaseEntry();
//		StringBinding.stringToEntry(usr, searchKey);
//		cursor = m_accountDb.openCursor(null, null);
//		retVal = cursor.getSearchKey(searchKey, foundVal, LockMode.DEFAULT);
//		cursor.close();
//		dataBinding = new SerialBinding(m_classCatalog, UserInfo.class);
//
//		UserInfo usrInfo = null;
//		// should succeed
//		if (retVal == OperationStatus.SUCCESS) {
//			usrInfo = (UserInfo) dataBinding.entryToObject(foundVal);
//
//			// update user's owned channel list
//			keyEntry = new DatabaseEntry();
//			dataEntry = new DatabaseEntry();
//			dataBinding = new SerialBinding(m_classCatalog, UserInfo.class);
//
//			StringBinding.stringToEntry(usr, keyEntry);
//			dataBinding.objectToEntry(usrInfo, dataEntry);
//			txn = m_env.beginTransaction(null, null);
//			status = m_accountDb.put(txn, keyEntry, dataEntry);
//			if (status != OperationStatus.SUCCESS) {
//				txn.abort();
//				throw new RuntimeException("AccountDB Data insertion got status " + status);
//			}
//			txn.commit();
//		} else {
//			System.out.println("No such user exists! Create failed!");
//			return false;
//		}
		return true;
	}
	public String getPsw(String usr) {
		DatabaseEntry searchKey = new DatabaseEntry();
		DatabaseEntry foundVal = new DatabaseEntry();
		StringBinding.stringToEntry(usr, searchKey);
		Cursor cursor = m_accountDb.openCursor(null, null);
		OperationStatus retVal = cursor.getSearchKey(searchKey, foundVal, LockMode.DEFAULT);
		cursor.close();
		EntryBinding dataBinding = new SerialBinding(m_classCatalog, UserInfo.class);

		if (retVal == OperationStatus.SUCCESS) {
			UserInfo info = (UserInfo) dataBinding.entryToObject(foundVal);
			return info.psw;
		}
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
			txn.abort();
			throw new RuntimeException("FileDB Data insertion got status " + status);
		}
		txn.commit();
	}
	
	public PageInfo getPageInfo(String url) {
		UUID key = UUID.nameUUIDFromBytes(url.getBytes());
		// System.out.println(key);
		DatabaseEntry searchKey = new DatabaseEntry();
		DatabaseEntry foundVal = new DatabaseEntry();
		EntryBinding dataBinding = new SerialBinding(m_classCatalog, PageInfo.class);
		StringBinding.stringToEntry(key.toString(), searchKey);
		Cursor cursor = m_pageInfoDb.openCursor(null, null);
		OperationStatus retVal = cursor.getSearchKey(searchKey, foundVal, LockMode.DEFAULT);
		cursor.close();
		if (retVal == OperationStatus.SUCCESS) {
			PageInfo p = (PageInfo) dataBinding.entryToObject(foundVal);
			// System.out.println(p.getLastModified());
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
			txn.abort();
			throw new RuntimeException("FileDB Data insertion got status " + status);
		}
		txn.commit();
		// System.out.println("SUCCESS");

	}

	public String getPageContent(String url) {
		UUID key = UUID.nameUUIDFromBytes(url.getBytes());
		// System.out.println(key);
		DatabaseEntry searchKey = new DatabaseEntry();
		DatabaseEntry foundVal = new DatabaseEntry();
		StringBinding.stringToEntry(key.toString(), searchKey);
		Cursor cursor = m_fileDb.openCursor(null, null);
		OperationStatus retVal = cursor.getSearchKey(searchKey, foundVal, LockMode.DEFAULT);
		// cursor.getNext(searchKey, foundVal, LockMode.DEFAULT);
		// cursor.getNext(searchKey, foundVal, LockMode.DEFAULT);
		if (retVal == OperationStatus.SUCCESS)
			return StringBinding.entryToString(foundVal);
		return null;
	}

	public void nonTransactionalInsertAccount(String usr, String psw) {

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
		// Transaction txn = m_env.beginTransaction(null, null);
		OperationStatus status = m_accountDb.put(null, keyEntry, dataEntry);
		if (status != OperationStatus.SUCCESS) {
			throw new RuntimeException("AccountDB Data insertion got status " + status);
		}
		// txn.commit();
	}

	// store a MD5 digest for content-seen test
	public boolean nonTransactionalInsertContent(String content) throws UnsupportedEncodingException {
		// System.out.println(content.length());
		if (isContentExist(content))
			return false;
		byte[] key = m_messageDigest.digest(content.getBytes());
		// key = m_messageDigest.digest("SADASDAS".getBytes());
		// System.out.println(Arrays.toString(key));
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry valueEntry = new DatabaseEntry();
		ByteArrayBinding byteArrayBinding = new ByteArrayBinding();

		byteArrayBinding.objectToEntry(key, keyEntry);
		byteArrayBinding.objectToEntry(key, valueEntry);
		// Transaction txn = m_env.beginTransaction(null, null);
		OperationStatus status = m_contentHashDb.put(null, keyEntry, valueEntry);
		if (status != OperationStatus.SUCCESS) {
			throw new RuntimeException("ContentHashDB Data insertion got status " + status);
		}
		// txn.commit();
		return true;
	}

	public void nonTransactionalInsertPage(String url, String content, PageInfo info) {
		UUID keyId = UUID.nameUUIDFromBytes(url.getBytes());
		String key = keyId.toString();

		// insert PageInfo
		insertPageInfo(key, info);

		// insert content
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();
		// Transaction txn = m_env.beginTransaction(null, null);
		StringBinding.stringToEntry(key, keyEntry);
		StringBinding.stringToEntry(content, dataEntry);
		OperationStatus status = m_fileDb.put(null, keyEntry, dataEntry);
		if (status != OperationStatus.SUCCESS) {
			throw new RuntimeException("FileDB Data insertion got status " + status);
		}
		// txn.commit();
		// System.out.println("SUCCESS");

	}
}
