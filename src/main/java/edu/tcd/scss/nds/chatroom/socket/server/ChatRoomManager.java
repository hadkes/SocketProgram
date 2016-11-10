package edu.tcd.scss.nds.chatroom.socket.server;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.tcd.scss.nds.chatroom.socket.models.ChatRoom;
import edu.tcd.scss.nds.chatroom.socket.models.ChatUser;

public class ChatRoomManager {
	public static ChatRoomManager instance;
	
	private volatile Set<ChatRoom> chatRooms; 
	private volatile Set<ChatUser> chatUsers; 
	
	private ChatRoomManager(){
		chatRooms = new HashSet<ChatRoom>();
		chatUsers = new HashSet<ChatUser>();
	}
	
	public static ChatRoomManager getInstance(){
		if(instance == null){
			instance = new ChatRoomManager();
		}
		return instance;		
	}
	
	public ChatRoom createChatRoom(String chatRoomName){
		for (Iterator<ChatRoom> iterator = chatRooms.iterator(); iterator.hasNext();) {
			ChatRoom chatRoom = (ChatRoom) iterator.next();
			if(chatRoom.getName().equals(chatRoomName)){
				System.out.println("Chat room exist with id "+chatRoom.getId());
				return chatRoom;
			}
		}
		
		System.out.println("Creating new chat room. name "+chatRoomName);

		ChatRoom chatRoom = new ChatRoom();
		chatRoom.setName(chatRoomName);
		chatRooms.add(chatRoom);
		
		System.out.println("New chat room has been creaed with id "+chatRoom.getId());
		return chatRoom;
	}
	
	public ChatRoom getChatRoom(String id){
		for (Iterator<ChatRoom> iterator = chatRooms.iterator(); iterator.hasNext();) {
			ChatRoom chatRoom = (ChatRoom) iterator.next();
			if(chatRoom.getId().equals(id)){
				return chatRoom;
			}
		}
		return null;
	}
	
	public ChatUser createChatUser(String userName, String clientIp, String port){
		System.out.println("Creating new chat user with name "+userName);
		
		ChatUser newUser = new ChatUser();
		newUser.setName(userName);
		newUser.setHost(clientIp);
		newUser.setPort(port);
		chatUsers.add(newUser);

		System.out.println("Create new chat user with id "+newUser.getId());
		return newUser;
	}
	
	public void addChatUserToRoom(ChatUser user, ChatRoom room, Socket clientSocket){
		user.setClientSocket(clientSocket);	
		room.addClientSocket(clientSocket);
		
		// TODO: this association is not need as we are maintaining client socket. In future remove this association
		room.addUser(user);
	}
	
	public void removeUserSocketFromRoom(String roomId, Socket clientSocket){
		ChatRoom room = getChatRoom(roomId);
		if(room != null){
			Set<Socket> chatRoomSockets = room.getClientSockets();
			boolean isRemoved = chatRoomSockets.remove(clientSocket);
			System.out.println("Client socket removed from chatroom? "+isRemoved);
		}
	}
	
	private ChatUser getChatUser(String userId){
		for (Iterator<ChatUser> iterator = chatUsers.iterator(); iterator.hasNext();) {
			ChatUser user = (ChatUser) iterator.next();
			if(user.getId().equalsIgnoreCase(userId)){
				return user;
			}
		}
		return null;
	}
	
	public void handleChatInRoom(String inputLine, Socket clientSocket){
		System.out.println("Inside chat room");
		// get users based on socket
		ChatUser typingUser = null;
		for (Iterator<ChatUser> iterator = chatUsers.iterator(); iterator.hasNext();) {
			ChatUser user = (ChatUser) iterator.next();
			if(user.getClientSocket().equals(clientSocket)){
				typingUser = user;
			}
		}
		
		System.out.println("typing user "+typingUser);
		// get chat room of login user and socket; possibly only socket
		if(typingUser != null){
			ChatRoom typingRoom = null;
			for (Iterator<ChatRoom> iterator = chatRooms.iterator(); iterator.hasNext();) {
				ChatRoom chatRoom = (ChatRoom) iterator.next();
				Set<ChatUser> chatUsers = chatRoom.getUsers();
				for (Iterator<ChatUser> iterator2 = chatUsers.iterator(); iterator2.hasNext();) {
					ChatUser chatUser = (ChatUser) iterator2.next();
					if(chatUser.getName().equals(typingUser.getName())){
						Set<Socket> clientSockets = chatRoom.getClientSockets();
						for (Iterator<Socket> iterator3 = clientSockets.iterator(); iterator3.hasNext();) {
							Socket socket = (Socket) iterator3.next();
							if(socket.equals(clientSocket)){
								typingRoom = chatRoom;
								break;
							}
						}
					}
				}
			}
			
			System.out.println("typing room "+typingRoom);
			if(typingRoom != null){
				Set<Socket> clientSockets = typingRoom.getClientSockets();
				for (Iterator<Socket> iterator = clientSockets.iterator(); iterator.hasNext();) {
					Socket toSendSocket = (Socket) iterator.next();
					if(toSendSocket.isConnected() && !clientSocket.equals(toSendSocket)){ //do not send message to self
						PrintWriter out = null;
						try{ 
							out = new PrintWriter(toSendSocket.getOutputStream(), true);
							out.println(inputLine);
						}catch(Exception ex){
							// dont care
						}
					}						
				}
			}
		}		
	} 
	

	
	//TODO: maintain chat user at one place	
	public void removeClient(String roomId, String userId, Socket clientSocket){
		ChatRoom room = getChatRoom(roomId);
		if(room != null){
			Set<Socket> chatRoomSockets = room.getClientSockets();
			chatRoomSockets.remove(clientSocket);
			System.out.println("Client socket removed from chatroom");
			
			// overhead, chat room does not need list of users. all it need a user socket
			Set<ChatUser> users = room.getUsers();
			for (Iterator<ChatUser> iterator = users.iterator(); iterator.hasNext();) {
				ChatUser chatUser = (ChatUser) iterator.next();
				if(chatUser.getName().equals(userId)){
					iterator.remove();
					System.out.println("User "+userId+" is removed from room.");
					break;
				}
			}
			ChatUser toRemote = getChatUser(userId);
			chatUsers.remove(toRemote);
		}
	}
	
	public ChatUser getChatUserBasedOnSocket(Socket socket){
		for (Iterator<ChatUser> iterator = chatUsers.iterator(); iterator.hasNext();) {
			ChatUser chatUser = (ChatUser) iterator.next();
			if(chatUser.getClientSocket().equals(socket)){
				return chatUser;
			}			
		}
		return null;
	}
}
