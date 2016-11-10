package edu.tcd.scss.nds.threadpool.socket.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ResponseTask {
	
	public void performTask(Socket clientSocket){
		try{ 
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);                   
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
       
			String inputLine;
			while ((inputLine = in.readLine()) != null) {				
				ResponderThread resThread = (ResponderThread)Thread.currentThread();
				System.out.println("Client input is "+inputLine+" inside thread "+resThread.getName()+" and thread status is "+resThread.getRunnbingStatus());
				if ((inputLine == null) || inputLine.equalsIgnoreCase("KILL_SERVICE")) {
					
					// release the task
					ThreadPoolManager.getInstance().releaseTask();
					
					// close client socket, the return text is used to terminate the client
					out.println("Client connection closed");
					clientSocket.close();

					
					
					System.out.println("Maximum pool size "+ThreadPoolManager.getInstance().getMaximumPoolSize());
					System.out.println("Number of threads in pool "+ThreadPoolManager.getInstance().getCurrentPoolSize());
					System.out.println("Number of available threads "+ThreadPoolManager.getInstance().getAvailableThreads());
					System.out.println("Number of busy threads "+ThreadPoolManager.getInstance().getBusyThreads());
                    return;
                } else if ((inputLine == null) || inputLine.equalsIgnoreCase("helo")){
                	out.println("Response from server: "+inputLine+ "IP address: "+clientSocket.getInetAddress() + "Port number: "+clientSocket.getPort() + "Student Id: hadkes");
                } else {
                	out.println("Sorry, incorrect input. Please type in HELO or helo.");
                }
			}
		} catch (IOException ex){
			System.out.println("Error trying to receive/send data from/to client.");
		}
	}
}
