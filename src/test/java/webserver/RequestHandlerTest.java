package webserver;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class RequestHandlerTest {
	InputStream in;

	@Before
	public void setUp() {
		String request = "GET /index.html HTTP/1.1\nHost: localhost:8080";
		this.in = new ByteArrayInputStream(request.getBytes());
	}

	@Test
	public void testExtractMethod() {
		// Given
		RequestHandler requestHandler = new RequestHandler(null);

		// When
		String method = requestHandler.extractMethod(in);

		// Then
		Assert.assertEquals("GET", method);
	}

	@Test
	public void testExtractPath() {
		// Given
		RequestHandler requestHandler = new RequestHandler(null);

		// When
		String path = requestHandler.extractPath(in);

		// Then
		Assert.assertEquals("/index.html", path);
	}

}