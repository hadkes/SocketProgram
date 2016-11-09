package edu.tcd.scss.nds.threadpool.socket.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketClient {
	public static void main(String[] args) throws IOException {
        
        if (args.length != 2) {
            System.err.println(
                "Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }
 
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
 
        try {
            Socket echoSocket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        
            System.out.println("Client connection established.");
            System.out.println("For server response type in - helo.");
            System.out.println("To exit type in ;- KILL_SERVICE.");
            
            String serverInput;
            while ((serverInput = stdIn.readLine()) != null) {
                out.println(serverInput);
                
                String serverResponse = in.readLine();
                System.out.println("echo: " + serverResponse);
                if(serverResponse.equalsIgnoreCase("Client connection closed")){
                	System.out.println("Breaking the client loop and making it free to exit.");                	
                	break;
                }
            }
            System.out.println("Terminating client process.");
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        } 
    }
}
