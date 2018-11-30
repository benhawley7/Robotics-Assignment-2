
import java.io.*;
import java.net.*;

/**
 * Client Class
 * Basic Socket Client to run on the PC to receive arena map String
 * 
 * 
 */
public class Client {
	
//	private String ip = "192.168.70.187"; 
	private String ip = "127.0.0.1"; 
	private int port = 1234;
	public boolean connected = false;
	private Socket sock = null;
	
	public Client() throws IOException, InterruptedException {
		connectToRobot();
	}
	
	public void connectToRobot() throws InterruptedException {
		try {
			sock = new Socket(ip, port);
			System.out.println("Connected to Robot");
			connected = true;
		} catch(Exception e) {
			System.out.println("Could not connect. Trying again in 500ms");
			Thread.sleep(500);
			connectToRobot();
		}
	}
	
	public boolean isConnected() {
		if (sock == null) {
			return false;
		}
		 if (sock.isClosed() == true) {
			 connected = true;
		 } else {
			 connected = false;
		 }
		 return connected;
	}
	
	public void sendData(String dataString) throws IOException {
		// Create a data output stream to write strings to the client
		OutputStream out = sock.getOutputStream();
		DataOutputStream dOut = new DataOutputStream(out);
		dOut.writeUTF(dataString);
		dOut.flush();
	}
	
	public String awaitData() throws IOException {
		String data = null;
		do {
			try {
				// Create an data input stream to receive the map probability strings
				InputStream in = sock.getInputStream();
				DataInputStream dIn = new DataInputStream(in);
				data = dIn.readUTF();
			} catch(Exception e) {
				
			}
		} while(data == null);
		
		return data;
	}
}