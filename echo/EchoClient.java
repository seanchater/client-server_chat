import java.io.*;
import java.net.*;

/**
 * How to run:
 * java EchoClient seans-macbook-air-2.local 5000
 * Then can run separate clients in diff terminal windows
 */
public class EchoClient {

	private static class Client_conn implements Runnable {
		private String hostName;
		private int portNumber;

		public Client_conn(String host, int port) {
			hostName = host;
			portNumber = port;
		}

		public void run() {
			try (
            Socket echoSocket = new Socket(hostName, portNumber);
			/*
			DataInputStream in = new DataInputStream(new BufferedInputStream(echoSocket.getInputStream()));
			DataOutputStream out = new DataOutputStream(echoSocket.getOutputStream());
			*/
        	// use message objects to send/receive information NOT printwriter
            PrintWriter out =
                new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in =
                new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));
            BufferedReader stdIn =
                new BufferedReader(
                    new InputStreamReader(System.in))
        ) {
        	System.out.println("Client socket opened successfully on port " + portNumber);
        	String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                System.out.format("Client %s echo: %s\n", Thread.currentThread().getName(), in.readLine());
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        } 
		}
			
	}
    public static void main(String[] args) throws IOException {
        
        if (args.length != 2) {
            System.err.println(
                "Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

		// only need multiple threads on client side for GUI etc...
		Thread t1 = new Thread(new Client_conn(hostName, portNumber));
		t1.start();
        
    }

}