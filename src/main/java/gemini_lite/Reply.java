package gemini_lite;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

//Helper class for server
public class Reply {
    private String metaData;
    private int status;     //we introduce handling o different status codes
    private InputStream content;

    public Reply(int status, String metaData) {  //for errors
        this.status = status;   
        this.metaData = metaData;    
    }

    public Reply(int status, String metaData, InputStream content) {
        this.status = status;   
        this.metaData = metaData;     //so the client will see how to handle the file
        this.content = content;
    }

    public void format(OutputStream outputstream) throws IOException {

        String header = this.status + " " + this.metaData + "\r\n";

        outputstream.write(header.getBytes(StandardCharsets.UTF_8));
        
        outputstream.flush();

        if (this.status == 20) {
            this.content.transferTo(outputstream);   //sends bytes to client
            outputstream.flush();     //send leftover data immediately wihtout waiting to fill up buffer
            this.content.close();    //close the file after sending it. To avoid exceeding number of open files
        }
    }
}
