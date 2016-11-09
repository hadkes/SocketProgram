package edu.tcd.scss.nds.chatroom.socket.server;

import java.net.Socket;

public interface Task {
	void perform(Socket clientSocket);
}
