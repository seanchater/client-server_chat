import java.io.*;
import java.net.*;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Gui extends Thread {

	public static JFrame frame;
	private JTextField textField;
	private JTextField msg;
	private static JTextArea chatroom;
	private static DefaultListModel<String> listModel;
	private static ObjectInputStream in;
	private static ObjectOutputStream out;
	private static Socket socket;
	private static Client_conn c;
	private static String clientusername;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		// connect to hamachi

		if (args.length != 2) {
			System.err.println(
				"Usage: java EchoClient <host name> <port number>");
			System.exit(1);
		}

		String host_name = args[0];
		int port = Integer.parseInt(args[1]);

		// init socket connection and in/out streams

		init(host_name, port);
		
		// create a new thread to listen to the server for any incoming group / direct msgs
		c = new Client_conn(socket, in);
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Gui window = new Gui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Gui() {
		initialize();
	}

	/**
	 * Initialises the socket to the server and the input/output streams of the socket
	 * @param host_name the host of the server
	 * @param port port number of the server
	 */
	public static void init(String host_name, int port) {
		try {
			socket = new Socket(host_name, port);
			// System.out.println("Client socket opened successfully on port " + socket.getPort());
			in = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.err.println("Don't know about host " + host_name);
	 	} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Couldn't get I/O for the connection to " + host_name);
		}
	}

	/**
	 * Verifies that the username is not a duplicate
	 * @param username whatever the user entered
	 * @return true if username is valid, false otherwise
	 */
	public static boolean verify_user(String username) {
		try {
				// and create a user object to write to server
				Message username_msg = new Message(Message.USERNAME, username);
				// System.out.printf("[CLIENT SENT] %s\n", username_msg.toString());
				out.writeObject(username_msg);

				// get response from server
				Message server_resp = (Message) in.readObject();
				// System.out.printf("[CLIENT RECV] %s\n", server_resp.toString());
				// if resp says unique username then allow to chat
				if (server_resp.get_header() == Message.ACCEPT) {
					// System.out.println("username was valid - allowed to chat");
					clientusername = username;
					return true;
				} else if (server_resp.get_header() == Message.REJECT) {
					System.out.println("username not valid - try again");
					return false;
				} else {
					System.err.println("server error when receiving username accept");
					return false;
				}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("I/O error for connection to " + socket.getInetAddress());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.err.printf("Message class (%s) not found when reading resp from socket\n", e.getClass());
		}
		return false;
	}


	/**
	 * Sends a message object to the server
	 * @param user_input whatever the user typed in if it is a group msg or a disconnect
	 * @param dm dm to be sent if the message is a whisper msg
	 */
	public static void send_message(String user_input, Direct_message dm) {
		Message msg;
		try {
			// get msg object from gui method
			if (user_input.equals("exit")) {
				msg = new Message(Message.DISCONNECT, null);
				out.writeObject(msg);
				disconnect();
			} else if (user_input.equals("direct_msg")) {
				msg = new Message(Message.DIRECT, null);
				msg.set_dm(dm);
				out.writeObject(msg);
			} else if (dm == null) {
				msg = new Message(Message.GROUPMSG, user_input);
				// System.out.printf("[SEND] %s\n", user_input);
				out.writeObject(msg);
			}  
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error when reading user input");
		}
	}

	public static void disconnect() {
		try {
			if (in != null) in.close();
		} catch (Exception e) {}
		try {
			if (out != null) out.close();
		} catch (Exception e) {}
		try {
			if (socket != null) socket.close();
		} catch (Exception e) {}

		// close the gui
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblConnectedUsers = new JLabel("Enter a username");
		lblConnectedUsers.setBounds(37, 78, 159, 15);
		frame.getContentPane().add(lblConnectedUsers);
		
		JButton btnNewButton = new JButton("Submit");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (c.isAlive()) {
					send_message(textField.getText(),null);
				} else {
					if (verify_user(textField.getText())) {
						// draw room gui
						frame.dispose();
						initializenew();

						// start listening for incoming updates
						c.start();
					} else {
						// display error msg
					}
				}
				
				
			}
		});
		btnNewButton.setBounds(276, 73, 117, 25);
		frame.getContentPane().add(btnNewButton);
		
		textField = new JTextField();
		textField.setBounds(260, 145, 164, 70);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
	}

	private void initializenew() {
		frame = new JFrame();
		frame.setBounds(100, 100, 961, 632);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				send_message("exit", null);
			}
			  });
		frame.getContentPane().setLayout(null);
		frame.setVisible(true);
		
		chatroom = new JTextArea();
		chatroom.setBounds(290, 73, 630, 456);
		chatroom.setEditable(false);
		chatroom.setLineWrap(true);
		frame.getContentPane().add(chatroom);
		
		
		JLabel lblNewLabel = new JLabel("Connected users");
		lblNewLabel.setBounds(82, 30, 167, 33);
		frame.getContentPane().add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Messages ");
		lblNewLabel_1.setBounds(374, 30, 140, 26);
		frame.getContentPane().add(lblNewLabel_1);
		
		msg = new JTextField();
		msg.setBounds(290, 541, 404, 26);
		frame.getContentPane().add(msg);
		msg.setColumns(10);
		
		JButton btnNewButton = new JButton("Send message");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chatroom.append(msg.getText()+"\n");
				send_message(msg.getText(),null);
				msg.setText("");
			}
		});
		btnNewButton.setBounds(749, 558, 171, 25);
		frame.getContentPane().add(btnNewButton);
		
		JPanel panel = new JPanel();
		panel.setBounds(42, 73, 236, 456);
		frame.getContentPane().add(panel);
		
		listModel = new DefaultListModel<String>();
		listModel.addElement("one");
		listModel.addElement("twee");
		JList list = new JList(listModel);
		panel.add(list);
		
		JButton btnNewButton_1 = new JButton("Direct message");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (list.isSelectionEmpty()) {
					 JOptionPane.showMessageDialog(null, "Select a user to direct message", "InfoBox: ", JOptionPane.INFORMATION_MESSAGE);
				} else {
					Direct_message drcmsg = new Direct_message(clientusername, list.getSelectedValue().toString(), msg.getText());
					send_message("direct_msg",drcmsg);
					list.clearSelection();
					msg.setText("");
				}
			}
		});
		btnNewButton_1.setBounds(52, 558, 167, 25);
		frame.getContentPane().add(btnNewButton_1);
		
		
	}
	public static void redraw(Client_obj pattern) {
		chatroom.setText("");
		for (String groupmsg : pattern.get_groupmsg()) {
			chatroom.append(groupmsg+"\n");
		}
		listModel.clear();
		for (String username : pattern.get_users()) {
			listModel.addElement(username);
		}
		for (Direct_message dr : pattern.get_dm()) {
			if (dr.get_to().equals(clientusername)) {
				JOptionPane.showMessageDialog(frame, dr.get_msg(), "Wisper message from "+dr.get_from(), JOptionPane.INFORMATION_MESSAGE);
			}
		}

	}
	public static void serverclose() {
		JOptionPane.showMessageDialog(null, "Disconected from server client will terminate", "Server Disconnect ", JOptionPane.INFORMATION_MESSAGE);
	}

}