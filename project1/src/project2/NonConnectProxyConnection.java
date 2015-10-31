package project2;

import java.io.*;
import java.net.Socket;
import java.util.Date;

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
            String firstLine = "";

            while ((info = reader.readLine()) != null) {
                if (info.startsWith("GET") || info.startsWith("POST") || info.startsWith("PUT") || info.startsWith("CONNECT")) {
                    firstLine = info;
                    info = info.replace("HTTP/1.1", "HTTP/1.0");
                    uri = info.split(" +")[1];
                }

                if (info.equals(""))
                    break;

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
                        case "Proxy-Connection":
                            value = "close";
                            info = param + ": " + value;
                            break;
                    }
                }

                header += info + "\r\n";
            }
            header += "\r\n";
            System.out.println(new Date() + " - >>> " + firstLine);

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
                    System.out.println(hostName + ":" + port);
                    Socket webSocket = new Socket(hostName, port);

                    OutputStreamWriter writer = new OutputStreamWriter(webSocket.getOutputStream());
                    writer.write(header);
                    writer.flush();

                    InputStream serverResponse = webSocket.getInputStream();

                    // a buffer to hold the response from server
                    // and send to the client
                    byte[] buf = new byte[32767];
                    int numOfBytes = serverResponse.read(buf);

                    // keep reading from the buffer until there's nothing to be read
                    while (numOfBytes != -1) {
                        // write the response to client
                        // and flush the writer
                        os.write(buf, 0, numOfBytes);
                        os.flush();

                        // read the next line of the response
                        numOfBytes = serverResponse.read(buf);

                        // cSocket.shutdownOutput(); // for experiment
                        // cSocket.shutdownInput(); // for experiment
                    }

                    serverResponse.close();
                    webSocket.close();
                    os.close();
                }
            }

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
