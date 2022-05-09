import java.io.*;
import java.net.*;

/**
 * Listens for incoming chatroom msgs (or direct msgs or new user updates) from the server to update the gui
 */
public class Client_conn extends Thread {

	private Socket socket;
	private ObjectInputStream in;

	public Client_conn(Socket socket, ObjectInputStream in) {
		this.socket = socket;
		this.in = in;
	}

	public void run() {
		try {
			Client_obj server_resp;
			while (true) {
				// System.out.println("waiting for data from server"); 
				// // get response from server
				server_resp = (Client_obj) in.readObject();
				// System.out.println("[RECV] " + server_resp.toString());
				// draw gui
				Gui.redraw(server_resp);
			}
	} catch (UnknownHostException e) {
		System.err.println("Don't know about host " + socket.getInetAddress());
		System.exit(1);
	} catch (IOException e) {
		System.err.println("Couldn't get I/O for the connection to " +
			socket.getInetAddress());
		Gui.serverclose();
		System.exit(1);
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
		System.err.printf("Message class (%s) not found when reading resp from socket\n", e.getClass());
	} 
	}
		
}