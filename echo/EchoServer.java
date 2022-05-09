import java.net.*;
import java.io.*;

/**
 * How to run:
 * java EchoServer 5000
 */
public class EchoServer {

	private static class Server_conn implements Runnable {
		private int port_number;
		private Socket clientSocket;

		public Server_conn(int port, Socket sock) {
			port_number = port;
			clientSocket = sock;
		}

		public void run() {
			try (
				/*
					* datainputstream not neccessarily safe for multithreaded access. ALthough we will
					* only be doing that if the server is reading input from clients from a single port??
				DataInputStream in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
				*/
					// DONT use print writer to send messages
				PrintWriter out =
					new PrintWriter(clientSocket.getOutputStream(), true);                   
				BufferedReader in = new BufferedReader(
					new InputStreamReader(clientSocket.getInputStream()));
			) {
				System.out.println("Server socket opened successfully on port " + port_number);           
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					out.println(inputLine);
				}
			} catch (IOException e) {
				System.out.println("Exception caught when trying to listen on port "
					+ port_number + " or listening for a connection");
				System.out.println(e.getMessage());
			}
		}
		
	}
    public static void main(String[] args) throws IOException {
        
        if (args.length != 1) {
            System.err.println("Usage: java EchoServer <port number>");
            System.exit(1);
        }
        
        int port_number = Integer.parseInt(args[0]);

		ServerSocket serv_sock = null;
		Socket client_sock = null;
		/**
		 * open single server socket. The main thread listens on the given port for incoming 
		 * client connections and creates a new thread with a connection to the server for each 
		 * one that comes in
		 */
		try {
			serv_sock = new ServerSocket(port_number);
		} catch (IOException e) {
			System.err.println("Error when attempting to listen on port" + port_number);
			e.printStackTrace();
		}

		while (true) {
			client_sock = serv_sock.accept();
			System.out.println("Server socket opened successfully on port " + port_number);
			Thread t = new Thread(new Server_conn(port_number, client_sock));
			t.start();
		}
    }
}