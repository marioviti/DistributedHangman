package Registry;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.json.simple.parser.ParseException;

import RMI.Server;

public class ServerApp {
	
	public static McastAddrGenerator addressGenerator;
	
	public static void main(String[] args) throws IOException, ParseException, InterruptedException {
		
		Lobby.initialize();
		Config.defaultConfig();
		Config.loadConfiguration();
		
		String serverAddress = (String) Config.config.get(Config.serverAddress);
		String serverPort = (String) Config.config.get(Config.serverPort);
		String registryPort = (String) Config.config.get(Config.registryPort);
		String multicastBase = (String) Config.config.get(Config.multicastBase);
		String multicastLimit = (String) Config.config.get(Config.multicastLimit);
				
		addressGenerator = new McastAddrGenerator(multicastBase,multicastLimit);
		
		String serverPath = "java.rmi.server.hostname";
		System.setProperty(serverPath, serverAddress);
		
		RemoteServer objServer = new RemoteServer();
		Registry reg = LocateRegistry.createRegistry(Integer.parseInt(registryPort));
		reg.rebind(Server.registryName, objServer);
		
		System.out.println("Hang Man Server started");
		Thread server = new Thread( new RequestServer(Integer.parseInt(serverPort)));
		GarbageCollector garColl = new GarbageCollector();
		server.start();
		garColl.start();
		server.join();
		garColl.shutdown();
		System.out.println("Hang Man Server stopped");
		Lobby.saveLobby();
	}
}
