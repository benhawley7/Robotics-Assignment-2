//
//import java.io.*;
//import java.net.*;
////import lejos.hardware.Button;
////import lejos.hardware.lcd.LCD;
//
///**
// * Server Class
// * Class to host a server on the robot
// * and send the map to the PC Client
// * 
// */
//public class Server {
//
//	// Port Number for Server - needs to be same port on the client
//	public static final int port = 1234;
//	
//	// Server and client
//	private ServerSocket ss;
//	private Socket client;
//	private RobotInterface robotInterface;
//	
//	public boolean serverCreated = false;
//	public boolean clientConnected = false;
//	
//	
//	public static void main(String[] args) throws Exception {
//		
//		Server s = new Server();
//		RobotInterface rI = new RobotInterface(s);
//		s.setInterface(rI);
//		
//		while (true) {
//			Thread.sleep(2000);
//			s.awaitData();
//		}
//		
//	}
//	/**
//	 * Server Constructor
//	 * Attempt to create a server
//	 * 
//	 */
//	public Server() {
//		// Try and create the server
//		do {
//			try {
//				// Create a Server Socket and Await for the client to connect
//				ss = new ServerSocket(port);
//				serverCreated = true;
//				System.out.println("Server Created");
//				
//			} catch(Exception e) {
//				System.out.println("Server couldn't be made:" + e);
//				System.out.println("Trying again");
//			}
//		} while (serverCreated == false);
//		
//		do {
//			System.out.println("Try connecting to client");
//			connectToClient();
//		} while (clientConnected == false);
//		
//	}
//	
//	public void setInterface(RobotInterface rI) {
//		robotInterface = rI;
//	}
//	
//	public void connectToClient() {
//		try {
//			// Create a Server Socket and Await for the client to connect
//			client = ss.accept();
//			clientConnected = true;
//			System.out.println("Client Connected");
//		} catch(Exception e) {
//			System.out.println("Can't connect to client" + e);
//		}
//	}
//	
//	public void sendData(String data) throws IOException {
//		if (client.isClosed() == true) {
//			clientConnected = false;
//			do {
//				connectToClient();
//			} while (clientConnected == false);
//		}
//		System.out.println("Sending Data: " + data);
//		// Create a data output stream to write strings to the client
//		OutputStream out = client.getOutputStream();
//		DataOutputStream dOut = new DataOutputStream(out);
//		dOut.writeUTF(data);
//		dOut.flush();
//	}
//	
//	public void awaitData() {
//		
//		String data = null;
//
//		try {
//			// Create an data input stream to receive the map probability strings
//			InputStream in = client.getInputStream();
//			DataInputStream dIn = new DataInputStream(in);
//			data = dIn.readUTF();
//
//		} catch(Exception e) {
//			try {
//				client = ss.accept();
//			} catch (IOException e1) {
////				// TODO Auto-generated catch block
////				e1.printStackTrace();
//			}
//		}
//		try {
//			robotInterface.parseCommand(data);
//		} catch(Exception e) {
//			
//		}
//	}
//
//	
//	/**
//	 * close()
//	 * Close the server socket server
//	 * 
//	 */
//	public void close() {
//		try {
//			ss.close();
//		} catch(Exception e) {
//			// Close failed?
//		}
//	}
//}