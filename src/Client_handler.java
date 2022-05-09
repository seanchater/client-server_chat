import java.io.*;
import java.net.*;

public class Client_handler extends Thread {

	private Socket client_socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String username;

	public Client_handler(Socket socket) {
		this.client_socket = socket;
	}

	public void run() {
		try {
			out = new ObjectOutputStream(client_socket.getOutputStream());
			in = new ObjectInputStream(client_socket.getInputStream());
			System.out.println("[SERVER] new client connection opened successfully on port " + client_socket.getPort());           
			// verify username
			boolean name_unique = verify_username();
			if (name_unique) {
				Server_app.update_global(null, username, 1);
			}

			// wait for updates from clients 
			wait_for_updates();
			
		} catch (IOException e) {
			System.out.println("Exception caught when trying to listen on port "
				+ client_socket.getPort() + " or listening for a connection");
			System.out.println(e.getMessage());
		} 
	}

	public ObjectOutputStream get_out() {
		return this.out;
	}

	public boolean username_unique(String username) {
		return Server_app.username_unique(username);
	}

	public String get_username() {
		return this.username;
	}

	public void disconnect() {
		try {
			if (in != null) in.close();
		} catch (Exception e) {}
		try {
			if (out != null) out.close();
		} catch (Exception e) {}
		try {
			if (client_socket != null) client_socket.close();
		} catch (Exception e) {}
	}

	private void wait_for_updates() {
		Message msg;
		boolean connected = true;
		while (connected) {
			try {
				msg = (Message) in.readObject();
				switch (msg.get_header()) {
					case Message.GROUPMSG:
						Server_app.update_global(msg, null, 0);
						break;

					case Message.DIRECT:
						Server_app.update_global(msg, null, 0);
						break;
					
					case Message.DISCONNECT:
						Server_app.update_global(msg, this.username, 0);
						connected = false;
						disconnect();
						System.out.printf("[SERVER] %s disconnected\n", this.username);
						break;

					default: 
						break;
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				System.err.printf("Message class (%s) not found when reading resp from socket\n", e.getClass());
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("I/O error for connection to " + client_socket.getInetAddress());
			} 
		
		}
	}

	private boolean verify_username() {
					Message username_msg;
					String username;
					while (true) {
						try {
							username_msg = (Message) in.readObject();
							username = username_msg.get_data();
							// System.out.printf("[SERVER RECV] %s\n", username_msg.toString());

							// add to bigd
							if (username_unique(username)) {
								// send some object back saying that connection was accepted
								Message resp = new Message(Message.ACCEPT, null);
								out.writeObject(resp);
								// System.out.printf("[SERVER SENT] %s\n", resp.toString());
								System.out.printf("[SERVER] %s now connected\n", username);
								this.username = username;
								return true;
							} else {
								// send some object back saying connection was rejected incorrect username or something
								Message resp = new Message(Message.REJECT, null);
								out.writeObject(resp);
								// System.out.printf("[SERVER SENT] %s\n", resp.toString());
								// System.out.println("invalid username");
							}
						}  catch (IOException e) {
							System.err.println("Couldn't get I/O for the connection to " +
								client_socket.getInetAddress());
							System.exit(1);
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
							System.err.printf("Message class (%s) not found when reading resp from socket\n", e.getClass());
						} 
					}
			}
		

}