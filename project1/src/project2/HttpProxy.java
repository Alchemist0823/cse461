package project2;
import jdk.internal.util.xml.impl.Input;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Date;

public class HttpProxy {

    private static final int TCP_PORT = 46103;

    public static void main(String[] args) {

        try {
            ServerSocket listenSocket = new ServerSocket(TCP_PORT);
            System.out.println(new Date() + " - Proxy listening on 0.0.0.0:" + TCP_PORT);

            while (true) {
                Socket clientSocket = listenSocket.accept();

                System.out.println("accepted client");

                new Thread(new ProxyConnection(clientSocket)).run();

                /*
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
                        Socket webSocket = new Socket(hostName, port);

                        OutputStreamWriter writer = new OutputStreamWriter(webSocket.getOutputStream());
                        writer.write(header);
                        writer.flush();

                        InputStream serverResponse = webSocket.getInputStream();

                        byte[] buf = new byte[32767];
                        int numOfBytes = serverResponse.read(buf);

                        while (numOfBytes != -1) {

                            os.write(buf, 0, numOfBytes);
                            os.flush();

                            numOfBytes = serverResponse.read(buf);
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

                //DataInputStream dis = new DataInputStream(clientSocket.getInputStream());

                new Thread(new NonConnectProxyConnection(clientSocket)).start();
            }
            */
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
