package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            String path = extractPath(in);

            String contentType = "html";
            if (path.endsWith(".css")) {
                contentType = "css";
            } else if (path.endsWith(".js")) {
                contentType = "js";
            }

            byte[] body = readFile(path);

            if (body == null) {
                body = "Hello World".getBytes();
            }

            DataOutputStream dos = new DataOutputStream(out);

            response200Header(dos, body.length, contentType);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String extractMethod(InputStream in) {
        return parseRequest(in, ParseResource.METHOD);
    }

    private String extractPath(InputStream in) {
        return parseRequest(in, ParseResource.PATH);
    }

    enum ParseResource {
        METHOD(0), PATH(1);

        private final int position;

        ParseResource(int position) {
            this.position = position;
        }

        public int getPosition() {
            return position;
        }
    }
    
    private String parseRequest(InputStream in, ParseResource resource) {
        InputStreamReader isr = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(isr);

        String line = null;
        try {
            line = br.readLine();
            String[] splited = line.split(" ");
            return splited[resource.getPosition()];
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return "";
    }

    private byte[] readFile(String fileName) {
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

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
