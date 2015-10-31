package project2;
import java.io.*;
import java.net.Socket;

public class NonConnectProxyConnection implements Runnable {


    private final Socket socket;

    public NonConnectProxyConnection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("Start HTTP request");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStream os = socket.getOutputStream();

            String uri = null;
            String hostName = null;
            int port = -1;
            String info;
            String header = "";

            while ((info = reader.readLine()) != null) {
                if (info.startsWith("GET") || info.startsWith("POST") || info.startsWith("PUT") || info.startsWith("CONNECT")) {
                    info = info.replace("HTTP/1.1", "HTTP/1.0");
                    uri = info.split(" +")[2];
                }

                int splitPos = info.indexOf(": ");
                if (splitPos != -1) {
                    String param = info.substring(0, splitPos).trim();
                    String value = info.substring(splitPos + 2).trim();

                    switch (param) {
                        case "Host":
                            String[] strs = value.split(":");
                            hostName = value.split(":")[0];
                            if (strs.length > 1) {
                                port = Integer.parseInt(value.split(":")[1]);
                            }
                            break;
                        case "Connection":
                            value = "close";
                            info = param + ": " + value;
                            break;
                    }
                }

                header += info + "\n\r";
            }
            System.out.println(header);

            if (hostName != null) {
                if (port == -1) {
                    if (uri != null) {
                        if (uri.startsWith("http://"))
                            port = 80;
                        else if (uri.startsWith("https://"))
                            port = 443;
                    }
                }
                if (port != -1) {
                    Socket webSocket = new Socket(hostName, port);

                    OutputStreamWriter writer = new OutputStreamWriter(webSocket.getOutputStream());
                    writer.write(header);
                    pipe(webSocket.getInputStream(), os);
                    webSocket.close();
                }
            }

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pipe(InputStream is, OutputStream os) throws IOException {
        int n;
        byte[] buffer = new byte[1024];
        while ((n = is.read(buffer)) > -1) {
            os.write(buffer, 0, n);   // Don't allow any extra bytes to creep in, final write
        }
        is.close();
    }
}
