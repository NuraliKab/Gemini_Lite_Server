package gemini_lite;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class Server {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("You need to specify root directory");
            System.exit(1);
        }

        String root = args[0];
        int port = 1958;
        if (args.length == 1) {
            try {
                new Server(root, port).run();
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(1);
            }
        } else if (args.length == 2) {
            try {
                port = Integer.parseInt(args[1]); // if user specified port then use it
                new Server(root, port).run();
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(1);
            }
        }

    }

    private final String Root;
    private final int port;

    public Server(String root, int port) {
        this.Root = root;
        this.port = port;
    }

    private void run() throws IOException {
        try (final var server = new ServerSocket(port)) {
            System.err.println("Listening on port " + port);
            while (true) {
                final var socket = server.accept(); // server stops here and waits until client will connect
                handleConnection(socket);
            }
        }
    }

    private void handleConnection(Socket socket) throws IOException {
        try {
            InputStream inputStream = socket.getInputStream();
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            int charRead;
            boolean r = false;
            int counterByte = 0;

            while ((charRead = inputStream.read()) != -1) {
                if (charRead != '\r' && charRead != '\n') {
                    counterByte++;
                    if (counterByte > 1024) {
                        Reply reply = new Reply(59, "URL is too long");
                        reply.format(socket.getOutputStream());
                        socket.close();
                        return;
                    }
                }

                if (charRead == '\n') { // this is the end of the heading
                    if (r == true) {
                        break;
                    } else { // send error to client
                        Reply reply = new Reply(59, "Heading is not valid");
                        reply.format(socket.getOutputStream());
                        socket.close();
                        return;
                    }
                }
                if (charRead == '\r') {
                    r = true;
                } else {
                    r = false;
                    byteOutputStream.write(charRead);
                }
            }
            String urlAddress = new String(byteOutputStream.toByteArray(), StandardCharsets.UTF_8);

            try {
                Request request = new Request(urlAddress); // make an request object to make design more cleaner
                RequestHandler handler = new FileSystemRequestHandler(this.Root);
                Reply reply = handler.handle(request); // produce compelete reply object to send to client

                reply.format(socket.getOutputStream());

            } catch (URISyntaxException e) {
                e.printStackTrace();
                Reply unvalidReply = new Reply(59, "URL is not valid");
                unvalidReply.format(socket.getOutputStream()); // send the error to client with already established
                                                               // connection
                return;
            } catch (IOException e) {
                e.printStackTrace();
                Reply ioErrorReply = new Reply(50, "Some IO Problem");
                ioErrorReply.format(socket.getOutputStream());
                return;
            } catch (Exception e) {
                e.printStackTrace();
                Reply generalErrorReply = new Reply(50, "Some error");
                generalErrorReply.format(socket.getOutputStream());
                return;
            }

        } finally {
            socket.close();
        }
    }
}
