package test.edu.upenn.cis455;


import edu.upenn.cis455.crawler.XPathCrawler;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XPathEngineTest extends TestCase {
	private XPathEngineImpl m_xpathEngine;
	
	protected void setUp() {
		m_xpathEngine = (XPathEngineImpl) XPathEngineFactory.getXPathEngine();
	}
	
	public void testValidXml() throws ParserConfigurationException, SAXException, IOException {
		String xml = new String();
		xml += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xml += "<note>";
		xml += "<to>text()</to>";
		xml += "<to src=\"abc.gif\" date=\"12/11/1993\">Tove</to>";
		xml += "<from>Jani</from>";
		xml += "<heading>Reminder</heading>";
		xml += "<body>Don't forget me this weekend!</body>";
		xml += "<div><from att1=\"test\"><heading headingAtt=\"test\">Reminder</heading></from></div>";
		xml += "</note>";
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = f.newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
		String[] xpath = {"/note/to",
				"/note/to[text() = \"text()\"]",
				"/note/to[@att=\"src\"]",
				"/note/to[@att=\"src\"][@att=\"date\"]",
				"/note/body[text()=\"Don't forget me this weekend!\"]",
				"/note/body[contains(text(), \"forget\")]",
				"/note/div[from[@att=\"att1\"]/heading]",
				"/note/div/from/heading[contains(text(), \"min\")]",
				"/note/div[from[@att=\"att1\"]/heading[@att=\"headingAtt\"][contains(text(), \"min\")]]"};
		m_xpathEngine.setXPaths(xpath);
		boolean[] b = m_xpathEngine.evaluate(doc);
		assertEquals(b[0], true);
		assertEquals(b[1], true);
		assertEquals(b[2], true);
		assertEquals(b[3], true);
		assertEquals(b[4], true);
		assertEquals(b[5], true);
		assertEquals(b[6], true);
		assertEquals(b[7], true);
		assertEquals(b[8], true);
	}
}
