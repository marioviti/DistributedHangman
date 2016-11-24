package Registry;

import java.io.IOException;
import org.json.simple.JSONObject;

public class RequestReactor implements Runnable {
	
	private static String unknownAction = "unknown action";
	private Message request;
	private Connection player;

	public RequestReactor(Message request, Connection player) {
		this.request = request;
		this.player = player;
	}
	
	@Override
	public void run() {
		String task = "";
		if(request!=null) {
			log("new task "+request.getMessage().toString());
			task = request.header(Message.fieldType);
		}
		try {
		if (player.expired()==false)
			switch(task) {
				case Message.typeCreateGame : {
					createGameAndNotify(request.argument(0),request.argument(1));
					ConnectionList.sendBroadcastUpdate(Lobby.getGamesList());
					break;
				}
				case Message.typeDeleteGame : {
					deleteGameAndNotify();
					ConnectionList.sendBroadcastUpdate(Lobby.getGamesList());
					break;
				}
				case Message.typeJoinGame : {
					joinGameAndNotify(request.argument(0));
					ConnectionList.sendBroadcastUpdate(Lobby.getGamesList());
					break;
				}
				case Message.typeLeaveGame : {
					leaveGameAndNotify(request.argument(0));
					ConnectionList.sendBroadcastUpdate(Lobby.getGamesList());
					break;
				}
				default: {
					failedRequest(RequestReactor.unknownAction); 
					break;
				}
			}
			else {
				sessionExpired();
			}
		}catch(GameStateException e) {
			failedRequest(e.getMessage()); 
		}catch(Exception e) {
			e.printStackTrace();
		}
		log("end task");
		try {
			Lobby.saveLobby();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.request = null;
		this.player = null;
	}

	private void createGameAndNotify(Object secretWord, Object numGuessers) throws GameStateException {
		int numLetters = ((String)secretWord).length();
		Lobby.addGame((Object)player.getCookie(), numLetters+"", numGuessers);
		Message response = newCleanMessage(request);
		response.setType(Message.typeGameCreated);
		response.successfull();
		player.contact(response.getMessage().toString());
		log(response.getMessage().toString());
		player.setMasterGame(player.getCookie()+"");
		synchronized(player) {
			player.setMasterGame(player.getCookie()+"");
		}
	}

	private void deleteGameAndNotify() throws GameStateException {
		JSONObject contactList = (JSONObject) Lobby.deleteGame(Lobby.getUser((Object)player.getCookie()));
		Message response = newCleanMessage(request);
		response.setType(Message.typeGameHasBeenDeleted);
		response.successfull();
		String message = response.getMessage().toString();
		player.contact(message);
		Connection conn;
		for(Object cookie : contactList.values()) {
			conn = ConnectionList.getConnection(((Long)cookie).intValue());
			if(conn!=null)
				conn.contact(message);
		}
		log(response.getMessage().toString());
		synchronized(player) {
			player.setMasterGame(null);
		}
	}
	
	private void joinGameAndNotify(String master) throws GameStateException {
		Message response = newCleanMessage(request);
		if(Lobby.joinGame(master, (Object)player.getCookie())) {
			Object numOfLetters = Lobby.getGameNumOfLetters(master);
			response.successfull();
			response.setType(Message.typeGuesserGameReady).setArgument(0, McastAddrGenerator.getMulticastAddress())
			.setArgument(1, PasswordGenerator.getPassword()).setArgument(2, master).setArgument(3, numOfLetters);
			String message = response.getMessage().toString();
			JSONObject contactList = (JSONObject)Lobby.deleteGame(master);
			Connection gamer;
			for(Object cookie : contactList.values()) {
				gamer = ConnectionList.getConnection(((Long)cookie).intValue());
				gamer.contact(message);
				gamer.halfClose();
			}
			int masterCookie = Integer.parseInt(Lobby.getCookie(master));
			response.setType(Message.typeMasterGameReady);
			Connection masterconn = ConnectionList.getConnection(masterCookie);
			masterconn.contact(response.getMessage().toString());
			masterconn.halfClose();
		}
		else {
			response.setType(Message.typeGameJoined);
			response.successfull();
			player.contact(response.getMessage().toString());
			log(response.getMessage().toString());
		}
		synchronized(player) {
			player.setGuesserGame(master);
		}
	}
	
	private void leaveGameAndNotify(String master) throws GameStateException {
		Lobby.leaveGame(master,(Object)player.getCookie());
		Message response = newCleanMessage(request);
		response.setType(Message.typeGameLeft);
		response.successfull();
		player.contact(response.getMessage().toString());
		log(response.getMessage().toString());
		synchronized(player) {
			player.setGuesserGame(null);
		}
	}
	
	private void sessionExpired() throws IOException {	
		String toDelete;
		try {
			if((toDelete = player.getGuesserGame())!=null) {	
				leaveGameAndNotify(toDelete);
			}
			if((toDelete = player.getMasterGame())!=null) {
				deleteGameAndNotify();
			}
		} catch (GameStateException e) {
			log("leftovers");
		}
		Lobby.logOut(player.getCookie());
		failedRequest(Message.failSessionExpired);
		player.close();	
	}
	
	private Message newCleanMessage(Message m) {
		return (m == null)? new Message() : m.clean();
	}

	private void failedRequest(String cause) {
		Message response = newCleanMessage(request);
		response.setType(Message.typeUnfullfillReq).setArgument(0, cause);
		response.failed();
		player.contact(response.getMessage().toString());
	}
	
	private void log(String s) {
		System.err.println(this.getClass().getName()+": "+s);
	}
}
