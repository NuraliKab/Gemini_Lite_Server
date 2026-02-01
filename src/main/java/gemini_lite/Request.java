package gemini_lite;

import java.io.IOException;
import java.io.OutputStream; //use it to make request flow to server
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

//This is helper class for client
public class Request {
    private String request;
    private URI uri;

    public Request(String request) throws URISyntaxException {
        this.request = request;
        this.uri = new URI(this.request);

        // reject request if it has user info
        if (this.uri.getUserInfo() != null) {
            throw new URISyntaxException(request, "User ino is not allowed in the request");
        }
        // reject request if it has fragment
        if (this.uri.getFragment() != null) {
            throw new URISyntaxException(request, "Fragment is not allowed in the request");
        }
    }

    public String getPath() throws URISyntaxException {
        return this.uri.getPath();
    }

    public String getURI() {
        return this.request;
    }

    public void format(OutputStream outputstream) throws IOException { // we throw exception because network might fail
        // mark the end of request with \r\n
        String requestWithEnding = this.request + "\r\n";

        // translate the string to bytes and push it to the output stream
        outputstream.write(requestWithEnding.getBytes(StandardCharsets.UTF_8));

        // immediately send the request
        outputstream.flush();
    }
}
