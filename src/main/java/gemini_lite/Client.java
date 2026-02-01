package gemini_lite;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws Throwable {
        if (args.length == 0) {
            System.err.println("Enter directory");
            System.exit(1);
        }

        String urlCurrent = args[0];
        int Counter = 0;

        while (true) {
            URI uri = new URI(urlCurrent);
            String host = uri.getHost();
            int port = uri.getPort();
            if (port == -1) {
                port = 1958; // default port for gemini protocol
            }

            // check if proxy exist
            String proxyCheck = System.getenv("GEMINI_LITE_PROXY");
            /* user manually specify which proxy to connect the client */
            if (proxyCheck != null) {
                String[] proxyDivided = proxyCheck.split(":");
                host = proxyDivided[0];
                port = Integer.parseInt(proxyDivided[1]);
            }

            // make the tcp connection to server
            Socket socket = new Socket(host, port);

            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();

            // send the request to server
            String requestString = urlCurrent + "\r\n";
            outputStream.write(requestString.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

            // emoji needs 4 characters to be displayed so we need array
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            int charRead;
            boolean r = false; // check if the character before is \r
            int counterByte = 0;

            while ((charRead = inputStream.read()) != -1) {
                if (charRead != '\r' && charRead != '\n') {
                    // metadata has limit of 1024 bytes and 3 bytes for status code
                    counterByte++;
                    if (counterByte > 1027) {
                        System.err.println("Too much bytes in the header");
                        socket.close();
                        System.exit(1);
                    }
                }

                if (charRead == '\n') { // this is the end of the heading
                    if (r == true) {
                        break;
                    } else {
                        System.err.println("The heading is wrong");
                        socket.close();
                        System.exit(1);
                    }
                }
                if (charRead == '\r') {
                    r = true;
                } else {
                    r = false;
                    byteOutputStream.write(charRead);
                }
            }

            /*
             * We collected all characters and convert it to string but we also considering
             * the array of characters which create the emoji
             */
            String response = new String(byteOutputStream.toByteArray(), StandardCharsets.UTF_8);

            // Check for the prohibited chracters
            for (int i = 0; i < response.length(); i++) {
                char c = response.charAt(i);
                if (c >= 128 && c <= 159) {
                    System.err.println("Invalid character used in heading");
                    socket.close();
                    System.exit(1);
                }
            }
            if (response.isEmpty()) {
                System.err.println("no response");
                System.exit(1);
            } else if (response.length() < 2) {
                System.err.println("response is too short");
                System.exit(1);
            } else if (!Character.isDigit(response.charAt(0)) || !Character.isDigit(response.charAt(1))) {
                // prevents to have 1 or 3 digit status code
                System.err.println("Status code is not two digits");
                System.exit(1);
            } else if (response.length() > 2 && response.charAt(2) != ' ') {
                /*
                 * for gemini protocol it is mandatory to have the status code
                 * but not always it has metadata
                 */
                System.err.println("Status code should be two digits");
                System.exit(1);
            }

            if (response.charAt(0) == '2') { // any status code that starts with 2 is success
                if (response.length() < 4) { // response only have status code
                    System.err.println("no mimetype found");
                    System.exit(1);
                }

                inputStream.transferTo(System.out); // take remaining bytes from inputstream and pirint it to terminal
                socket.close();
                System.exit(0);
            } else if (response.charAt(0) == '5') {
                System.err.println("Very bad error occured:" + response);
                int exit = 1;
                exit = Integer.parseInt(response.substring(0, 2));
                System.exit(exit);
            } else if (response.charAt(0) == '4') { // Temporary error so wait for 1 second
                System.err.println("Waiting for server response");
                Thread.sleep(1000);
            } else if (response.charAt(0) == '3') {
                String urlNew = response.substring(3);
                urlCurrent = uri.resolve(urlNew).toString();
                // handle address even if it not full url (No host and port)
                if (Counter >= 5) {
                    System.exit(1);
                }
                Counter++;
                socket.close();
            } else if (response.charAt(0) == '1') { // need input (password) from user to access the directory
                if (response.length() < 3) {
                    System.err.println("No prompt found");
                    System.exit(1);
                }
                if (urlCurrent.contains("?")) { // remove old password from url
                    urlCurrent = urlCurrent.substring(0, urlCurrent.indexOf("?"));
                }

                String pswd;
                if (args.length > 1) { // check if user already inputed password in args
                    pswd = args[1];
                    /*
                     * add password to url and replace the empty space with %20
                     * which is mean space in hex code
                     */
                    urlCurrent = urlCurrent + "?" + pswd.replace(" ", "%20");
                    socket.close();
                } else {
                    System.out.println("Enter password:");
                    Scanner scanner = new Scanner(System.in); // if no passord in args make user to enter passord
                    pswd = scanner.nextLine();

                    urlCurrent = urlCurrent + "?" + pswd.replace(" ", "%20");
                    socket.close();
                }
            } else {
                System.err.println("Unknown response received:" + response);
                System.exit(1);
            }
        }
    }

}