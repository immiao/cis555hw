package test.edu.upenn.cis455;


import edu.upenn.cis455.crawler.XPathCrawler;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;
import junit.framework.TestCase;
import sun.misc.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.utils.DOMBuilder;

public class XPathEngineTest extends TestCase {
	private XPathEngineImpl m_xpathEngine;
	
	protected void setUp() {
		m_xpathEngine = (XPathEngineImpl) XPathEngineFactory.getXPathEngine();
	}
	
	public void testValidXml() throws ParserConfigurationException, SAXException, IOException {
		String xml = new String();
		//File file = new File("test.html");
		String html = new String(Files.readAllBytes(Paths.get("euro2.xml")));
		org.jsoup.nodes.Document jdoc = Jsoup.parse(html);
//		System.out.println(jdoc.toString());
//		W3CDom w3cDom = new W3CDom();
//		org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jdoc);
//		System.out.println(w3cDoc.toString());
		xml += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xml += "<note>";
		xml += "<to>text()</to>";
		xml += "<to src=\"abc.gif\" date=\"12/11/1993\">Tove</to>";
		xml += "<from>Jani</from>";
		xml += "<heading>Reminder</heading>";
		xml += "<tt>Don't forget me this weekend!</tt>";
		xml += "<nest1><nest2></nest2></nest1>";
		xml += "<div><from att1=\"test\"><heading headingAtt=\"test\">Reminder</heading></from></div>";
		xml += "</note>";
//		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
//		DocumentBuilder builder = f.newDocumentBuilder();
//		Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
		Document jxmldoc = Jsoup.parse(xml, "", Parser.xmlParser());
		System.out.println(jxmldoc.toString());
		String[] xpath = {"/note/to",
				"/note/to[text() = \"text()\"]",
				"/note/to[@src=\"abc.gif\"]",
				"/note/to[@src=\"abc.gif\"][@date=\"12/11/1993\"]",
				"/note/tt[text()=\"Don't forget me this weekend!\"]",
				"/note/tt[contains(text(), \"forget\")]",
				"/note/div[from[@att1=\"test\"]/heading]",
				"/note/div/from/heading[contains(text(), \"min\")]",
				"/note/div[from[@att1=\"test\"]/heading[@headingAtt=\"test\"][contains(text(), \"min\")]]",
				"/note/div[from[@att1=\"false\"]/heading]",
				"/note/nest2",
				"/rss/channel/title[contains(text(),\"sports\")]"};
		//String[] xpath = {"/note/tt[text()=\"Don't forget me this weekend!\"]"};
		m_xpathEngine.setXPaths(xpath);
		System.out.println(jdoc.body());
		boolean[] b = m_xpathEngine.evaluate(jxmldoc);
		assertEquals(b[0], true);
		assertEquals(b[1], true);
		assertEquals(b[2], true);
		assertEquals(b[3], true);
		assertEquals(b[4], true);
		assertEquals(b[5], true);
		assertEquals(b[6], true);
		assertEquals(b[7], true);
		assertEquals(b[8], true);
		assertEquals(b[9], false);
		assertEquals(b[10], false);
		assertEquals(b[11], false);
	}
}
