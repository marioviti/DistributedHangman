package Client;

import java.io.IOException;
import java.net.SocketException;
import org.json.simple.parser.ParseException;

public class ResponseListener extends Thread {
	
	Connection playerConnection;
	Message response;

	private boolean running;
	
	/**
	 * main Thread for Server message listening
	 * 
	 * @param playerConnection
	 */
	public ResponseListener(Connection playerConnection) {
		this.playerConnection = playerConnection;
		response = new Message();
	}
	
	public void run(){
		log("running...");
		running = true;
		while(running)
			retrivingResponse();
		log("shutting down...");
	} 	

	private void retrivingResponse() {
		String roughData;
		try {
			if((roughData = playerConnection.connectionIn.readLine())!=null) {
				response.validate(roughData);
				TaskManager.add(response);
			}
			else {
				shutdown();
				playerConnection.halfClose();
			}
		} catch ( java.net.SocketException e) {
			shutdown();
			e.printStackTrace();
			try {
				Config.config.put(Config.cookie, null);
				Config.saveConfiguration();;
				TaskManager.induceShutdown("connection refused, maybe due to cookie session expired, try restart");
			} catch (InterruptedException | IOException e1) {
				e1.printStackTrace();
			}
			shutdown();
		} catch ( ParseException e) {
			e.printStackTrace();
		} catch ( IOException | InterruptedException e ) {
			e.printStackTrace();
			shutdown();
		}
	}
	
	public void shutdown() {
		if(running) {
			log("called the shutdouwn");
			running = false;
		}
	}
	
	public void log(String s) {
		System.err.println(this.getClass().getName()+ ": " + s);
	}
	
}
