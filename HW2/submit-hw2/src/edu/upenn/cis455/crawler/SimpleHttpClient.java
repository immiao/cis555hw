package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
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
			socket.setSoTimeout(1000);
			// send request
			m_out = new PrintWriter(socket.getOutputStream());
			// System.out.println(req.getRawString());
			m_out.print(req.getRawString());
			m_out.flush();
			// receive response
			m_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String s = null;
			// read response line
			s = m_in.readLine();
			if (s == null || s.isEmpty())
				throw new InvalidHttpResponseException();

			String[] responseLine = s.split(" ", 3);
			if (responseLine.length < 3)
				throw new InvalidHttpResponseException();
			response = new SimpleHttpResponse(responseLine[0], Integer.parseInt(responseLine[1]), responseLine[2]);

			// read header
			s = null;
			while ((s = m_in.readLine()) != null && !s.isEmpty()) {
				String[] headerLine = s.split(":", 2);
				if (headerLine.length != 2)
					throw new InvalidHttpResponseException();
				response.addHeader(headerLine[0].trim(), headerLine[1].trim());
				 //System.out.println(headerLine[0].trim() + " : " + headerLine[1].trim());
			}
			// System.out.println("---------");

			// if not HEAD request, read content
			if (!req.getMethod().equals("HEAD")) {
				int contentSize = response.getContentLength();
				char[] buf = new char[contentSize + 1000];

				int offset = 0;
				// int c = 0;
				// contentSize = 30205;
				while (offset < contentSize) {
					// String t = c + ":" + offset;
					// if (contentSize == 30338) {
					// System.out.println(t);
					// writer.write(t);
					// }
					// c++;
					// System.out.println(contentSize - offset);
					offset += m_in.read(buf, offset, contentSize - offset);
				}

				String content = new String(buf);
				response.setContent(content);
				// System.out.print(response.getContent());
			}
		} catch (SocketTimeoutException e) {
			System.out.println(req.getURL() + ": Read Time Out");
			//e.printStackTrace();
			return null;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (InvalidHttpResponseException e) {
			e.printStackTrace();
			return null;
		} finally {
			socket.close();
		}
		return response;
	}
}
