package test.edu.upenn.cis455.hw1;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Vector;

import edu.upenn.cis455.webserver.utils.MyHttpServletRequest;
import junit.framework.TestCase;

public class HttpRequestTest extends TestCase {
	private MyHttpServletRequest req;

	
	protected void setUp() {
		HashMap<String, Vector<String>> headerMap = new HashMap<String, Vector<String>>();
		HashMap<String, Vector<String>> paramsMap = new HashMap<String, Vector<String>>();
		
		req = new MyHttpServletRequest(null, "GET", headerMap, paramsMap, "Web Server", "HTTP/1.1",
				null, null, null, "key=value&otherKey=otherValue", "/demo", "http://localhost:8080", 
				"0.0.0.0", 8080, "1.1.1.1",
				1234, null, null);
	}
	
	public void testCharset() {
		try {
			req.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals("UTF-8", req.getCharacterEncoding());
	}
	public void testMethod() {
		assertEquals("GET", req.getMethod());
	}
	
	public void testServerName() {
		assertEquals("Web Server", req.getServerName());
	}
	
	public void testProtocol() {
		assertEquals("HTTP/1.1", req.getProtocol());
	}
	
	public void testURI() {
		assertEquals("/demo", req.getRequestURI());
	}
	
	public void testURL() {
		assertEquals("http://localhost:8080", req.getRequestURL().toString());
	}
	
	public void testLocalAddr() {
		assertEquals("0.0.0.0", req.getLocalAddr());
	}
	
	public void testLocalPort() {
		assertEquals(8080, req.getLocalPort());
	}
	
	public void testRemoteAddr() {
		assertEquals("1.1.1.1", req.getRemoteAddr());
	}
	
	public void testRemotePort() {
		assertEquals(1234, req.getRemotePort());
	}
	
	public void testQueryString() {
		assertEquals("key=value&otherKey=otherValue", req.getQueryString());
	}
}
