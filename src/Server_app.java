import java.net.*;
import java.io.*;
import java.util.ArrayList;

/**
 * How to run:
 * java EchoServer 5000
 */
public class Server_app {

	// stores info on all connected clients
	private static Client_obj global_pool;
	private static ArrayList<Client_handler> clients;
    public static void main(String[] args) throws IOException {
        
        if (args.length != 1) {
            System.err.println("Usage: java EchoServer <port number>");
            System.exit(1);
        }
        
        int port_number = Integer.parseInt(args[0]);
		global_pool = new Client_obj();
		clients = new ArrayList<Client_handler>();

		/**
		 * open single server socket. The main thread listens on the given port for incoming 
		 * client connections and creates a new thread with a connection to the server for each 
		 * one that comes in
		 */
		try (ServerSocket serv_sock = new ServerSocket(port_number)) {
			while (true) {
				System.out.println("[SERVER] waiting for new client connection");
				Socket client_sock = serv_sock.accept();
				// System.out.println("New client conn opened: " + client_sock.getInetAddress().getHostAddress());
				Client_handler c = new Client_handler(client_sock);
				clients.add(c);
				c.start();
			}
		} catch (IOException e) {
			System.err.println("Error when attempting to listen on port" + port_number);
			e.printStackTrace();
		}
    }



	/**
	 * Update the global data structure that stores all the groupchat messagse / dms  / users
	 * @param msg msg containing either a group msg/ dm / username to be removed
	 * @param username username to be updated
	 * @param action 0: if updating group messages or direct messages or removing username
	 * 				 1: if updating  usernames
	 * 				 default: error
	 */
	public synchronized static void update_global(Message msg, String username, int action) {
		/**
		 * can have multiple sync methods apparantly, can make each method in Client_obj.java synchronised and then
		 * all accesses to that instances (ie: global_pool) of that object will be synchronise
		 */
		switch (action) {
			case 0:
				if (msg.get_header() == Message.GROUPMSG) {
					global_pool.update_group(msg.get_data());
				} else if (msg.get_header() == Message.DIRECT) {
					global_pool.update_direct(msg.get_dm());
				} else if (msg.get_header() == Message.DISCONNECT) {
					global_pool.remove_user(username);
					// remove client that is disconnecting from the list of clients conected
					Client_handler disconnected_user = null;
					for (Client_handler c : clients) {
						if (c.get_username().equals(username)) {
							disconnected_user = c;
						}
					}
					clients.remove(disconnected_user);
				}
				break;

			case 1:
				global_pool.update_users(username);
				break;

			default:
				System.err.println("error when updating global_pool");
				break;
		}

		// System.out.println(global_pool.toString());

		// broadcast updates to all clients
		broadcast();
		// clear all direct messages for the next broadcast to clients
		global_pool.clear_direct();
	}

	/**
	 * Broadcast the updates to all the other client threads coz there was an update to the global_pool
	 */
	private static void broadcast() {
		for (int i = 0; i < clients.size(); i++) {
			Client_handler c = clients.get(i);
			write_to_client(c);
		}
	}
	
	/**
	 * Write updates to client side whenever global_pool gets updated
	 */
	public static void write_to_client(Client_handler c) {
		try {
			c.get_out().writeObject(global_pool);
			c.get_out().reset();
			// System.out.println("[SERVER SENT] global_pool: "+ global_pool.toString());
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("I/O error for connection to ");
		}
	}	

	public static boolean username_unique(String username) {
		return global_pool.username_unique(username);
	}

}