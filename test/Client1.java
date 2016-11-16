package test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JOptionPane;

public class Client1 extends Frame implements Runnable {
	private TextField tf1;
	private TextField tf2;
	private TextArea ta;
	private TextArea to;
	private Socket socket;
	private DataOutputStream dout;
	private DataInputStream din;
	private int count = 0;

	public Client1(String host, int port) {
		String name = JOptionPane.showInputDialog(null, "Enter Your Name");
		tf1 = new TextField(name + ":");
		tf1.setEditable(false);
		tf2 = new TextField();
		ta = new TextArea();
		to = new TextArea("People Online:n", 50, 16);
		ta.setEditable(false);
		to.setEditable(false);
		tf2.requestFocus();
		setLayout(new BorderLayout());
		add("North", tf1);
		add("South", tf2);
		add("Center", ta);
		add("West", to);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		tf2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				processMessage(ae.getActionCommand());
			}
		});
		try {
			socket = new Socket(host, port);
			System.out.println("Connected to " + socket);
			din = new DataInputStream(socket.getInputStream());
			dout = new DataOutputStream(socket.getOutputStream());
			new Thread(this).start();
			processMessage(tf1.getText() + "^^^^^^");
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	private void processMessage(String message) {
		try {
			dout.writeUTF(tf1.getText() + ":" + message);
			tf2.setText("");
		} catch (IOException ie) {
			System.out.println(ie);
		}
	}

	public void run() {
		try {
			while (true) {
				String message = din.readUTF();
				if (count == 0 && !(message.equals(null))) {
					System.out.println(message);
					String[] names = message.split(" ");
					int i = 0;
					while (i < names.length) {
						to.append(names[i] + "n");
						i++;
					}
					count++;
				} else {
					boolean name = true;
					int len = message.length();
					for (int i = 0; i < 6; i++) {
						if (!(message.charAt(len - i - 1) == '^')) {
							name = false;
							System.out.println(message.charAt(len - i - 1));
							break;
						}
					}
					if (name == false) {
						ta.append(message + "n");
					} else {
						String name1 = "";
						int i = 0;
						while (!(message.charAt(i) == ':' && message
								.charAt(i + 1) == ':')) {
							name1 = name1 + message.charAt(i);
							i++;
						}
						to.append(name1 + "n");
					}
				}
			}
		} catch (IOException ie) {
			System.out.println(ie);
		}
	}

	public static void main(String args[]) {
		Client1 obj = new Client1("IP Address of server", 8888);
		obj.setSize(new Dimension(500, 500));
		obj.setVisible(true);
		obj.setTitle("Chatting Client");
	}
}