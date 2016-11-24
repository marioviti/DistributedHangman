package Registry;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RequestServer implements Runnable {

	private ServerSocket requestServerConnection;
	private Socket requestConnection;
	private Executor requestListenerPool;
	
	private boolean running;
	
	/**
	 * Create a Listener on the TCP server connection for incoming Requests from client applications.
	 * Extract the message from the socket and passes the content to the RequestInterpreter.
	 * @throws IOException 
	 */
	public RequestServer(int hostPort) throws IOException {
		requestServerConnection = new ServerSocket(hostPort);
		log(requestServerConnection.toString());
		requestListenerPool = Executors.newCachedThreadPool();
		requestConnection = null;
	}
	
	/**
	 * Awaits for connection, when connected delivers the connection Socket to a RequestListener.
	 */
	@Override
	public void run() {
		running = true;
		log("running...");
		while(running)
			serveSocket();
		log("shutting down...");
	}
	
	/**
	 * Core method of the running loop.
	 */
	private void serveSocket() {
		try {
			log("waiting new connection...");
			requestConnection = requestServerConnection.accept();
		} catch (IOException e1) {	
			e1.printStackTrace();
			shutdown();
		}
		try {
			log("new connection received "+requestConnection.toString());
			requestListenerPool.execute( new RequestListener(requestConnection) );
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void shutdown() {
		running = false;
	}
	
	private void log(String s) {
		System.err.println(this.getClass().getName() +": " + s);
	}

}
