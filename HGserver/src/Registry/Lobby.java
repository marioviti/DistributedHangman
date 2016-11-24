package Registry;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Lobby {
	
	public static final String lobbyPath = "./server_lobby.txt";
	
	/*
	 * Lobby
	 * 
	 * {
	 * 
	 * games:
	 * 	{ master : { numLetters: k, numGuessers: n, numJoined: m, guesser: { usr0 : cookie , usr1 : cookie, ... } } ,
	 * 	  user0 : { ... }, ... },
	 * 
	 * logged : { cookie : user , cookie0 : user0 , ... },
	 * 
	 * registered : { user : password, user0 : password ... }
	 * 
	 * }
	 * 
	 */
	
	private static int cookieCounter = 0;
	private static int gameCounter = 0;
	private static int maxGameLimit = 10;

	
	public static final String _games = "games";
	public static final String _logged = "logged";
	public static final String _registered = "registered";
	
	public static final String games_numLetters = "numLetters";
	public static final String games_numJoined = "numJoined";
	public static final String games_numGuessers = "numGuessers";
	public static final String games_guesser= "guesser";

	private static JSONParser parser = new JSONParser();
    
	public static JSONObject lobby = new JSONObject();
	public static JSONObject games = new JSONObject();
	public static JSONObject logged = new JSONObject();
	public static JSONObject registered = new JSONObject();
	
	public static void register(String user, String password) throws RegistrationExcpetion {
		synchronized(registered) {
		if (registered.containsKey(user))
			throw new RegistrationExcpetion(RegistrationExcpetion.usernameNotAvailable);
		registered.put(user, password);
		}
	}
	
	public static int login(Object user, Object password) throws RegistrationExcpetion {
		synchronized(logged) {
			if( !registered.containsKey(user) || !registered.get(user).equals(password))
				throw new RegistrationExcpetion(RegistrationExcpetion.wrongCredentials);
			else {
				if(logged.containsValue(user))
					throw new RegistrationExcpetion(RegistrationExcpetion.alreadyLogged);
				int cookie = ++cookieCounter;
				logged.put(cookie, user);
				return cookie;
			} 
		}
	}
	
	public static void logOut(int cookie) {
		synchronized(logged) {
			logged.remove(cookie);
		}
	}
	
	public static void addGame(Object cookie, Object numLetters, Object numGuessers) throws GameStateException {
		synchronized(games) {
			if(Lobby.gameCounter == Lobby.maxGameLimit)
				throw new GameStateException(GameStateException.maxGameLimitReached);
			Lobby.gameCounter++;
			JSONObject game = new JSONObject();
			JSONObject guessers = new JSONObject();
			String master = (String) logged.get(cookie);
	
			game.put(games_numLetters, numLetters);
			game.put(games_numGuessers, numGuessers);
			game.put(games_guesser, guessers);
			game.put(games_numJoined, 0);
			
			games.put(master,game);
		}
	}
	
	public static Object deleteGame(String master) throws GameStateException {
		synchronized(games) {
			JSONObject game = ((JSONObject)games.get(master));
			if(game == null)
				throw new GameStateException(GameStateException.gameIsNotExtistent);
			JSONObject original = (JSONObject) (game).get(games_guesser);
			JSONObject clone = null;
			try {
				clone = (JSONObject) parser.parse(original.toString());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			games.remove(master);
			Lobby.gameCounter--;
			return clone;
		}
	}
	
	public static boolean joinGame(String master, Object cookie) throws GameStateException {
		Object user = logged.get(cookie);
		Object game = games.get(master);
		Object guessers;
		int numGuesser, joined;
		synchronized(games) {
			if(game == null)
				throw new GameStateException(GameStateException.gameIsNotExtistent);
			if(!(game instanceof JSONObject) || cookie==null) 
				throw new GameStateException(GameStateException.lobbyCorrupted);
			else {
				numGuesser = Integer.parseInt((String) ((JSONObject)game).get(games_numGuessers));
				joined = (int)((JSONObject)game).get(games_numJoined);
				if(numGuesser==joined) {
					throw new GameStateException(GameStateException.gameIsFull);
				}
				guessers = ((JSONObject)game).get(games_guesser);
				((JSONObject)guessers).put(user, cookie);
				((JSONObject)game).put(games_numJoined,++joined);
				if(joined == numGuesser) {
					return true;
				}		
			}
		}
		return false;
	}
	
	public static void leaveGame(String master, Object cookie) throws GameStateException {
		Object user = logged.get(cookie);
		Object game = games.get(master);
		JSONObject guessers;
		int joined;
		synchronized(games) {
			if(game instanceof JSONObject) {
				joined = (int)((JSONObject)game).get(games_numJoined);
				((JSONObject)game).put(games_numJoined,--joined);
				guessers = (JSONObject) ((JSONObject)game).get(games_guesser);
				guessers.remove(user);
			}
			else
				throw new GameStateException(GameStateException.gameIsNotExtistent);
		}
	}
	
	public static String getUser(Object cookie) {
		return (String) logged.get(cookie);
	}
	
	public static String getCookie(String user) {
		for(Object cookie: logged.keySet()) {
			if(logged.get(cookie).equals(user))
				return cookie+"";
		}
		return null;
	}
	
	public static void initialize() {
		lobby.put(_games, games);
		lobby.put(_logged, logged);
		lobby.put(_registered, registered);
	}

	public static void saveLobby() throws IOException {
		FileWriter file = new FileWriter(lobbyPath);
		file.write(lobby.toJSONString());
		System.err.println("Successfully Copied JSON Object to File...");
		System.err.println("JSON Object: " + lobby);
		file.flush();
		file.close();
	}
	
	public static void loadLobby() throws FileNotFoundException, IOException, ParseException {
		 Object obj = parser.parse(new FileReader(lobbyPath));
		 lobby = (JSONObject) obj;
		 obj = lobby.get(_games);
		 if(obj instanceof JSONObject)
			 games = (JSONObject)obj;
		 obj = lobby.get(_logged);
		 if(obj instanceof JSONObject)
			 logged = (JSONObject)obj;
		 obj = lobby.get(_registered);
		 if(obj instanceof JSONObject)
			 registered = (JSONObject)obj;	 
	}

	public static String getGamesList() {
		return Lobby.games.toString();
	}

	public static Object getGameNumOfLetters(String master) {
		return ((JSONObject)games.get(master)).get(games_numLetters);
	}
}
