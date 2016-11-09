package edu.tcd.scss.nds.chatroom.socket.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ResponseTask implements Task{
	
	public void perform(Socket clientSocket){
		PrintWriter out = null;
		try{ 
			out = new PrintWriter(clientSocket.getOutputStream(), true);                   
			out.println("server> To join existing room or create a new room type in JOIN_CHATROOM:<name of chat room>");
			out.println("server> To leave room type in LEAVE_CHATROOM:<name of chat room>");
		} catch (IOException ex){
			System.out.println("Error trying to receive/send data from/to client.");
		}
	}
}
