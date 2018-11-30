
import java.io.*;
import java.net.*;
//import lejos.hardware.Button;
//import lejos.hardware.lcd.LCD;

/**
 * Server Class
 * Class to host a server on the robot
 * and send the map to the PC Client
 * 
 */
public class Server {

	// Port Number for Server - needs to be same port on the client
	public static final int port = 1234;
	
	// Server and client
	private static ServerSocket ss;
	private static Socket client;
	
	public static boolean serverCreated = false;
	public static boolean clientConnected = false;
	
	public static void main(String[] args) throws Exception {
		new Server();
		while (true) {
			String data = awaitData();
			System.out.println(data);
			Thread.sleep(2000);
			// Execute Action
			sendData("Achieved");
		}

		
		
	}
	/**
	 * Server Constructor
	 * Attempt to create a server
	 * 
	 */
	public Server() {
		// Try and create the server
		do {
			try {
				// Create a Server Socket and Await for the client to connect
				ss = new ServerSocket(port);
				serverCreated = true;
				System.out.println("Server Created");
				
			} catch(Exception e) {
				System.out.println("Server couldn't be made:" + e);
				System.out.println("Trying again");
			}
		} while (serverCreated == false);
		
		do {
			System.out.println("Try connecting to client");
			connectToClient();
		} while (clientConnected == false);
		
	}
	
	public static void connectToClient() {
		try {
			// Create a Server Socket and Await for the client to connect
			client = ss.accept();
			clientConnected = true;
			System.out.println("Client Connected");
		} catch(Exception e) {
			System.out.println("Can't connect to client" + e);
		}
	}
	
	public static void sendData(String data) throws IOException {
		if (client.isClosed() == true) {
			clientConnected = false;
			do {
				connectToClient();
			} while (clientConnected == false);
		}
		
		// Create a data output stream to write strings to the client
		OutputStream out = client.getOutputStream();
		DataOutputStream dOut = new DataOutputStream(out);
		dOut.writeUTF(data);
		dOut.flush();
	}
	
	public static String awaitData() throws Exception {
		if (client.isClosed() == true) {
			clientConnected = false;
			do {
				connectToClient();
			} while (clientConnected == false);
		}
		
		String data = null;
		do {
			try {
				// Create an data input stream to receive the map probability strings
				InputStream in = client.getInputStream();
				DataInputStream dIn = new DataInputStream(in);
				data = dIn.readUTF();
			} catch(Exception e) {
				
			}
		} while(data == null);
		
		return data;
	}

	
	/**
	 * close()
	 * Close the server socket server
	 * 
	 */
	public void close() {
		try {
			ss.close();
		} catch(Exception e) {
			// Close failed?
		}
	}
}