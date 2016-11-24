package Registry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.json.simple.parser.ParseException;

public class RequestListener implements Runnable {
	
	private Socket connection;
	private BufferedReader requestIn;
	private PrintWriter responseOut;
	private Connection player;
	private Message request;
	private Message badRequest;
	private static Executor reactorsPool;
	public volatile boolean running;
	
	public RequestListener(Socket requestConnection) throws IOException {
		this.connection = requestConnection;
		this.requestIn = new BufferedReader(new InputStreamReader(requestConnection.getInputStream()));
		this.responseOut = new PrintWriter(requestConnection.getOutputStream(), true);	
		reactorsPool = Executors.newCachedThreadPool();
		request = new Message();
		badRequest = new Message();
	}

	@Override
	public void run() {
		running = true;
		log("running...");
		
		validateProfile();
		while(running)
			serveConnection();
		
		log("shutting down connection...");
	}
	
	private void validateProfile() {
		String roughdata;
		try {
			if((roughdata = requestIn.readLine())!=null) {
				request = request.validate(roughdata);
				completeProfile();
				reactorsPool.execute(new RequestReactor(request, player));
			}else 
				shutdown();
		} catch ( ParseException e ) {
			sendBadRequest();
			e.printStackTrace();
		} catch( IOException e ) {
			e.printStackTrace();
			shutdown();
		}
	}
	
	private void completeProfile() throws IOException {
		int cookie = Integer.parseInt(request.header(Message.fieldCookie));
		if(Lobby.logged.containsKey(cookie)) {
			this.player = ConnectionList.getConnection(cookie);
		}
		else {
			if(connection!=null) {
				connection.setSoLinger (true, 0) ;
				connection.close();
			}
			throw new IOException();
		}
		if(player!=null) {
			player.setSocket(connection).setOut(responseOut).setIN(requestIn);
		}
		else {
			if(connection!=null) {
				connection.setSoLinger (true, 0) ;
				connection.close();
			}
			throw new IOException();
		}
	}
	
	private void serveConnection() {
		String rougRequest;
		try {
			if(( rougRequest = player.listen())!=null ) {
				request = request.validate(rougRequest);
				reactorsPool.execute(new RequestReactor(request, player));
			}else 
				shutdown();
		} catch ( ParseException e ) {
			sendBadRequest();
			e.printStackTrace();
		} catch( IOException e1 ) {
			if(!e1.getMessage().equals("Socket closed"))
				e1.printStackTrace();
			shutdown();
		}
	}
	
	private void sendBadRequest() {
		badRequest.setType(Message.typeBadReq);
		responseOut.println(badRequest.getMessage().toString());
	}
	
	private void shutdown() {
		running = false;
	}
	
	private void log(String s) {
		System.err.println(this.getClass().getName()+ ": " + s);
	}
}
