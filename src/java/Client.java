
import java.io.*;
import java.net.*;

/**
 * Client Class Basic Socket Client to run on the PC to receive arena map String
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

	public void connectToRobot() {
		try {
			sock = new Socket(ip, port);
			System.out.println("Connected to Robot");
			connected = true;
		} catch (Exception e) {
			System.out.println("Could not connect. Trying again in 500ms");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			connectToRobot();
		}
	}

	public boolean isConnected() {
	
		if (sock == null) {
			return false;
		}
		if (sock.isClosed() == true) {
			connected = false;
		} else {
			connected = true;
		}
		return connected;
	}

	public void sendData(String dataString) throws IOException {
		// Create a data output stream to write strings to the client
		try {
			OutputStream out = sock.getOutputStream();
			DataOutputStream dOut = new DataOutputStream(out);
			dOut.writeUTF(dataString);
			dOut.flush();
		} catch (Exception e) {
			connectToRobot();
			sendData(dataString);
		}

	}

	public String awaitData() throws IOException {
		String data = null;
		do {
			try {
				// Create an data input stream to receive the map probability strings
				InputStream in = sock.getInputStream();
				DataInputStream dIn = new DataInputStream(in);
				data = dIn.readUTF();
			} catch (Exception e) {
				connectToRobot();
			}
		} while (data == null);

		return data;
	}
}