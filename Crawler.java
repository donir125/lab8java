import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.sql.SQLException;

public class Crawler {
    static final String CONST1 = "(href=\").*?\"";
    static final String CONST2 = "(<a).*?>";
    private static final Pattern TAG_A_PATTEN_COMPILE = Pattern.compile(CONST2);
    private static final Pattern CONST1_COMPILE = Pattern.compile(CONST1);
    private HashSet<String> enter = new HashSet<String>();
    private HashSet<String> allDomain = new HashSet<String>();
    private LinkedBlockingDeque <URLDepthPair> unVisited = new LinkedBlockingDeque <URLDepthPair>();
    private LinkedList <URLDepthPair> checkedList = new LinkedList <URLDepthPair>();
    private LinkedList <URLDepthPair> noneCheckedList = new LinkedList <URLDepthPair>();
    private int depth;

    public static void main(String[] args) throws UnknownHostException,IOException,ClassNotFoundException,SQLException,InterruptedException {
        String test = "http://ausgrass2.myspecies.info";
        Crawler crawl = new Crawler(test, 1);
        crawl.startCrawl();
    }

    public Crawler(String url, int depth) throws UnknownHostException, IOException, ClassNotFoundException, SQLException {
        URLDepthPair urlpair = new URLDepthPair(url,0);
        unVisited.add(urlpair);
        enter.add(urlpair.url);
        allDomain.add(urlpair.domain);
        this.depth = depth;
    }

    public void startCrawl() throws IOException, InterruptedException {
        while(!unVisited.isEmpty()) {
            URLDepthPair urlpair = unVisited.pollFirst();
            if (urlpair.depth<=depth)
                if(readURL(urlpair)) checkedList.addLast(urlpair);
                else noneCheckedList.add(urlpair);
        }
    }

    private boolean readURL(URLDepthPair urlpair) throws IOException {
        try {
            SocketConnection conn = new SocketConnection(urlpair);
            if (!conn.connect()) return false;
            conn.sendGET();
            if (Integer.parseInt(conn.code) == 200) {
                System.out.printf("[%s]: good request, status code: %s\n", urlpair.url, conn.code);
                String line;
                BufferedReader buffReader = conn.buffReader;
                while ((line = buffReader.readLine()) != null) {
                    ParseNewURL(line,urlpair);
                }
                conn.close();
                return true;
            }
            else {
                System.out.printf("[%s]: bad request, status code: %s\n", urlpair.url, conn.code);
                return false;
            }
        }
        catch (SocketTimeoutException exception) {
            System.out.printf("[%s]: connection timeout\n", urlpair.url);
            return false;

        }

    }

    private boolean ParseNewURL(String line, URLDepthPair url){
        try {
            Matcher m = TAG_A_PATTEN_COMPILE.matcher(line);
            m.find();
            String tagA = line.substring(m.start(),m.end());
            Matcher m2 = CONST1_COMPILE.matcher(tagA);
            m2.find();
            String href = tagA.substring(m2.start(),m2.end());
            URLDepthPair newURL = new URLDepthPair(href,url.depth+1,url.domain);

            if (!enter.contains(newURL.url)) {
                enter.add(newURL.url);
                if (allDomain.contains(newURL.domain)) unVisited.addLast(newURL);
                else {
                    unVisited.addFirst(newURL);
                    allDomain.add(newURL.domain);
                }

                return true;
            }
            else return false;

        }
        catch (IllegalStateException e) {
            return false;
        }
        catch (MalformedURLException e) {
            return false;
        }
    }

    public LinkedList <URLDepthPair> getcheckedSites() {
        return checkedList;
    }

    public LinkedList <URLDepthPair> getUncheckedSites() {
        return noneCheckedList;
    }

}