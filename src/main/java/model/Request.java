package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webserver.RequestHandler;

public class Request {

	BufferedReader br;
	private String method;
	private String path;
	private Map<String, String> headers;
	private Map<String, String> cookies;
	private Map<String, String> params;

	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);


	public Request(InputStream in) throws IOException {
		InputStreamReader isr = new InputStreamReader(in);
		this.br = new BufferedReader(isr);

		String line = br.readLine();

		String[] splited = line.split(" ");

		this.method = splited[0];
		this.path = splited[1];

		headers = new HashMap<>();

		while ((line = br.readLine()) != null) {
			if (line.isEmpty()) {
				break;
			}

			String[] header = line.split(": ");
			headers.put(header[0], header[1]);
		}

	}

	private enum ParseResource {
		METHOD(0), PATH(1);

		private final int position;

		ParseResource(int position) {
			this.position = position;
		}

		public int getPosition() {
			return position;
		}
	}

	public BufferedReader getBr() {
		return br;
	}

	public String getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public Map<String, String> getCookies() {
		return cookies;
	}

	public Map<String, String> getParams() {
		return params;
	}
}
