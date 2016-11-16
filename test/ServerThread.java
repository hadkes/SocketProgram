package test;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public class ServerThread extends Thread {
	private Server1 server;
	private Socket socket;

	public ServerThread(Server1 server, Socket socket) {
		System.out.println("Hai");
		this.server = server;
		this.socket = socket;
		this.start();
	}

	public void run() {
		try {
			DataInputStream din = new DataInputStream(socket.getInputStream());
			while (true) {
				String message = din.readUTF();
				System.out.println(message);
				boolean name = true;
				int len = message.length();
				for (int i = 0; i < 6; i++) {
					if (!(message.charAt(len - i - 1) == '^')) {
						name = false;
						break;
					}
				}
				if (name == true) {
					String name1 = "";
					int i = 0;
					while (!(message.charAt(i) == ':' && message.charAt(i + 1) == ':')) {
						Server1.OnlineNames = Server1.OnlineNames
								+ message.charAt(i);
						i++;
					}
					Server1.OnlineNames = Server1.OnlineNames + " ";
				}
				server.sendToAll(message);
			}
		} catch (EOFException e) {
		} catch (IOException ie) {
			ie.printStackTrace();
		} finally {
			server.removeConnection(socket);
		}
	}
}
