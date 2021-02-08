import java.io.*;
import java.net.*;
import java.util.HashMap;


public class SocketConnection {
    public String code;
    public String domain;
    public String path;
    private Socket sock;
    private final String PROTOCOL = "HTTP/1.1";
    private final int READ_TIMEOUT = 20000;
    private final int CONNECTION_PORT = 80;
    public PrintWriter pWriter;
    public BufferedReader buffReader;
    public HashMap<String, String>  request;

    public SocketConnection(URLDepthPair url) {
        this.domain = url.domain;
        this.path = url.path;
    }

    public SocketConnection(String domain, String path) {
        this.domain = domain;
        this.path = path;
    }

    public SocketConnection(String domain) {
        this.domain = domain;
        this.path = "/";
    }

    public boolean connect() {
        try {
            this.sock = new Socket(domain,CONNECTION_PORT);
            sock.setSoTimeout(READ_TIMEOUT);
            OutputStream os = sock.getOutputStream();
            this.pWriter = new PrintWriter(os,true);
            InputStream is = sock.getInputStream();
            InputStreamReader in  = new InputStreamReader(is);
            this.buffReader = new BufferedReader(in);
            return true;
        }
        catch (UnknownHostException e) {
            return false;
        }
        catch (IOException e) {
            return false;
        }
    }

    public void sendGET () throws IOException
    {
        pWriter.println("GET "+path+" " + PROTOCOL);
        pWriter.println("Host: "+ domain);
        pWriter.println("");
        getCode();
    }

    private void getCode() throws IOException {
        String line = buffReader.readLine();
        code = line.substring(PROTOCOL.length()+1,PROTOCOL.length()+4);
        while (!(line = buffReader.readLine()).equals(""));
    }

    public void close() throws IOException {
        this.sock.close();
    }
}