package project2;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

public class ConnectProxyConnection implements Runnable {


    private final Socket socket;

    public ConnectProxyConnection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            OutputStream clientOutput = new BufferedOutputStream(socket.getOutputStream());
            InputStream clientInput = socket.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(clientInput));

            String uri = null;
            String hostName = null;
            int port = -1;
            String info;
            String header = "";
            String firstLine = "";

            while ((info = reader.readLine()) != null) {
                if (info.startsWith("CONNECT")) {
                    firstLine = info;
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
                    try {
                        Socket webSocket = new Socket(hostName, port);
                        OutputStreamWriter writer = new OutputStreamWriter(clientOutput);
                        writer.write("HTTP/1.1 200 OK" + "\r\n");
                        writer.flush();

                        InputStream serverInput = webSocket.getInputStream();
                        OutputStream serverOutput = new BufferedOutputStream(webSocket.getOutputStream());

                        int availableCI = -1, availableSI = -1, ch = -1;
                        long timeout = 10000;
                        long lastReceived = new Date().getTime();
                        long now = new Date().getTime();
                        try {
                            while (availableCI != 0 || availableSI != 0 || (now - lastReceived) <= timeout) {
                                while ((availableCI = clientInput.available()) > 0) {
                                    for (int i = 0; i < availableCI; i++) {
                                        ch = clientInput.read();
                                        if (ch != -1) {
                                            serverOutput.write(ch);
                                            System.out.print(ch);
                                        } else {
                                            System.out.println("client stream closed");
                                        }
                                    }
                                    lastReceived = new Date().getTime();
                                    serverOutput.flush();
                                }
                                while ((availableSI = serverInput.available()) > 0) {
                                    for (int i = 0; i < availableSI; i++) {
                                        ch = serverInput.read();
                                        if (ch != -1) {
                                            clientOutput.write(ch);
                                            System.out.print(ch);
                                        } else {
                                            System.out.println("server stream closed");
                                        }
                                    }
                                    lastReceived = new Date().getTime();
                                    clientOutput.flush();
                                }
                                if (availableCI == 0 && availableSI == 0) {
                                    now = new Date().getTime();
                                    Thread.sleep(100);
                                }
                            }
                        } catch (Throwable t) {
                            t.printStackTrace(System.err);
                        } finally {
                            try {
                                clientInput.close();
                                clientOutput.close();
                                serverInput.close();
                                serverOutput.close();
                                socket.close();
                                webSocket.close();
                            } catch (Exception e) {
                                e.printStackTrace(System.err);
                            }
                        }
                    } catch (UnknownHostException e) {
                        OutputStreamWriter writer = new OutputStreamWriter(clientOutput);
                        writer.write("HTTP/1.1 502 Bad Gateway" + "\r\n");
                        writer.flush();
                        socket.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
