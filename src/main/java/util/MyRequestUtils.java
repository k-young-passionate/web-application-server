package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MyRequestUtils {
	public static String extractMethod(InputStream in) throws IOException {
		return parseRequest(in, ParseResource.METHOD);
	}

	public static String extractResource(InputStream in) throws IOException {
		return parseRequest(in, ParseResource.PATH);
	}

	public enum ParseResource {
		METHOD(0), PATH(1);

		private final int position;

		ParseResource(int position) {
			this.position = position;
		}

		public int getPosition() {
			return position;
		}
	}

	private static String parseRequest(InputStream in, ParseResource resource) throws IOException {
		InputStreamReader isr = new InputStreamReader(in);
		BufferedReader br = new BufferedReader(isr);

		String line = br.readLine();
		String[] splited = line.split(" ");
		while (!"".equals(line)) {
			line = br.readLine();
		}
		return splited[resource.getPosition()];
	}
}
