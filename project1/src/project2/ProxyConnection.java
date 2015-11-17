package project2;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

public class ProxyConnection implements Runnable {

    private final Socket clientSocket;

    public ProxyConnection(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {

        try {
            InputStream clientInput = clientSocket.getInputStream();
            OutputStream clientOutput = clientSocket.getOutputStream();

            String header = "";

            int intch;
            while ((intch = clientInput.read()) != -1) {
                char ch = (char) intch;
                header += ch;
                if (header.lastIndexOf("\r\n\r\n") != -1) {
                    break;
                }
            }
            
            HTTPHeader httpHeader = new HTTPHeader(header);

            System.out.println(new Date().toString() + " >>> " + httpHeader.hostName + " " + httpHeader.port + " " + httpHeader.uri);
            //System.out.println(httpHeader.newHeader);

            if (httpHeader.method == null || httpHeader.hostName == null || httpHeader.port == -1)
                return;

            if (httpHeader.method.equals("NONCONNECT")) {

            	String body = "";
                if (httpHeader.newHeader.startsWith("POST")) {
	            	while ((intch = clientInput.read()) != -1) {
	                	char ch = (char) intch;
	                    body += ch;
	                }
                }
            	
                Socket webSocket = new Socket(httpHeader.hostName, httpHeader.port);

                OutputStreamWriter writer = new OutputStreamWriter(webSocket.getOutputStream());
                writer.write(httpHeader.newHeader);
                if (body.length() > 0)
                	writer.write(body);
                writer.flush();

                InputStream serverResponse = webSocket.getInputStream();

                byte[] buf = new byte[32767];
                int numOfBytes = serverResponse.read(buf);

                while (numOfBytes != -1) {

                    clientOutput.write(buf, 0, numOfBytes);
                    clientOutput.flush();

                    numOfBytes = serverResponse.read(buf);
                }

                serverResponse.close();
                webSocket.close();
                clientOutput.close();

            } else if (httpHeader.method.equals("CONNECT")) {
                try {
                    Socket webSocket = new Socket(httpHeader.hostName, httpHeader.port);
                    OutputStreamWriter writer = new OutputStreamWriter(clientOutput);
                    writer.write("HTTP/1.1 200 OK" + "\r\n\r\n");
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
                                        //System.out.print((char) ch);
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
                                        //System.out.print(ch);
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
                            clientSocket.close();
                            webSocket.close();
                        } catch (Exception e) {
                            e.printStackTrace(System.err);
                        }
                    }
                } catch (UnknownHostException e) {
                    OutputStreamWriter writer = new OutputStreamWriter(clientOutput);
                    writer.write("HTTP/1.1 502 Bad Gateway" + "\r\n\r\n");
                    writer.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	try {
        		clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
