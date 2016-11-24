package Client;

import java.io.IOException;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GuesserHelper extends Thread {

	private MulticastService udpSender;
	private GuessMessage gmessage;
	private boolean running;
	private String update;

	public GuesserHelper( MulticastService udpSender, GuessMessage gmessage) {
		this.gmessage = gmessage;
		this.udpSender = udpSender;
	}
	
	public void run() {
		running = true;
		String received;
		while(running) {
			try {
				if((received=udpSender.receive())!=null) {
					update = gmessage.checkUpdate(received);
					if(gmessage.update!=null && gmessage.counter!=null)
						iLog(gmessage.update + " " + gmessage.counter);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			} catch ( EncryptionOperationNotPossibleException e ) {
				e.printStackTrace();
			}
		}
	}

	public void log(String s) {
		System.err.println(this.getClass().getName()+ ": " + s);
	}
	
	public void iLog(String s) {
		System.out.println(this.getClass().getName()+ ": " + s);
	}
}
