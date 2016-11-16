package test;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;

public class Server1 {
	private Hashtable outputStreams = new Hashtable();
	private ServerSocket ss;
	public static String OnlineNames = "";

	public Server1(int port) throws IOException {
		listen(port);
	}

	public static void main(String args[]) throws Exception {
		int port = Integer.parseInt(args[0]);
		new Server1(port);
	}

	public void listen(int port) throws IOException {
		ss = new ServerSocket(port);
		System.out.println("Listening on " + ss);
		while (true) {
			Socket s = ss.accept();
			System.out.println("Connection From " + s);
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());
			outputStreams.put(s, dout);
			dout.writeUTF(OnlineNames);
			System.out.println(OnlineNames);
			new ServerThread(this, s);
		}
	}

	Enumeration getOutputStreams() {
		return outputStreams.elements();
	}

	void sendToAll(String message) {
		synchronized (outputStreams) {
			for (Enumeration e = getOutputStreams(); e.hasMoreElements();) {
				DataOutputStream dout = (DataOutputStream) e.nextElement();
				try {
					dout.writeUTF(message);
				} catch (IOException ie) {
					System.out.println(ie);
				}
			}
		}
	}

	void removeConnection(Socket s) {
		synchronized (outputStreams) {
			System.out.println("Removing connection to " + s);
			outputStreams.remove(s);
			try {
				s.close();
			} catch (IOException e) {
				System.out.println("Error in closing " + e);
			}
		}
	}
}
