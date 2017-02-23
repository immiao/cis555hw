package edu.upenn.cis455.webserver.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class MyHttpServletResponse implements HttpServletResponse {
	private String m_contentType = "text/html";
	private int m_contentLength = -1;
	private int m_bufferSize;
	private String m_charset = "ISO-8859-1";
	private PrintWriter m_writer;
	private boolean m_isCommited = false;
	private Locale m_locale = null;
	private OutputBuffer m_outputBuffer;
	private OutputStream m_os;
	private HashMap<String, Object> m_headerMap = new HashMap<String, Object>();
	private int m_statusCode;
	private final String m_protocol = "HTTP/1.1";
	private String m_dateKey = null;
	private Date m_date = null;
	private Vector<Cookie> Cookies = new Vector<Cookie>();
	
	private String m_statusMsg = null;
	private String m_location = null;
	
	public static HashMap<Integer, String> status = new HashMap<Integer, String>();
	static SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy, hh:mm:ss z");
	
	class OutputBuffer extends Writer {
		
		private char[] buf;
		private int charsWritten = 0;
		private MyHttpServletResponse resp;
		
		public OutputBuffer(int size, MyHttpServletResponse resp) {
			buf = new char[size];
			this.resp = resp;
		}
		
		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			if (len > buf.length)
				throw new IndexOutOfBoundsException();
			
			int remain = buf.length - charsWritten;
			if (len > remain)
				flush();
			System.arraycopy(cbuf, off, buf, charsWritten, len);
			charsWritten += len;
		}

		@Override
		public void flush() throws IOException {
			resp.flushBuffer();
			charsWritten = 0;
		}

		@Override
		public void close() throws IOException {
			resp.flushBuffer();
		}
		
		public String getString() {
			return new String(buf, 0, charsWritten);
		}
		
		public byte[] getByteBuf() {
			return new String(buf, 0, charsWritten).getBytes();
		}
		
		public void clear() {
			charsWritten = 0;
		}
		
	}
	
	public MyHttpServletResponse(OutputStream os, int size) {
		m_os = os;
		m_outputBuffer = new OutputBuffer(size, this);
		m_writer = new PrintWriter(m_outputBuffer);
	}
	
	@Override
	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub
		if (m_isCommited)
			return;
		m_isCommited = true;
		String result = new String();
		
		// initial line
		if (m_statusMsg == null)
			result += m_protocol + " " + m_statusCode + " " + status.get(m_statusCode) + "\r\n";
		else {
			result += m_protocol + " " + m_statusCode + " " + m_statusMsg + "\r\n";
			m_statusMsg = null;
		}
		
		// special header
		result += "Content-Type: " + m_contentType + "\r\n";
		m_contentLength = m_outputBuffer.getByteBuf().length;
		if (m_contentLength != -1)
			result += "Content-Length: " + m_contentLength + "\r\n";
		if (m_dateKey != null)
			result += m_dateKey + ": " + format.format(m_date) + "\r\n";
		if (m_location != null)
			result += "Location: " + m_location;
		
		// header map
		for (String key : m_headerMap.keySet()) {
			result += key + ": " + m_headerMap.get(key) + "\r\n";
		}
		// cookie
		for (Cookie c : Cookies) {
			result += "Set-Cookie: " + c.getName() + "=" + c.getValue();
			if (c.getSecure())
				result += "; Secure";
			if (c.getMaxAge() != -1)
				result += "; Max-Age=" + c.getMaxAge();
			if (c.getDomain() != null)
				result += "; Domain=" + c.getDomain();
			if (c.getPath() != null)
				result += "; Path=" + c.getPath();
			result += "\r\n";
		}
		// additional blank line after header
		result += "\r\n";
		// body
		result += m_outputBuffer.getString();
		
		System.out.print(result);
		m_os.write(result.getBytes(), 0, result.getBytes().length);
		resetBuffer();
	}

	@Override
	public int getBufferSize() {
		return m_bufferSize;
	}

	@Override
	public String getCharacterEncoding() {
		return m_charset;
	}

	@Override
	public String getContentType() {
		return m_contentType;
	}

	@Override
	public Locale getLocale() {
		return m_locale;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return null; // return null
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return m_writer;
	}

	@Override
	public boolean isCommitted() {
		return m_isCommited;
	}

	@Override
	public void reset() {
		if (m_isCommited)
			throw new IllegalStateException();
		m_outputBuffer.clear();
	}

	@Override
	public void resetBuffer() {
		m_outputBuffer.clear();
	}

	@Override
	public void setBufferSize(int size) {
		if (m_isCommited)
			return;
		m_bufferSize = size;
	}

	@Override
	public void setCharacterEncoding(String charset) {
		if (m_isCommited)
			return;
		m_charset = charset;
	}

	@Override
	public void setContentLength(int len) {
		if (m_isCommited)
			return;
		m_contentLength = len;
	}

	@Override
	public void setContentType(String type) {
		if (m_isCommited)
			return;
		m_contentType = type;
	}

	@Override
	public void setLocale(Locale locale) {
		if (m_isCommited)
			return;
		m_locale = locale;
	}

	@Override
	public void addCookie(Cookie cookie) {
		if (m_isCommited)
			return;
		Cookies.add(cookie);
	}

	@Override
	public void addDateHeader(String key, long value) {
		if (m_isCommited)
			return;
		m_dateKey = key;
		m_date = new Date(value);
	}

	@Override
	public void addHeader(String key, String value) {
		if (m_isCommited)
			return;
		m_headerMap.put(key, value);
	}

	@Override
	public void addIntHeader(String key, int value) {
		if (m_isCommited)
			return;
		m_headerMap.put(key, value);
	}

	@Override
	public boolean containsHeader(String key) {
		return m_headerMap.containsKey(key);
	}

	@Override
	public String encodeRedirectURL(String url) {
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return url;
		}	
	}

	@Override
	public String encodeRedirectUrl(String arg0) {
		// TODO Auto-generated method stub
		return null; // deprecated
	}

	@Override
	public String encodeURL(String url) {
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return url;
		}	
	}

	@Override
	public String encodeUrl(String arg0) {
		// TODO Auto-generated method stub
		return null; // deprecated
	}

	@Override
	public void sendError(int sc) throws IOException {
		m_statusCode = sc;
		flushBuffer();
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		m_statusCode = sc;
		m_statusMsg = msg;
		flushBuffer();
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		m_location = location;
		m_statusCode = 302;
		flushBuffer();
	}

	@Override
	public void setDateHeader(String key, long value) {
		if (m_isCommited)
			return;
		m_headerMap.put(key, value);
	}

	@Override
	public void setHeader(String key, String value) {
		if (m_isCommited)
			return;
		m_headerMap.put(key, value);
	}

	@Override
	public void setIntHeader(String key, int value) {
		if (m_isCommited)
			return;
		m_headerMap.put(key, value);
	}

	@Override
	public void setStatus(int code) {
		if (m_isCommited)
			return;
		m_statusCode = code;
	}

	@Override
	public void setStatus(int arg0, String arg1) {
		if (m_isCommited)
			return; // deprecated
	}

}
