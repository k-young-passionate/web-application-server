package webserver;

import static util.HttpRequestUtils.*;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.Request;
import model.User;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            Request request = new Request(in);

            String path = request.getPath();

            String contentType = "html";
            if (path.endsWith(".css")) {
                contentType = "css";
            } else if (path.endsWith(".js")) {
                contentType = "js";
            }

            DataOutputStream dos = new DataOutputStream(out);

			switch (path) {
				case "/user/create":
					postUserCreateHandler(dos, request);
					break;
				case "/user/login":
					postUserLoginHandler(dos, request);
					break;
				case "/user/list":
					getUserListHandler(dos, request);
					break;
				default:
					responseResource(dos, path, contentType);
					break;
			}
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    void responseResource(DataOutputStream dos, String path, String contentType) throws IOException {
        byte[] body = readFile(path);
        response200Header(dos, body.length, contentType);
        responseBody(dos, body);
    }

    void responseResource(DataOutputStream dos, String path, String contentType, String cookie) throws IOException {
        byte[] body = readFile(path);
        response200Header(dos, body.length, contentType, cookie);
        responseBody(dos, body);
    }

    void getUserListHandler(DataOutputStream dos, Request request) throws IOException {
        boolean isLogin = Boolean.parseBoolean(request.getCookies().get("logined"));

        if (isLogin) {
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            sb.append("<body>");
            sb.append("<table border='1'>");
            sb.append("<tr>");
            sb.append("<th>userId</th>").append("<th>name</th>").append("<th>email</th>");
            sb.append("</tr>");

            for (User user : DataBase.findAll()) {
                sb.append("<tr>");
                sb.append("<td>").append(user.getUserId()).append("</td>");
                sb.append("<td>").append(user.getName()).append("</td>");
                sb.append("<td>").append(user.getEmail()).append("</td>");
                sb.append("</tr>");
            }

            sb.append("</table>");
            sb.append("</body>");
            sb.append("</html>");

            byte[] userList = sb.toString().getBytes(StandardCharsets.UTF_8);

            response200Header(dos, userList.length, "html");
            responseBody(dos, userList);
        } else {
            response302Header(dos, "/user/login.html");
        }
    }

    void postUserCreateHandler(DataOutputStream dos, Request request) throws IOException {
        String body = IOUtils.readData(request.getBr(), Integer.parseInt(request.getHeaders().get("Content-Length")));
        Map<String, String> params = parseQueryString(body);

        User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
        DataBase.addUser(user);

        response302Header(dos, "/index.html");
    }

    void postUserLoginHandler(DataOutputStream dos, Request request) throws IOException {
        String body = IOUtils.readData(request.getBr(), Integer.parseInt(request.getHeaders().get("Content-Length")));
        Map<String, String> params = parseQueryString(body);

        User user = DataBase.findUserById(params.get("userId"));

        boolean isLogin = user != null && user.getPassword().equals(params.get("password"));

        if (isLogin) {
            response200Header(dos, 0, "html", "logined=true");
        } else {
            responseResource(dos, "/user/login_failed.html", "html", "logined=false");

        }
    }

    private byte[] readFile(String fileName) throws IOException {
        byte[] body = null;
        try {
            body = Files.readAllBytes(new File("./webapp" + fileName).toPath());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return body;
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        if (contentType == null) {
            contentType = "html";
        }

        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/" + contentType + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType, String cookie) {
        if (contentType == null) {
            contentType = "html";
        }

        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/" + contentType + ";charset=utf-8\r\n");
            dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String path) {
        try {
            dos.writeBytes("Http/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + path + " \r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
