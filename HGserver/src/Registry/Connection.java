package Registry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.Date;

import RMI.Client;

public class Connection {

	private Socket connection;
	private int cookie;
	private BufferedReader in;
	private PrintWriter out;
	private Client updater;
	
	private Object outLock;
	private Object updateLock;
	
	private long lastCheck;
	private long sessionTime;
	
	private String guesserGame;
	private String masterGame;
	
	/**
	 * A wrapper for all the communication with a client.
	 * @param connectionID
	 * @param updater
	 * @throws RegistrationExcpetion
	 */
	public Connection(int connectionID, Object updater) throws RegistrationExcpetion {
		if(updater instanceof Client)
			this.setUpdater((Client)updater);
		else
			throw new RegistrationExcpetion(RegistrationExcpetion.updaterNotValid);
		this.setCookie(connectionID);
		outLock = new Object();
		updateLock = new Object();
		lastCheck = System.currentTimeMillis();
		sessionTime = 5*60*1000;
		guesserGame = null;
		masterGame = null;
	}
	
	/**
	 * send a message through this connection and reset the expireTime
	 * @param message
	 */
	public void contact(String s) {
		synchronized(outLock) {
			if(out!=null) {
				out.println(s);
				lastCheck = System.currentTimeMillis();
			}
		}
	}
	
	public void halfClose() {
		synchronized(outLock) {
			try {
				if (connection!=null)
				connection.shutdownOutput();
			} catch (IOException e) { System.err.println(e.getMessage()); }
		}
	}
	
	/**
	 * Wrapper of the socket connection
	 * @return rough data coming from the tcp socket
	 * @throws IOException
	 */
	public String listen() throws IOException {
		return in.readLine();
	}
	
	/**
	 * send an update through this connection
	 * @param update in String format
	 */
	public void update(String update) {
		synchronized(updateLock) {
			try { this.updater.update(update); } 
			catch (RemoteException e) {	System.err.println(e.getMessage()); }
		}
	}
	
	/**
	 * check if the expireTime is passed
	 * @return true if expireTime is already passed, false o.w.
	 */
	public boolean expired() {
		return ( System.currentTimeMillis() - lastCheck > sessionTime);
	}
	
	/**
	 * close the Connection, if any reader was blocked fromConnection
	 * @throws IOException
	 */
	public void close() throws IOException {
		if(connection!=null) {
			connection.close();
		}
		this.clean();
	}
	
	/**
	 * clean up references
	 * @throws IOException
	 */
	private void clean() throws IOException {
		outLock = null;
		updateLock = null;
		updater = null;
		out = null;
		in = null;
		connection = null;
	}
	
	// Setters and Cleaner
	
	public void setCookie(int cookie) {
		this.cookie = cookie;
	}
	
	public int getCookie() {
		return cookie;
	}
	
	public Connection setIN(BufferedReader in) {
		this.in = in;
		return this;
	}
	
	public BufferedReader getIn() {
		return this.in;
	}
	
	public Connection setOut(PrintWriter out) {
		this.out = out;
		return this;
	}
	
	public PrintWriter getOut() {
		return this.out;
	}
	
	public void setUpdater(Client updater) {
		this.updater = updater;
	}
	
	public Client getUpdater() {
		return this.updater;
	}

	public Connection setSocket(Socket connection) {
		this.connection = connection;
		return this;
	}

	public String getGuesserGame() {
		return guesserGame;
	}

	public void setGuesserGame(String guesserGame) {
		this.guesserGame = guesserGame;
	}

	public String getMasterGame() {
		return masterGame;
	}

	public void setMasterGame(String masterGame) {
		this.masterGame = masterGame;
	}
}
