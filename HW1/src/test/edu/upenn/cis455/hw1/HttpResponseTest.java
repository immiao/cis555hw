package test.edu.upenn.cis455.hw1;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import edu.upenn.cis455.webserver.utils.MyHttpServletResponse;
import junit.framework.TestCase;

public class HttpResponseTest extends TestCase {
	private MyHttpServletResponse resp;
	
	protected void setUp() {
		resp = new MyHttpServletResponse(null, 8192);
	}
	
	public void testContentType() {
		resp.setContentType("image/gif");
		assertEquals("image/gif", resp.getContentType());
	}
	
	public void testCharset() {
		resp.setCharacterEncoding("UTF-8");
		assertEquals("UTF-8", resp.getCharacterEncoding());
	}
	
	public void testBufferSize() {
		resp.setBufferSize(1024);
		assertEquals(1024, resp.getBufferSize());
	}
	
	public void testEncodedRedirectURL() {
		try {
			assertEquals(resp.encodeRedirectURL("http://www.apple.com"), URLEncoder.encode("http://www.apple.com", "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testEncodedURL() {
		try {
			assertEquals(resp.encodeRedirectURL("http://www.apple.com"), URLEncoder.encode("http://www.apple.com", "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
