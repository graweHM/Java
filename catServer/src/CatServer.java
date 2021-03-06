import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TreeMap;

public class CatServer {
    //to format logMessage time
    private SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
    //i/o to client
    private PrintWriter cpw;
    private BufferedReader cbr;
    //i/o to server
    private PrintWriter spw;
    private BufferedReader sbr;
    //sockets
    private ServerSocket ps;
    private Socket pcs;
    private Socket pss;
    //port to listen on for browser request
    private int port = 8082;
    //client info
    private TreeMap<String, String> clientInfo = new TreeMap<>();
    //host input
    private String host;
    private String url;
    //http version
    private String httpVersion;
    //uft-8 charset
    private String charset = StandardCharsets.UTF_8.name();
    //link to replace img link
    private String catPic = "http://test.annarboranimalhospital.com/wp-content/uploads/2011/06/Obese-cat.jpg";
    //default request values
    private String accLang = " en-US";

    public CatServer(){

    }

    /**
     * opens i/o to client and server and gets the request host
     * @throws IOException
     */
    public void startServer() throws IOException, InterruptedException{
        try {
            logMessage("Starting Server");
            ps = new ServerSocket(port);
            logMessage("Server listening on port " + port);

            cycle();

        }catch (Exception e){
            logMessage(e.toString());
        }finally {
            stopServer();
        }
    }

    private void cycle() throws IOException, InterruptedException{
        pcs = ps.accept();
        logMessage("Client connected");

        cbr = new BufferedReader(new InputStreamReader(pcs.getInputStream()));
        cpw = new PrintWriter(new OutputStreamWriter(pcs.getOutputStream()), true);

        if (!(getInputURL(cbr.readLine())))
            stopServer();

        logMessage("Host: " + host);

        if (clientInfo.size() == 0) {
            logMessage("");
            logMessage("CLIENT INFO");
            for (String tmp; (tmp = cbr.readLine()) != null && cbr.ready(); ) {
                if (tmp.contains(":"))
                    clientInfo.put(tmp.substring(0, tmp.indexOf(":")), tmp.substring(tmp.indexOf(":") + 1, tmp.length()));
                else {

                }
                logMessage(tmp);
            }
        }
        sendGET();
        evaluateResponse();
        cycle();
    }

    private boolean getInputURL(String x){
        if(x == "" || x == null)
            return false;
        url = "";
        host = null;

        String[] get = x.split(" ");
        String tmp = get[1];
        httpVersion = get[2];

        boolean gotHost = false;
        for(String z : tmp.split("/")){
            if((z.contains(".")) && host == null) {
                if(!z.startsWith("www.")){
                    z = "www." + z;
                }
                host = z;
                gotHost = true;
                continue;
            }
            if(gotHost == true) {
                url = url + "/" + z;
                System.out.println(z);
            }
        }
        if(url == "")
            url = "/";
        if(host == null)
            return false;

        return true;
    }

    private void sendGET() throws IOException{
        pss = new Socket(host, 80);
        spw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(pss.getOutputStream())), true);
        sbr = new BufferedReader(new BufferedReader(new InputStreamReader(pss.getInputStream())));

        logMessage("");
        logMessage("Connection to " + host + " established");

        spw.print("GET " + url + " " + httpVersion + "\r\n");
        spw.print("Host: " + host + "\r\n");
        if(clientInfo.containsKey("Accept"))
            spw.print("Accept:" + clientInfo.get("Accept") + "\r\n");
        if(clientInfo.containsKey("Accept-Language"))
            accLang = clientInfo.get("Accept-Language");
        spw.print("Accept-Language:" + accLang + "\r\n");
        if(clientInfo.containsKey("User-Agent"))
            spw.print("User-Agent:" + clientInfo.get("User-Agent") + "\r\n");
        spw.print("Accept-Encoding: \r\n");
        spw.print("Accept-Charset: "+ charset + "\r\n");
        spw.println();

        logMessage("");
        logMessage("SENDING REQUEST: ");
        logMessage("GET " + url + " " + httpVersion);
        logMessage("Host: " + host);
        if(clientInfo.containsKey("Accept"))
            logMessage("Accept:" + clientInfo.get("Accept"));
        logMessage("Accept-Language:" + accLang);
        if(clientInfo.containsKey("User-Agent"))
            logMessage("User-Agent:" + clientInfo.get("User-Agent"));
        logMessage("Accept-Encoding: ");
        logMessage("Accept-Charset: "+ charset);
        logMessage("");
    }

    private void evaluateResponse() throws IOException, InterruptedException{
        for(String line; (line = sbr.readLine()) != null;){
            if(line.startsWith("HTTP/")) {
                logMessage("SERVER RESPONSE: " + line);
                logMessage("");
            }
            if(line.contains("</html>")){
                cpw.println(line);
                return;
            }

            if(line.toLowerCase().contains("you")){
                line = line.replaceAll("you", "you, admirer of cats and all things feline :-)");
                line = line.replaceAll("You", "You, admirer of cats and all things feline :-)");
            }
            if(line.contains("<img")){
                String tmp = line.substring(line.indexOf("src=") + 5, line.length());
                if(tmp.contains("\""))
                    line = line.substring(0, line.indexOf("src=")+5) + catPic +  tmp.substring(tmp.indexOf("\""), tmp.length());
            }
            cpw.println(line);
        }
    }

    private void logMessage(String m){
        System.err.println("[" + time.format(Calendar.getInstance().getTime()) + "] - " + m);
    }

    private void stopServer() throws IOException, InterruptedException{
        logMessage("STOPPING SERVER");
        if(ps != null) {
            cbr.close();
            cpw.close();
            ps.close();
        }
        if(pss != null) {
            sbr.close();
            spw.close();
            pss.close();
        }
        startServer();
    }

}
