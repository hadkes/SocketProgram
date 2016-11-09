package edu.tcd.scss.nds.threadpool.socket.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
	public static void main(String[] args) {
		if (args.length != 1) {
            System.err.println("Usage: java SocketServer <port number>");
            System.exit(1);
        }
		
		int portNumber = Integer.parseInt(args[0]);
		SocketServer server = new SocketServer();
		server.startServer(portNumber);		
	}
	
	
	public void startServer(int portNumber){
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        
        try {
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Echo (one-2-one) Socket server is up and running.");
        } catch (IOException ex){
        	System.out.println("Error connecting to server. Please check port is available to connect.");
        }
        
        try{
	        while (true){
	        	clientSocket = serverSocket.accept(); // this will wait till some client connect to server.
	        	System.out.println("Connection established with cilent "+clientSocket.getInetAddress());
	        	
	        	ThreadPoolManager responderThread = ThreadPoolManager.getInstance();
	        	responderThread.performTask(clientSocket);
	        }           
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
        
        finally{
        	if(serverSocket != null){
        		try{
        			serverSocket.close();
        		}catch(Exception ex){
        			//i don't care about it
        			System.out.println("Closing server socket.");
        			ex.printStackTrace(System.err);
        		}
        		
        	}
        }
	}
}
