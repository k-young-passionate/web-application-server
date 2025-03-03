package webserver;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import db.DataBase;
import model.User;

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
	public void testExtractResource() {
		// Given
		RequestHandler requestHandler = new RequestHandler(null);

		// When
		String path = requestHandler.extractResource(in);

		// Then
		Assert.assertEquals("/index.html", path);
	}

	@Test
	public void testGetUserCreateHandler() {
		// Given
		RequestHandler requestHandler = new RequestHandler(null);
		String path = "/user/create?userId=abc&password=def&name=ghi&email=jkl";

		// When
		requestHandler.getUserCreateHandler(path);

		User user = DataBase.findUserById("abc");
		User expected = new User("abc", "def", "ghi", "jkl");

		// Then
		Assert.assertEquals(expected.getUserId(), user.getUserId());
		Assert.assertEquals(expected.getPassword(), user.getPassword());
		Assert.assertEquals(expected.getName(), user.getName());
		Assert.assertEquals(expected.getEmail(), user.getEmail());
	}
}