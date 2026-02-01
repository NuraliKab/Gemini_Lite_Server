package gemini_lite;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class ProxyRequestHandler implements RequestHandler {

    public ProxyRequestHandler() {
    }

    @Override
    public Reply handle(Request request) throws IOException, URISyntaxException {
        String urlCurrent = request.getURI();

        // use counter to control number of redirects and avoid infinite loops
        int counter = 0;
        while (counter < 10) {
            URI uri = new URI(urlCurrent);
            String host = uri.getHost();
            int port = uri.getPort();
            if (port == -1) {
                port = 1958; // use default port for the gemini server
            }

            Socket socket = new Socket(host, port); // do TCp call to server
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();

            String requestString = urlCurrent + "\r\n"; // format and send the request to server
            outputStream.write(requestString.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

            // wait for the response from server and read the inputstream byte by byte to
            // get only header
            StringBuilder headingStringBuffer = new StringBuilder();
            int charRead;
            while ((charRead = inputStream.read()) != -1) {
                if (charRead == '\n') {
                    break;
                }
                if (charRead != '\r') {
                    headingStringBuffer.append((char) charRead);
                }
            }
            String headingString = headingStringBuffer.toString();

            char statusCode = headingString.charAt(0);

            if (statusCode == '2') {
                int code = Integer.parseInt(headingString.substring(0, 2));
                String metaData = headingString.substring(3);
                return new Reply(code, metaData, inputStream);
            } else if (statusCode == '3') {
                String urlNew = headingString.substring(3);
                urlCurrent = urlNew;
                counter++;
                socket.close();
            } else {
                int code = Integer.parseInt(headingString.substring(0, 2)); // any other error just send it to client to
                                                                            // handle
                String metaData = headingString.substring(3);
                socket.close();
                return new Reply(code, metaData);
            }
        }
        return new Reply(43, "Too many redirects from proxy side");
    }

}