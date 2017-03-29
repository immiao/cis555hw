package edu.upenn.cis455.xpathengine;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.DefaultHandler;

class Tokenizer {
	private String m_str;
	private int m_index;

	public Tokenizer(String str) {
		m_str = str;
		m_index = 0;
	}

	private boolean isSpecialChar(char c) {
		return c == '/' || c == '\"' || c == ')' || c == '=' || c == '(' || c == '[' || c == ']' || c == ',' || c == '@';
	}

	public String nextToken() {
		int length = m_str.length();
		if (m_index >= length)
			return null;
		char c = m_str.charAt(m_index);
		// skip white space
		if (c == ' ') {
			m_index++;
			return nextToken();
		}
		if (isSpecialChar(c)) {
			m_index++;
			return String.valueOf(c);
		}
		if (m_index + 6 <= length && m_str.substring(m_index, m_index + 6).equals("text()")) {
			m_index += 6;
			return new String("text()");
		}
		if (m_index + 8 <= length && m_str.substring(m_index, m_index + 8).equals("contains")) {
			m_index += 8;
			return new String("contains");
		}
		//throw new InvalidXPathException();
		int startIndex = m_index;
		while (m_index < length && !isSpecialChar(m_str.charAt(m_index))) {
			m_index++;
		}
		// nodename
		return m_str.substring(startIndex, m_index);
	}
	
	public String nextString() {
		int length = m_str.length();
		int startIndex = m_index;
		while (m_index < length && m_str.charAt(m_index) != '"') {
			m_index++;
		}
		// nodename or "..."
		return m_str.substring(startIndex, m_index);
	}
	
	public String lookNextToken() {
		int length = m_str.length();
		if (m_index >= length)
			return null;
		char c = m_str.charAt(m_index);
		// skip white space
		if (c == ' ') {
			return lookNextToken();
		}
		if (isSpecialChar(c)) {
			return String.valueOf(c);
		}
		if (m_index + 6 <= length && m_str.substring(m_index, m_index + 6).equals("text()")) {
			return new String("text()");
		}
		if (m_index + 8 <= length && m_str.substring(m_index, m_index + 8).equals("contains")) {
			return new String("contains");
		}

		int startIndex = m_index;
		int index = m_index;
		while (index < length && !isSpecialChar(m_str.charAt(index))) {
			index++;
		}
		// nodename or "..."
		return m_str.substring(startIndex, index);
	}
};

class XPathNode {
	public String nodeName = null;
	public HashMap<String, String> attr = new HashMap<String, String>();
	public ArrayList<String> exactContent = new ArrayList<String>();
	public ArrayList<String> containContent = new ArrayList<String>();
	public ArrayList<XPathNode> containNode = new ArrayList<XPathNode>();
	public XPathNode nextNode = null;

	public XPathNode() {

	}
};

public class XPathEngineImpl implements XPathEngine {
	private String[] m_xpaths;
	private Tokenizer[] m_tokenizer;
	private Document m_document;
	private XPathNode[] m_XPathRoot;
	private boolean[] m_isValid;

	public void dfs(int i, XPathNode node, String indent) {
		if (node == null)
			return;
		System.out.println(indent + "nodeName: " + node.nodeName);
		for (String s : node.attr.keySet())
			System.out.println(indent + "att: [" + s + ", " + node.attr.get(s) + "]");
		for (String s : node.exactContent)
			System.out.println(indent + "exactContent: " + s);
		for (String s : node.containContent)
			System.out.println(indent + "containContent: " + s);
		int counter = 0;
		for (XPathNode n : node.containNode) {
			System.out.println(indent + "***containNode" + counter + " begin***");
			dfs(i, n, indent + "    ");
			System.out.println(indent + "***containNode" + counter + " end***");
			counter++;
		}
		System.out.println(indent + "***nextNode begin***");
		dfs(i, node.nextNode, indent + "    ");
		System.out.println(indent + "***nextNode end***");
	}

	public void print() {
		int length = m_XPathRoot.length;
		for (int i = 0; i < length; i++) {
			System.out.println("-----" + i + "-----");
			dfs(i, m_XPathRoot[i], "");
		}
	}

	private void isValidToken(String token, String expected) throws InvalidXPathException {
		if (token == null || !token.equals(expected))
			throw new InvalidXPathException();
	}

	private void parseAxis(int i) throws InvalidXPathException {
		if (m_tokenizer[i].nextToken().equals("/"))
			return;
		throw new InvalidXPathException();
	}

	private void parseTest(int i, XPathNode crtNode) throws InvalidXPathException {
		String tempNext = m_tokenizer[i].lookNextToken();
		String next = null;
		if (tempNext.equals("text()")) {
			next = m_tokenizer[i].nextToken(); // text()
			next = m_tokenizer[i].nextToken(); // =
			isValidToken(next, "=");
			next = m_tokenizer[i].nextToken(); // "
			isValidToken(next, "\"");
			next = m_tokenizer[i].nextString(); // text content
			if (next == null)
				throw new InvalidXPathException();
			else
				crtNode.exactContent.add(next);
			next = m_tokenizer[i].nextToken(); // "
			isValidToken(next, "\"");
		} else if (tempNext.equals("contains")) {
			next = m_tokenizer[i].nextToken(); // contains
			next = m_tokenizer[i].nextToken(); // (
			isValidToken(next, "(");
			next = m_tokenizer[i].nextToken(); // text()
			isValidToken(next, "text()");
			next = m_tokenizer[i].nextToken(); // ,
			isValidToken(next, ",");
			next = m_tokenizer[i].nextToken(); // "
			isValidToken(next, "\"");
			next = m_tokenizer[i].nextString(); // text content
			if (next == null)
				throw new InvalidXPathException();
			else
				crtNode.containContent.add(next);
			next = m_tokenizer[i].nextToken();
			isValidToken(next, "\"");
			next = m_tokenizer[i].nextToken();
			isValidToken(next, ")");
		} else if (tempNext.equals("@")) {
			next = m_tokenizer[i].nextToken(); // @
			String attrName = m_tokenizer[i].nextToken(); // attribute name
			next = m_tokenizer[i].nextToken(); // =
			isValidToken(next, "=");
			next = m_tokenizer[i].nextToken(); // "
			isValidToken(next, "\"");
			next = m_tokenizer[i].nextString(); // attribute value
			if (next == null)
				throw new InvalidXPathException();
			else
				crtNode.attr.put(attrName, next);
			next = m_tokenizer[i].nextToken(); // "
			isValidToken(next, "\"");
		} else {
			crtNode.containNode.add(parseStep(i));
		}
	}

	private XPathNode parseStep(int i) throws InvalidXPathException {
		String next = m_tokenizer[i].nextToken();

		if (next == null) // nodeName is empty
			throw new InvalidXPathException();
		XPathNode node = new XPathNode();
		node.nodeName = next;
		String tempNext = m_tokenizer[i].lookNextToken();
		if (tempNext == null) // no [test] && axis step
			return node;

		while (tempNext.equals("[")) {
			next = m_tokenizer[i].nextToken(); // [
			parseTest(i, node);
			next = m_tokenizer[i].nextToken(); // ]
			isValidToken(next, "]");
			tempNext = m_tokenizer[i].lookNextToken();
			if (tempNext == null) // no more tokens
				break;
		}

		tempNext = m_tokenizer[i].lookNextToken();
		if (tempNext == null)
			return node;
		if (tempNext.equals("/")) {
			parseAxis(i);
			node.nextNode = parseStep(i);
		}
		return node;
	}

	private XPathNode parseXPath(int i) throws InvalidXPathException {
		parseAxis(i);
		return parseStep(i);
	}

	private void buildXPathTree(int i) throws InvalidXPathException {
		m_XPathRoot[i] = parseXPath(i);
	}

	public XPathEngineImpl() {
		// Do NOT add arguments to the constructor!!
	}

	public void setXPaths(String[] s) {
		/* TODO: Store the XPath expressions that are given to this method */
		m_xpaths = s;
		m_tokenizer = new Tokenizer[s.length];
		m_XPathRoot = new XPathNode[s.length];
		m_isValid = new boolean[s.length];
		for (int i = 0; i < s.length; i++) {
			m_tokenizer[i] = new Tokenizer(s[i]);
			m_XPathRoot[i] = new XPathNode();

			try {
				buildXPathTree(i);
				m_isValid[i] = true;
			} catch (InvalidXPathException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				m_isValid[i] = false;
			}
		}
	}
	
	// verify if the attributes of a node match
	private boolean isValidElement(int i, XPathNode node, Element e) {
		for (String s : node.attr.keySet()) {
			String value = e.getAttribute(s);
			if (value == null)
				return false;
			if (!node.attr.get(s).equals(value))
				return false;
		}
		for (String s : node.containContent) {
			if (!e.getTextContent().contains(s))
				return false;
		}
		for (String s : node.exactContent) {
			if (!e.getTextContent().equals(s))
				return false;
		}
		
		for (XPathNode n : node.containNode) {
			if (!dfsValid(i, n, e))
				return false;
		}
		return true;
	}
	
	private boolean dfsValid(int i, XPathNode node, Element parentElement) {
		if (node == null)
			return true;
//		NodeList nodeList = parentElement.getElementsByTagName(node.nodeName);
//		
//		int length = nodeList.getLength();
//		if (length == 0)
//			return false;
//		
//		for (int j = 0; j < length; j++) {
//			Element e = (Element)nodeList.item(j);
//			if (isValidElement(i, node, e) && dfsValid(i, node.nextNode, e))
//				return true;
//		}
//		return false;
		NodeList nodeList = parentElement.getChildNodes();	
		
		int length = nodeList.getLength();
		for (int j = 0; j < length; j++) {
			Element e = (Element)nodeList.item(j);
			if (e.getNodeName().equals(node.nodeName)) {
				if (isValidElement(i, node, e) && dfsValid(i, node.nextNode, e))
					return true;
			}
		}
		return false;
	}
	
	public boolean isValid(int i) {
		// I check the xpath in setXPaths()->buildXPathTree(). It will throw an exception if the xpath is invalid
		return m_isValid[i];
	}

	public boolean isMatched(int i) {
//		NodeList nodeList = m_document.getElementsByTagName(m_XPathRoot[i].nodeName);
//		int length = nodeList.getLength();
//		if (length == 0)
//			return false;
//		for (int j = 0; j < length; j++) {
//			Element e = (Element)nodeList.item(j);
//			if (isValidElement(i, m_XPathRoot[i], e) && dfsValid(i, m_XPathRoot[i].nextNode, e))
//				return true;
//		}
//		return false;
		if (!m_isValid[i])
			return false;
		
		NodeList nodeList = m_document.getChildNodes();
		int length = nodeList.getLength();
		for (int j = 0; j < length; j++) {
			Element e = (Element)nodeList.item(j);
			
			if (e.getNodeName().equals(m_XPathRoot[i].nodeName)) {
				if (isValidElement(i, m_XPathRoot[i], e) && dfsValid(i, m_XPathRoot[i].nextNode, e))
					return true;
			}
		}
		return false;
	}
	
	public boolean[] evaluate(Document d) {
		/* TODO: Check whether the document matches the XPath expressions */
		m_document = d;
		boolean[] result = new boolean[m_xpaths.length];
		for (int i = 0; i < m_xpaths.length; i++) {
			result[i] = isMatched(i);
		}
		print();
		return result;
	}

	@Override
	public boolean isSAX() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean[] evaluateSAX(InputStream document, DefaultHandler handler) {
		// TODO Auto-generated method stub
		return null;
	}

}
