package test.edu.upenn.cis455;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.junit.After;
import org.junit.Before;

import com.sleepycat.je.DatabaseException;

import edu.upenn.cis455.webserver.utils.MyDbEnv;
import junit.framework.TestCase;

public class DbTest0 extends TestCase {
	private MyDbEnv m_dbEnv;
	private String usr;
	private String psw;
	private String content;
	
	protected void setUp() throws DatabaseException, NoSuchAlgorithmException, UnsupportedEncodingException {
		m_dbEnv = new MyDbEnv();
		m_dbEnv.setup(new File("database"), false);
		usr = new String("abc");
		psw = new String("123");
		content = new String("Fake Content");
		m_dbEnv.nonTransactionalInsertAccount(usr, psw);
		m_dbEnv.nonTransactionalInsertContent(content);
	}
	
	public void testAccount() {
		assertEquals(psw, m_dbEnv.getPsw(usr));
	}
	
	
	public void testContentSeen() throws UnsupportedEncodingException {
		assertEquals(true, m_dbEnv.isContentExist(content));
	}
}
