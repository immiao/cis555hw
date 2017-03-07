package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SimpleHttpClient {
	private BufferedReader m_in = null;
	private PrintWriter m_out = null;

	public SimpleHttpClient() {

	}

	public SimpleHttpResponse execute(SimpleHttpRequest req) throws IOException {
		Socket socket = null;
		SimpleHttpResponse response = null;
		try {
			// open connection
			socket = new Socket(req.getHost(), 80);
			
			// send request
			m_out = new PrintWriter(socket.getOutputStream());
			System.out.print(req.getRawString());
			m_out.print(req.getRawString());
			
			// receive response
			m_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String s = null;
			// read request line
			s = m_in.readLine();
			if (s == null || s.isEmpty())
				throw new InvalidHttpResponseException();
			
			String[] requestLine = s.split(" ");
			if (requestLine.length != 3)
				throw new InvalidHttpResponseException();
			response = new SimpleHttpResponse(requestLine[0], Integer.parseInt(requestLine[1]), requestLine[2]);
			
			// read header
			s = null;
			while ((s = m_in.readLine()) != null && !s.isEmpty()) {
				String[] headerLine = s.split(":");
				if (headerLine.length != 2)
					throw new InvalidHttpResponseException();
				response.addHeader(headerLine[0], headerLine[1]);
			}
			
			// read content
			int contentSize = response.getContentLength();
			String content = new String();
			while (contentSize-- != 0) {
				content += m_in.read();
			}
			System.out.println(content);
			response.setContent(content);
			m_out.print(req.getRawString());

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidHttpResponseException e) {
			e.printStackTrace();
		} finally {
			socket.close();
		}
		return response;
	}
}
