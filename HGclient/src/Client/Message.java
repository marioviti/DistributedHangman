package Client;

import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Message {
	
	private static JSONParser parser = new JSONParser();
	private static Object parserLock = new Object();
	private static int argumentNum = 4;
	
	/**
	 * HEADER
	 * 
	 * {
	 * 	cookie: "",
	 * 	type: "",
	 *  arguments: [...],
	 *  success: ""
	 * }
	 *
	 * SIGNATURE
	 * 
	 * create_game secretWor(arg[0]) numOfGuessers(arg[1])
	 * join_game gameCreator(arg[0])
	 * leave_game gameCreator(arg[0])
	 * delete_game gameCreator(arg0)
	 * 
	 */
	
	public static final String fieldCookie = "cookie";
	public static final String fieldType = "type";
	public static final String fieldArguments = "arguments";
	public static final String fieldSuccess = "success";
	
	// Client Messages

	public static final String typeCreateGame = "create_game";
	public static final String typeJoinGame = "join_game";
	public static final String typeLeaveGame = "leave_game";
	public static final String typeDeleteGame = "delete_game";
	
	// Server Messages
	
	public static final String typeGameCreated = "gameCreated";
	public static final String typeGameJoined = "gameJoined";
	public static final String typeGameLeft = "gameLeft";
	public static final String typeGuesserGameReady = "guesserGameReady";
	public static final String typeMasterGameReady = "masterGameready";
	public static final String typeGameHasBeenDeleted = "gameHasBeenDeleted";
	
	public static final String typeBadReq = "badRequest";
	public static final String typeUnfullfillReq = "unfullfillRequest";

	public static final String failSessionExpired = "session expired";
	
	public JSONObject header = null;
	public JSONArray arguments = null;
	private Object keysetLock;
	private boolean success;
		
	public Message() {
		init();
	}
	
	public Message(String roughData) throws ParseException {
		init();
		validate(roughData);
	}
	
	private void init() {
		keysetLock = new Object();
		header = new JSONObject();
		arguments = new JSONArray();
		for(int i = 0; i<argumentNum; i++)
			arguments.add(null);
	}
	
	public Message validate (String roughData) throws ParseException {
		header = null;
		synchronized(parserLock) {
			header = (JSONObject) parser.parse(roughData);
		}
		synchronized(keysetLock) {
			for(String field : (Set<String>)header.keySet()){
				if(!isValidHeaderField(field))
					throw new ParseException(0);
			}
		}
		if(header.containsKey(fieldSuccess))
			success = (boolean)header.get(fieldSuccess);
		if(header.get(fieldArguments) instanceof JSONArray) 
			arguments = (JSONArray)header.get(fieldArguments);
		else 
			throw new ParseException(0);
		return this;
	}
	
	private boolean isValidHeaderField(String field) {
		switch(field) {
		case fieldCookie : return true;
		case fieldType : return true;
		case fieldArguments : return true; 
		case fieldSuccess : return true; 
		default : return false;
		}
	} 
	
	public Message setType(String type) {
		setHeader(fieldType,type);
		return this;
	}
	
	public String header(String field) {
		return (isValidHeaderField(field)) ? (String)header.get(field) : null;
	}
	
	public Message setHeader(String field, String value) {
		if(isValidHeaderField(field))
			header.put(field, value);
		return this;
	}
	
	public String argument(int index) {
		return arguments.get(index).toString();
	}
	
	public Message setArgument(int index, Object arg) {
		arguments.set(index, arg);
		return this;
	}
	
	public JSONObject getMessage() {
		if (header.get(fieldArguments)==null)
			header.put(fieldArguments, arguments);
		return header;
	}
	
	public Message successfull() {
		header.put(fieldSuccess, true);
		return this;
	}
	
	public Message failed() {
		header.put(fieldSuccess, false);
		return this;
	}
	
	public boolean getSuccess() {
		return success;
	}

	public Message clean() {
		setHeader(fieldType,null);
		setHeader(fieldSuccess,null);
		for(int i = 0; i<arguments.size();i++)
			setArgument(i,null);
		return this;
	}
}
