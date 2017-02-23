package test.edu.upenn.cis455.hw1;

import edu.upenn.cis455.webserver.utils.MyHttpSession;
import junit.framework.TestCase;

public class HttpSessionTest extends TestCase {
	private MyHttpSession session;
	protected void setUp() {
		session = new MyHttpSession(null, null);
	}
	
	public void testMaxInactiveInterval() {
		session.setMaxInactiveInterval(20);
		assertEquals(20, session.getMaxInactiveInterval());
	}
	
	public void testAttribute() {
		session.setAttribute("key", "value");
		assertEquals("value", session.getAttribute("key"));
	}
}
