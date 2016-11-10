package edu.tcd.scss.nds.chatroom.socket.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import edu.tcd.scss.nds.chatroom.socket.models.ChatRoom;
import edu.tcd.scss.nds.chatroom.socket.models.ChatUser;

public class RequestTask implements Task{

	public void perform(Socket clientSocket){
		BufferedReader in = null;
		PrintWriter out = null;
		try{ 
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			out.println("server> To join existing room or create a new room type in JOIN_CHATROOM:<name of chat room>");
			out.println("server> To leave room type in LEAVE_CHATROOM:<[ROOM_REF]>");
			out.println("server> To disconnect room type in DISCONNECT:<IP address of client>");
			
			String roomName = null;
			String clientAddress = null;
			String port = null;
			String clientName = null;

			String leavingRoomId = null;
			String leavingUserId = null;
			
			String disconnectIP = null;

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				PooledThread resThread = (PooledThread)Thread.currentThread();
				System.out.println("Client input is "+inputLine+" inside thread "+resThread.getName()+" and thread status is "+resThread.getRunnbingStatus());

				if ((inputLine == null) || inputLine.startsWith("JOIN_CHATROOM")) {
					roomName = inputLine.substring(inputLine.indexOf(':')+1, inputLine.length());
					out.println("server>Enter CLIENT_IP:<client host>");
				} else if ((inputLine == null) || inputLine.startsWith("CLIENT_IP")) {
					clientAddress = inputLine.substring(inputLine.indexOf(':')+1, inputLine.length());
					out.println("server>Enter PORT:<client port>");
				} else if ((inputLine == null) || inputLine.startsWith("PORT")) {
					port = inputLine.substring(inputLine.indexOf(':')+1, inputLine.length());
					out.println("server>Enter CLIENT_NAME:<client name>");
				} else if ((inputLine == null) || inputLine.startsWith("CLIENT_NAME")) {
					clientName = inputLine.substring(inputLine.indexOf(':')+1, inputLine.length());					
				} else if ((inputLine == null) || inputLine.startsWith("LEAVE_CHATROOM")) {
					port = null;
					clientName = null;
					leavingRoomId = inputLine.substring(inputLine.indexOf(':')+1, inputLine.length());
					boolean leaveRoom = doesUserWantToLeaveRoom(leavingRoomId, leavingUserId, clientName, clientSocket);
					if(!leaveRoom){
						out.println("server>Enter JOIN_ID:<user id>");
					} else {
						leaveAndCleanUp(leavingRoomId, out, clientSocket);
					}
				} else if ((inputLine == null) || inputLine.startsWith("JOIN_ID")) {
					leavingUserId = inputLine.substring(inputLine.indexOf(':')+1, inputLine.length());
					out.println("server>Enter CLIENT_NAME:<client name>");					
				} else if ((inputLine == null) || inputLine.startsWith("DISCONNECT")) {
					disconnectIP = inputLine.substring(inputLine.indexOf(':')+1, inputLine.length());
					out.println("server>Enter PORT:<client port>");
					// Avoid using stale information
					port = null;
					clientName = null;
				}
				else {
					ChatUser chatUser = ChatRoomManager.getInstance().getChatUserBasedOnSocket(clientSocket);
					String senderStr = chatUser.getName()+"("+chatUser.getId()+")";
					System.out.println("sender "+senderStr);
					ChatRoomManager.getInstance().handleChatInRoom(senderStr+">"+ inputLine, clientSocket);
				}

				// create new room and user
				if(roomName != null && clientAddress != null && port != null && clientName != null){
					ChatRoomManager manager = ChatRoomManager.getInstance();
					ChatRoom room = manager.createChatRoom(roomName);
					ChatUser user = manager.createChatUser(clientName, clientAddress, port);
					manager.addChatUserToRoom(user, room, clientSocket);
												
					System.out.println("User "+user.getName()+" is connected to chat room "+room.getName());
					out.println("server> JOINED_CHATROOM: "+room.getName());
					out.println("server> SERVER_IP: "+user.getHost());
					out.println("server> PORT: "+user.getPort());
					out.println("server> ROOM_REF: "+room.getId());
					out.println("server> JOIN_ID: "+user.getId());
					out.println("server> CLIENT_NAME: "+user.getName());
					
					// inform all about addition of new member in chat room
					ChatRoomManager.getInstance().handleChatInRoom("server> "+user.getName()+" has joined the "+room.getName()+ " room.", clientSocket);
					
					roomName = null;
					clientAddress = null;
					port = null;
					clientName = null;

				}
				
				// disconnect user from chat room
				if(leavingRoomId != null && leavingUserId != null && clientName != null){
					boolean leaveRoom = doesUserWantToLeaveRoom(leavingRoomId, leavingUserId, clientName, clientSocket);
					if(leaveRoom){
						leaveAndCleanUp(leavingRoomId, out, clientSocket);
					}
				}
				
				// disconnect user from chat program
				if(disconnectIP != null && port != null && clientName != null){
					System.out.println("Disconnecting client "+clientName+" from room with id "+leavingRoomId);
					disconnectAndCleanUp(leavingRoomId, clientSocket);
					disconnect(in, out, clientSocket);
					break;
				}
			}
		} catch (IOException ex){
			System.out.println("Error trying to receive/send data from/to client.");
		} finally{
			Thread currentThread = Thread.currentThread();
			if(currentThread instanceof PooledThread){
				PooledThread requestThread = (PooledThread)currentThread;
				System.out.println("Changing state of " +requestThread.getName()+ " thread from "+requestThread.getRunnbingStatus()+" to "+PooledThread.STATUS_AVAILABLE);
				requestThread.setRunningStatus(PooledThread.STATUS_AVAILABLE);
			}
		}
	}
	
	private boolean doesUserWantToLeaveRoom(String leavingRoomId, String leavingUserId, String leavingClientName, Socket clientSocket){
		ChatRoomManager manager = ChatRoomManager.getInstance();
		ChatRoom room = manager.getChatRoom(leavingRoomId);
		System.out.println("For chat room with id "+leavingRoomId+ " found "+room +" room.");
		
		// taking care of leaving room after user has joined it
		if(leavingRoomId != null && leavingUserId != null && leavingClientName != null){
			if(room != null){
				return true;
			}			
		} else if(leavingRoomId != null){ // taking care of leaving room with out/before joining
			if(room == null){
				return true;
			}
		}
		return false;
	}

	private void leaveAndCleanUp(String roomId, PrintWriter out, Socket clientSocket){
		if(roomId != null){
			ChatUser chatUser = ChatRoomManager.getInstance().getChatUserBasedOnSocket(clientSocket);
			if(chatUser != null){
				String userId = chatUser.getId();
				
				// send message to leaving user
				out.println("LEFT_CHATROOM:"+roomId);
				out.println("JOIN_ID:"+userId);
				
				// send message to room about leaving user
				ChatRoomManager.getInstance().handleChatInRoom("server>"+chatUser.getName()+"("+chatUser.getId()+") has left the room", clientSocket);
				
				System.out.println("Removing user from chat room");
				ChatRoomManager.getInstance().removeUserSocketFromRoom(roomId, clientSocket);
			}			
		}
	}
	
	private void disconnectAndCleanUp(String roomId, Socket clientSocket){
		if(roomId != null){
			ChatUser chatUser = ChatRoomManager.getInstance().getChatUserBasedOnSocket(clientSocket);
			if(chatUser != null){
				String userId = chatUser.getId();
				
				System.out.println("Removing client from model");
				ChatRoomManager.getInstance().removeClient(roomId, userId, clientSocket);

			}			
		}
	}

	private void disconnect(BufferedReader in, PrintWriter out, Socket clientSocket){
		// let client know about connection closer so that client can free its resources
		out.println("Client connection closed");

		try{
			// close the streams
			if(in != null){
				in.close();
			}
			if(out != null){
				out.close();
			}
			
			//close client socket
			clientSocket.close();			

			// release the task
			ThreadPoolManager.getInstance().releaseTask();
		}catch(Exception ex){
			// i don't care;
		}
	}
}
