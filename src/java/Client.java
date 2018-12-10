
import java.io.*;
import java.net.*;

/**
 * Client Class Basic Socket Client to communicate commands to the EV3 Server
 */
public class Client {

	// IP and Port
	private String ip = "192.168.70.187";
//	private String ip = "127.0.0.1";
	private int port = 1234;
	public boolean connected = false;

	// Outer reference to the socket
	private Socket sock = null;

	/**
	 * Client
	 * Constructor which attempts to connect to robot server
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Client() throws IOException, InterruptedException {
		connectToRobot();
	}
	
	/**
	 * connectToRobot()
	 * Attempt to connect to robot server.
	 * Trys every 500ms until it connects.
	 */
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

	/**
	 * isConnected()
	 * Returns whether the client socket is connected to the server
	 * @return boolean connected
	 */
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

	/**
	 * sendData()
	 * Sends a string of data to the server using an output stream
	 * @param dataString
	 * @throws IOException
	 */
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

	/**
	 * awaitData()
	 * Awaits an input stream of data from the server
	 * Won't return until it has received data
	 * @return
	 * @throws IOException
	 */
	public String awaitData() throws IOException {
		String data = null;
		do {
			try {
				// Create input stream to receive data from robot server
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