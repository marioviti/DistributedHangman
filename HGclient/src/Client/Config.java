package Client;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Config {
	
	public static final String configPath = "./client_configuration.txt";
	
	public static void defaultConfig() throws IOException {
		initialize();
		Config.config.put(Config.serverAddress, "192.168.5.1");
		Config.config.put(Config.serverPort, "5000");
		Config.config.put(Config.formPort, "1111");
		saveConfiguration();
	}
	
	public static final String myAddress = "myAddress";
	public static final String serverAddress = "serverAddress";
	public static final String serverPort = "serverPort";
	public static final String formPort = "formPort";
	public static final String user = "user";
	public static final String cookie = "cookie";
	public static final String gamingMulticastAddress = "gamingMulticastAddress";
	public static final String gamingPassWord= "gamingPassWord";
	public static final String masterGameSecretWord = "secretWord";
	public static final String guesserGameMaster = "master";
	public static final String guesserNumOfLetters = "numOfLetters";
	public static final String gameMode = "gameMode";
	public static final String guesserMode = "guesserMode";
	public static final String masterMode = "masterMode";

	private static JSONParser parser = new JSONParser();
    public static JSONObject config = new JSONObject();
    public static JSONObject gusserGame = new JSONObject();
	public static JSONObject masterGame = new JSONObject();
	
	public static void initialize() {
		config.put("masterGame", masterGame);
		config.put("guesserGame", gusserGame);
	}
	
	public static void saveConfiguration() throws IOException {
		FileWriter file = new FileWriter(configPath);
		file.write(config.toJSONString());
		System.err.println("never remove this file: "+configPath);
		System.err.println("Successfully Copied JSON Object to File...");
		System.err.println("JSON Object: " + config +"\n");
		file.flush();
		file.close();
	}
	
	public static void loadConfiguration() throws FileNotFoundException, IOException, ParseException {
		System.err.println("never remove this file: "+configPath);
		 Object obj = parser.parse(new FileReader(configPath));
		 config = (JSONObject) obj;
		 obj = config.get("masterGame");
		 if(obj instanceof JSONObject)
			 masterGame = (JSONObject)obj;
		 else
			 throw new ParseException(0);
		 obj = config.get("guesserGame");
		 if(obj instanceof JSONObject)
			 gusserGame = (JSONObject)obj;
		 else 
			 throw new ParseException(0);
		System.err.println("Successfully Loaded File to JSON Object...");
	}

	public static void resetGame() {
		gusserGame = new JSONObject();
		masterGame = new JSONObject();
		config.put("masterGame",masterGame);
		config.put("guesserGame",gusserGame);
		Config.config.put(Config.gamingMulticastAddress, null);
		Config.config.put(Config.gamingPassWord, null);
		Config.config.put(Config.gameMode, null);
	}
}
