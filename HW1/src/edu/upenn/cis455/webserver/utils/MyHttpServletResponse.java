package edu.upenn.cis455.webserver.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class MyHttpServletResponse implements HttpServletResponse {
	private String m_contentType = "text/html";
	private int m_contentLength;
	private int m_bufferSize;
	private String m_charset = "ISO-8859-1";
	private PrintWriter m_writer;
	private boolean m_isCommited = false;
	private Locale m_locale = null;
	private OutputBuffer m_outputBuffer;
	private OutputStream m_os;
	private HashMap<String, Vector<Object>> m_headerMap = new HashMap<String, Vector<Object>>();
	
	class OutputBuffer extends Writer {
		
		private char[] buf;
		private int charsWritten = 0;
		private OutputStream os;
		private MyHttpServletResponse resp;
		
		public OutputBuffer(int size) {
			buf = new char[size];
		}
		
		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			if (len > buf.length)
				throw new IndexOutOfBoundsException();
			
			int remain = buf.length - charsWritten;
			if (len > remain)
				flush();
			System.arraycopy(cbuf, off, buf, charsWritten, len);
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
		
		public byte[] getByteBuf() {
			return new String(buf, 0, charsWritten).getBytes();
		}
		
		public void clear() {
			charsWritten = 0;
		}
		
	}
	
	public MyHttpServletResponse(OutputStream os) {
		
		m_writer = new PrintWriter(m_outputBuffer);
	}
	
	@Override
	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub
		m_isCommited = true;
		
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
	public void addCookie(Cookie arg0) {
		if (m_isCommited)
			return;
	}

	@Override
	public void addDateHeader(String key, long value) {
		if (m_isCommited)
			return;
	}

	@Override
	public void addHeader(String key, String value) {
		if (m_isCommited)
			return;
	}

	@Override
	public void addIntHeader(String key, int value) {
		if (m_isCommited)
			return;
	}

	@Override
	public boolean containsHeader(String key) {
		return m_headerMap.containsKey(key);
	}

	@Override
	public String encodeRedirectURL(String arg0) {
		// TODO Auto-generated method stub
		return null; 
	}

	@Override
	public String encodeRedirectUrl(String arg0) {
		// TODO Auto-generated method stub
		return null; // deprecated
	}

	@Override
	public String encodeURL(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeUrl(String arg0) {
		// TODO Auto-generated method stub
		return null; // deprecated
	}

	@Override
	public void sendError(int arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendError(int arg0, String arg1) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendRedirect(String arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDateHeader(String arg0, long arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIntHeader(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStatus(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStatus(int arg0, String arg1) {
		// TODO Auto-generated method stub

	}

}
