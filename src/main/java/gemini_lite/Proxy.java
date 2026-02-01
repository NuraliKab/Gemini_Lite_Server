package gemini_lite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;


//it has the same logic as server but it only reads and use port number from command line
public class Proxy {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("You need to specify port number");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        if (args.length == 1) {
            try {
                new Proxy(port).run();
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(1);
            }
        } else {
            System.err.println("You need to include port number only");
            System.exit(1);
            
        }

    }

    private final int port;

    public Proxy(int port) {
        this.port = port;
    }

    private void run() throws IOException {
        try (final var server = new ServerSocket(port)) {
            System.err.println("Listening on port " + port);
            while (true) {
                final var socket = server.accept(); //server stops here and waits until client will connect
                handleConnection(socket);
            }
        }
    }

    private void handleConnection(Socket socket) throws IOException {
        try {
            BufferedReader requestBuffered = new BufferedReader(new InputStreamReader(socket.getInputStream()));  //use decorater pattern to read os buffer then convert bytes to sting. getinputstraem just read the bytes from socket, inputstreamreader converts bytes into characters, bufferedreader transfer all characters to the local storage so program can read it istantly
            String urlAddress = requestBuffered.readLine();

            
            try {
            Request request = new Request(urlAddress);   //make an request object to make design more cleaner
            RequestHandler handler = new ProxyRequestHandler();
            Reply reply = handler.handle(request);    //produce compelete reply object to send to client

            reply.format(socket.getOutputStream());
            
            } catch (URISyntaxException e) {
                e.printStackTrace();
                Reply unvalidReply = new Reply(59, "URL is not valid");
                unvalidReply.format(socket.getOutputStream());  //send the error to client with already established connection
                return;
            } catch (IOException e) {
                e.printStackTrace();
                Reply ioErrorReply = new Reply(43, "Some IO Problem");
                ioErrorReply.format(socket.getOutputStream());
                return;
            } catch (Exception e) {
                e.printStackTrace();
                Reply generalErrorReply = new Reply(50, "Some error");
                generalErrorReply .format(socket.getOutputStream());
                return;
            }

        } finally {
            socket.close();
        }
    }
}
