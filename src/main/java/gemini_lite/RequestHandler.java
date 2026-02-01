package gemini_lite;

import java.io.IOException;
import java.net.URISyntaxException;

public interface RequestHandler {
    public Reply handle(Request request) throws IOException, URISyntaxException;
}
