package edu.tcd.scss.nds.threadpool.socket.server;

import java.net.Socket;

public class ResponderThread extends Thread{
	public static final String STATUS_BUSY = "BUSY";
	public static final String STATUS_AVAILABLE = "AVAILABLE";
	
	ResponseTask task = null;
	private Socket clientSocket = null;
	private volatile String status;

	public ResponderThread(){
		status = new String();
		task = new ResponseTask();
	}
	
	public void setClientSocket(Socket clientSocket){
		this.clientSocket = clientSocket;
	}
	
	public String getRunnbingStatus() {
		return status;
	}
	
	public void setRunningStatus(String status) {
		this.status = status;
	}

	public void run() {
		while(true){
			if(status.equals(STATUS_BUSY)){
				System.out.println("For thread "+getName()+" the state has changed to "+STATUS_BUSY);
				task.performTask(clientSocket);
				System.out.println("Seems like client has lost connection or explicitly closed connection. Now the thread "+getName()+" with state "+getRunnbingStatus()+" will be available for other client.");
			}
		}
	}
}
