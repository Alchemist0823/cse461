package project2;

import java.util.Scanner;

public class HTTPHeader {
    String newHeader = "";
    String uri = null;
    String hostName = null;
    String method = null;
    int port = -1;

    public HTTPHeader(String header) {
        Scanner scanner = new Scanner(header);
        if (scanner.hasNext()) {
	        String initial = scanner.nextLine();
	        	
	        if (initial.startsWith("CONNECT")) {
	            method = "CONNECT";
	            uri = initial.split(" +")[1];
	        }
	        if (initial.startsWith("GET") || initial.startsWith("POST")) {
	            method = "NONCONNECT";
	            uri = initial.split(" +")[1];
	            initial = initial.replace("HTTP/1.1", "HTTP/1.0");
	        }
	
	        newHeader = initial + "\r\n";
	
	        while (scanner.hasNext()) {
	            String line = scanner.nextLine();
	
	            int splitPos = line.indexOf(": ");
	            if (splitPos != -1) {
	                String param = line.substring(0, splitPos).trim();
	                String value = line.substring(splitPos + 2).trim();
	
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
	                        line = param + ": " + value;
	                        break;
	                    case "Proxy-Connection":
	                        value = "close";
	                        line = param + ": " + value;
	                        break;
	                }
	            }
	            newHeader +=  line + "\r\n";
	        }
	        
	        newHeader += "\r\n";
	
	        if (hostName != null) {
	            if (port == -1) {
	                if (uri != null) {
	                    if (uri.startsWith("http://"))
	                        port = 80;
	                    else if (uri.startsWith("https://"))
	                        port = 443;
	                    else if (uri.indexOf(":") != -1) {
	                        try {
	                            port = Integer.parseInt(uri.split(":")[1]);
	                        } catch (NumberFormatException e) {
	                            port = -1;
	                        }
	                    }
	                }
	            }
	        }
        }
    }
}
