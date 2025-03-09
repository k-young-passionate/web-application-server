package webserver;

import static util.HttpRequestUtils.*;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
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
            String method = request.getMethod();

            String contentType = "html";
            if (path.endsWith(".css")) {
                contentType = "css";
            } else if (path.endsWith(".js")) {
                contentType = "js";
            }

            DataOutputStream dos = new DataOutputStream(out);

            if (path.equals("/user/create")) {
                postUserCreateHandler(dos, request);
            } else if (path.equals("/user/login")) {
                postUserLoginHandler(dos, request);
            } else {
                responseResource(dos, path, contentType);
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

    void postUserCreateHandler(DataOutputStream dos, Request request) throws IOException {
        String body = IOUtils.readData(request.getBr(), Integer.parseInt(request.getHeaders().get("Content-Length")));
        Map<String, String> params = parseQueryString(body);

        User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
        DataBase.addUser(user);

        response302Header(dos);
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

    private void response302Header(DataOutputStream dos) {
        try {
            dos.writeBytes("Http/1.1 302 Found \r\n");
            dos.writeBytes("Location: /index.html \r\n");
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
