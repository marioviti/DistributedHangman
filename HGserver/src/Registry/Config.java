package Registry;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Config {
	
	public static final String configPath = "./server_configuration.txt";
	
	/*
	 * default Configuration
	 * 
	 * {
	 * 	serverAddress : 192.168.5.1
	 * 	serverPort: 5000,
	 * 	registryPort : 1111,
	 * 	multicastBase: 224.0.0.1,
	 * 	multicastLimit: 224.0.0.11 
	 * }
	 */
	
	public static final String serverAddress = "serverAddress";
	public static final String serverPort = "serverPort";
	public static final String registryPort = "registryPort";
	public static final String multicastBase = "multicastBase";	
	public static final String multicastLimit = "multicastLimit";

	private static JSONParser parser = new JSONParser();
    public static JSONObject config = new JSONObject();
	
	public static void initialize() throws IOException {
		config.put(serverAddress, null);
		config.put(serverPort, null);
		config.put(registryPort, null);
		config.put(multicastBase, null);
		config.put(multicastLimit, null);
		saveConfiguration();
	}
	
	public static void defaultConfig() throws IOException {
		config.put(serverAddress, "192.168.5.1");
		config.put(serverPort, "5000");
		config.put(registryPort, "1111");
		config.put(multicastBase, "224.0.0.1");
		config.put(multicastLimit, "224.0.0.11");
		saveConfiguration();
	}
	
	public static void saveConfiguration() throws IOException {
		FileWriter file = new FileWriter(configPath);
		file.write(config.toJSONString());
		System.out.println("Successfully Copied JSON Object to File...");
		System.out.println("JSON Object: " + config);
		file.flush();
		file.close();
	}
	
	public static void loadConfiguration() throws FileNotFoundException, IOException, ParseException {
		Object obj;
		synchronized(parser) {
			obj = parser.parse(new FileReader(configPath));
		}
		config = (JSONObject) obj;
	}
}
