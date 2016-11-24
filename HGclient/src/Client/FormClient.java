package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.rmi.RemoteException;
import RMI.Server;

public class FormClient {
	
	private final int start = -1, 
			config = 0, 
			registerLogin = 1, 
			askName = 2, 
			askPassword = 3,
			registerUpdate = 4, 
			end = 5;
	private int connectID;

	private Server server;
	private Object updater;
	
	private String userName, passWord, action;
	private final String login = "login",register = "register", registerLogiMessage = "type \"login\" to login or \"register\" to register a new user";
		
	private PrintStream out;
	private BufferedReader in;
	
	/**
	 * Protocol FSM for the authentication process
	 * @param server
	 * @throws RemoteException
	 */
	public FormClient(Server server) throws RemoteException {
		this.server = server;
		this.in = new BufferedReader(new InputStreamReader(System.in));
		this.updater = new RemoteClient();
		this.out = System.out;
	}
	
	public void registerLoginPhase() {
		String currentInput = null;
		int currentState = start;
		try {
			do {
				try {
				currentState = transition(currentState,currentInput);
				} catch (RemoteException e) {
					out.println(e.getMessage());
					currentState = registerLogin;
					out.println(registerLogiMessage);
				}
			}
			while(currentState == config || currentState == registerUpdate || 
					(currentState!=end && (currentInput = in.readLine())!=null) );
		} catch (IOException e) { 
			e.printStackTrace(); 
		}
		out.println("logged in");
	}

	private int transition(int currentState, String currentInput) throws RemoteException {
		switch(currentState) {
		case start : return config;
			case config : {
				String cookie = (String)Config.config.get(Config.cookie);
				if (cookie!=null) {
					connectID = Integer.parseInt(cookie);
					return registerUpdate;
				}
				out.println(registerLogiMessage);
				return registerLogin;
			}
			case registerLogin : {
				action = null;
				if(currentInput.equals(register)|| currentInput.equals(login)) {
					action = currentInput;
					out.println("name:");
					return askName;
				}
				out.println(registerLogiMessage);
				return registerLogin;
			}
			case askName : {
				this.userName = currentInput;
				out.println("password:");
				return askPassword;
			}
			case askPassword : {
				passWord = currentInput;
				if(action.equals(register)) {
					server.register(userName, passWord);
					out.println(registerLogiMessage);
					return registerLogin;
				}
				if(action.equals(login)) {
					connectID = server.login(userName, passWord);
					return registerUpdate;
				}
			}
			case registerUpdate : {
				server.registerClient(connectID, new RemoteClient());
				return end;
			}
		}
		return 0;
	}

	public int getConnectID() {
		return connectID;
	}
	
	public String getUserName() {
		return userName;
	}
}
