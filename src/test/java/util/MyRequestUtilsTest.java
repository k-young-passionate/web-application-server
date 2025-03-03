package util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MyRequestUtilsTest {
	InputStream in;

	@Before
	public void setUp() throws Exception {
		String request = "GET /index.html HTTP/1.1\nHost: localhost:8080";
		this.in = new ByteArrayInputStream(request.getBytes());
	}

	@Test
	public void testExtractMethod() throws IOException {
		// When
		String method = MyRequestUtils.extractMethod(this.in);

		// Then
		Assert.assertEquals("GET", method);
	}

	@Test
	public void testExtractResource() throws IOException {
		// When
		String path = MyRequestUtils.extractResource(this.in);

		// Then
		Assert.assertEquals("/index.html", path);
	}
}