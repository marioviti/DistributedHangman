package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;

import org.json.simple.parser.ParseException;

import RMI.Server;

public class ClientApp {
	
	public static void main(String[] args) throws UnknownHostException, IOException, NotBoundException, InterruptedException, ParseException {
		
		if(args.length>0)
			if(args[0].equals("reset_cnf"))
				Config.defaultConfig();
		Config.loadConfiguration();

		/**
		 * Load configurations from configuration file
		 */
		if(Config.config.get(Config.gameMode)==null) {	
			String serverAddress = (String) Config.config.get(Config.serverAddress);
			String serverPort = (String) Config.config.get(Config.serverPort);
			String formPort = (String) Config.config.get(Config.formPort);
			String user = (String) Config.config.get(Config.user);
			String cookie = (String) Config.config.get(Config.cookie);
				
			// taking my address
			Socket socket = new Socket(serverAddress,Integer.parseInt(formPort));
			String myAddress = socket.getLocalAddress().getHostAddress();
			Config.config.put(Config.myAddress, myAddress);
			
			String serverPath = "java.rmi.server.hostname";
			System.setProperty(serverPath, myAddress);
			
			Server formServer = (Server) Naming.lookup("rmi://"+serverAddress+":"+formPort+"/"+Server.registryName);
			FormClient formClient = new FormClient(formServer);
					
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			RequesSender client;

			/**
			 * in case configuration was correct will try to enstablish a connection with the server
			 */
			System.out.println("Wellcome");
			System.out.println("Hang Man client running...");
	
			int cookieInt;
			boolean connected = false;
			while(!connected) {
			try {
				formClient.registerLoginPhase();
				Config.config.put(Config.user, formClient.getUserName()+"");
				Config.config.put(Config.cookie, formClient.getConnectID()+"");
				cookieInt = formClient.getConnectID();
				Config.saveConfiguration();
				client = new RequesSender(in, serverAddress, Integer.parseInt(serverPort), cookieInt);
				client.start();
				client.join();
				connected = true;
			} catch (Exception e) {
				Config.config.put(Config.cookie, null);
				System.err.println(e.getMessage());
				System.err.println("session maybe expired");
			}
			}
			Config.saveConfiguration();
		}
		
		/**
		 * after being detached from the server will try to establish the multicast connection
		 */
		Config.loadConfiguration();
		if(Config.config.get(Config.gameMode)!=null) {	
		switch((String)Config.config.get(Config.gameMode)) {
		
			case Config.guesserMode: {
				GuesserGame game = new GuesserGame(	(String)Config.config.get(Config.myAddress),
													(String)Config.config.get(Config.gamingPassWord),
													(String)Config.config.get(Config.gamingMulticastAddress),
													Integer.parseInt((String)Config.gusserGame.get(Config.guesserNumOfLetters)+""),	
													(String)Config.config.get(Config.user));
				game.start();
				game.join();
				break;
			}
			case Config.masterMode: {
				MasterGame game = new MasterGame((String)Config.config.get(Config.myAddress),
												(String)Config.config.get(Config.gamingPassWord),
												(String)Config.config.get(Config.gamingMulticastAddress),
												(String)Config.masterGame.get(Config.masterGameSecretWord));
				game.start();
				game.join();
				break;
			}
		}}
		Config.resetGame();
		Config.saveConfiguration();
		return;
	}
}
