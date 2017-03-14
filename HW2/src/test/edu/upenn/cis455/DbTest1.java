package test.edu.upenn.cis455;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.junit.After;
import org.junit.Before;

import com.sleepycat.je.DatabaseException;

import edu.upenn.cis455.webserver.utils.MyDbEnv;
import edu.upenn.cis455.webserver.utils.PageInfo;
import junit.framework.TestCase;

public class DbTest1 extends TestCase {
	private MyDbEnv m_dbEnv;
	private PageInfo m_info;
	private long m_time;
	protected void setUp() throws DatabaseException, NoSuchAlgorithmException, UnsupportedEncodingException {
		m_dbEnv = new MyDbEnv();
		m_dbEnv.setup(new File("database"), false);
		m_time = 1234L;
		m_info = new PageInfo(m_time);
		m_dbEnv.insertPage("http://abc.com", "Fake content", m_info);
	}
	
	public void testPageInfo() {
		assertEquals(m_time, m_dbEnv.getPageInfo("http://abc.com").getLastModified());
	}
}
