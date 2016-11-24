package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection {

	private int connectionID;
	private Socket connection;
	public BufferedReader connectionIn;
	public PrintWriter connectionOut;
	
	/**
	 * Wrapper for socket IO operations
	 * @param connectionID
	 * @param connection
	 * @throws IOException
	 */
	public Connection(int connectionID, Socket connection) throws IOException {
		this.connectionID = connectionID;
		this.connection = connection;
		this.connectionIn = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		this.connectionOut = new PrintWriter(connection.getOutputStream(), true);	
	}
	
	public void close() throws IOException {
		connection.close();
	}
	
	public void halfClose() throws IOException {
		connection.shutdownOutput();
	}
	
	public void contact(String data) {
		connectionOut.println(data);
	}

	public int getConnectionID() {
		return connectionID;
	}

	public void setConnectionID(int connectionID) {
		this.connectionID = connectionID;
	}
}
