package project2;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class NonConnectProxyConnection implements Runnable {


    private final Socket socket;

    public NonConnectProxyConnection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String hostName;
            String info;

            while ((info = reader.readLine()) != null) {
                System.out.println(info);
                if (info.startsWith("GET") || info.startsWith("POST") || info.startsWith("PUT") || info.startsWith("CONNECT")) {
                    info = info.replace("HTTP/1.1", "HTTP/1.0");
                }

                int splitPos = info.indexOf(": ");
                if (splitPos != -1) {
                    String param = info.substring(0, splitPos).trim();
                    String value = info.substring(splitPos + 2).trim();

                    switch (param) {
                        case "Host":
                            hostName = value;
                            break;
                        case "Connection":
                            value = "close";
                            info = param + ": " + value;
                            break;
                    }
                }
                System.out.println(info);
            }

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
