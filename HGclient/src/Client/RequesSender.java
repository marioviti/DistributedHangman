package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class RequesSender extends Thread{
	
	private Message request;
	private Connection playerConnection;
	private BufferedReader intfaceIn;
	private TaskManager requestManager;
	private ResponseListener listener;
	private boolean connected = false;
	
	private volatile boolean running;
	
	/**
	 * The main Thread for Server Interaction
	 * 
	 * @param intfaceIn
	 * @param address
	 * @param port
	 * @param connectionID
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public RequesSender(BufferedReader intfaceIn ,String address, int port, int connectionID) throws UnknownHostException, IOException {
		this.intfaceIn = intfaceIn;
		log("connectiong");
		Socket socket = new Socket(address,port);
		this.playerConnection = new Connection(connectionID, socket);
		listener = new ResponseListener(playerConnection);
		requestManager = new TaskManager(this,listener,playerConnection); 
		request = new Message();
		request.setHeader(Message.fieldCookie, connectionID+"");
	}

	public void run() {
		log("strating...");
		try { 
			execution();
		} catch (InterruptedException e) { 
			e.printStackTrace(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
		log("shutting down...");
	}
	
	private void execution() throws InterruptedException, IOException {
		running = true;
		initialize();
		String roughData;
		while(running) { 
			if((roughData = intfaceIn.readLine())!=null && !roughData.equals("")) {
				processRequest(roughData);
			}
		}
	}

	private void processRequest(String data) throws IOException, InterruptedException {
		String usage;
		if((usage = RequestPreprocess.parseCommandLine(data,request))==null) {
				TaskManager.add(request);
		}
		else
			iLog("WARNING: " + usage);
	}
	
	private void initialize() throws InterruptedException {
		listener.start();
		requestManager.start();
		sleep(100); 
	}
	
	public void shutdown() throws IOException {
		running = false;	
		//intfaceIn.close(); // si potrebbe fare ma Java poi impedisce di aprirlo di nuovo
	}

	public void log(String s) {
		System.err.println(this.getClass().getName()+ ": " + s);
	}
	
	public void iLog(String s) {
		System.out.println(this.getClass().getName()+ ": " + s);
	}

}
