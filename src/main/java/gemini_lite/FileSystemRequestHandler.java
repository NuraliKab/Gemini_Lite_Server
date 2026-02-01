package gemini_lite;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemRequestHandler implements RequestHandler {

    private final String root;

    public FileSystemRequestHandler(String root) {
        this.root = root;
    }

    @Override
    public Reply handle(Request request) throws IOException, URISyntaxException {
        String path = request.getPath();
        Path rootPath = Path.of(this.root);
        path = path.substring(1); // must remove the "/" in the beginning so the resolve will work
        Path completePath = rootPath.resolve(path);

        Path normalizedCompletePath = completePath.normalize();
        if (!normalizedCompletePath.startsWith(rootPath)) {
            return new Reply(59, "The path is not allowed");
        } else if (!Files.exists(normalizedCompletePath)) {
            return new Reply(51, "File cannot be reached");
        } else if (Files.isDirectory(normalizedCompletePath)) {
            return new Reply(51, "The path is a directory");
        }

        String metaData;
        if (path.endsWith(".txt")) {
            metaData = "text/plain"; // just text
        } else if (path.endsWith(".gmi")) {
            metaData = "text/gemini"; // gemtext file with links
        } else {
            metaData = "application/octet-stream"; // just send bytes
        }

        InputStream content = Files.newInputStream(normalizedCompletePath); // preparation of file to be read

        return new Reply(20, metaData, content);
    }

}
