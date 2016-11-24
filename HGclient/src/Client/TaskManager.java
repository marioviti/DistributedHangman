package Client;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.json.simple.JSONObject;

public class TaskManager extends Thread{

	private RequesSender interf;
	private ResponseListener listener;
	private Connection playerConnection;

	private static int size = 10;
	public static final String shuttingDownFinalMessage = "shutting down the TaskManager";
	public static final String gameStarting = "game is starting...";
	public static final String notAllowed = "not allowed: ";
	public static final String alreadyA = "already a ";
	public static final String notA = "not a ";
	public static final String Aguesser = "guesser";
	public static final String Amaster = "guesser";

	
	private static BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(size);
	private static volatile boolean running = false;
	
	/**
	 * main Thread for User Agent task execution
	 * 
	 * @param interf
	 * @param listener
	 * @param playerConnection
	 */
	public TaskManager(RequesSender interf, ResponseListener listener, Connection playerConnection) {
		this.interf = interf;
		this.listener = listener;
		this.playerConnection = playerConnection;
	}

	public void run() {
		
		TaskManager.running = true;
		log("running...");
		try {
			while(running) {
				manage(TaskManager.queue.take());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		log("shutting down...");
	}
	
	private void manage(Object task) throws SocketException, IOException, InterruptedException {
		if(task instanceof Message)
			react((Message) task);
	}
	
	private void react(Message message) throws IOException {
		log(message.getMessage().toJSONString());
		String task = message.header(Message.fieldType);
		iLog(task);
		switch(task) {
			case Message.typeCreateGame : {
				if(Config.config.get(Config.gameMode)==null) {
					Config.config.put(Config.gameMode, Config.masterMode);
					Config.masterGame.put(Config.masterGameSecretWord, message.argument(0));
					playerConnection.contact(message.getMessage().toString());
				}else {
					iLog(TaskManager.notAllowed + TaskManager.alreadyA + TaskManager.Aguesser);
					iLog(TaskManager.notAllowed + TaskManager.alreadyA + TaskManager.Amaster);
				}
				break;
			}
			case Message.typeDeleteGame : {
				if(Config.config.get(Config.gameMode).equals(Config.masterMode)) {
					Config.config.put(Config.gameMode,null);
					playerConnection.contact(message.getMessage().toString());
				}else
					iLog(TaskManager.notAllowed + TaskManager.notA + TaskManager.Amaster);
				break;
			}
			case Message.typeJoinGame : {
				if(Config.config.get(Config.gameMode)==null) {
					Config.config.put(Config.gameMode, Config.guesserMode);
					playerConnection.contact(message.getMessage().toString());
				}else {
					iLog(TaskManager.notAllowed + TaskManager.alreadyA + TaskManager.Aguesser);
					iLog(TaskManager.notAllowed + TaskManager.alreadyA + TaskManager.Amaster);
				}
				break;
			}
			case Message.typeLeaveGame : {
				if(Config.config.get(Config.gameMode).equals(Config.guesserMode)) {
					Config.config.put(Config.gameMode,null);
					playerConnection.contact(message.getMessage().toString());
				}else
					iLog(TaskManager.notAllowed + TaskManager.notA + TaskManager.Aguesser);
				break;
			}
			case Message.typeGameHasBeenDeleted: {
				Config.config.put(Config.gameMode,null);
				break;
			}
			case Message.typeGameJoined: {
				break;
			}
			case Message.typeGuesserGameReady :{
				Config.config.put(Config.gamingMulticastAddress,message.argument(0));
				Config.config.put(Config.gamingPassWord, message.argument(1));
				Config.gusserGame.put(Config.guesserGameMaster, message.argument(2));
				Config.gusserGame.put(Config.guesserNumOfLetters, message.argument(3));
				Config.config.put(Config.gameMode,Config.guesserMode);
				iLog("guesser game starting... press ENTER to play");

				listener.shutdown();
				interf.shutdown();
				this.shutdown();
				break;
			}
			case Message.typeMasterGameReady : {
				Config.config.put(Config.gamingMulticastAddress,message.argument(0));
				Config.config.put(Config.gamingPassWord, message.argument(1));
				Config.config.put(Config.gameMode,Config.masterMode);
				iLog("master game starting...press ENTER to play");

				listener.shutdown();
				interf.shutdown();
				this.shutdown();
				break;
			} 
			case shuttingDownFinalMessage: {
				shutdown();
				break;
			}
			default: break;
		}
		boolean success = message.getSuccess();
		if(!success) {
			String cause = message.argument(0);
			switch(cause) {
			case Message.failSessionExpired: {
				listener.shutdown();
				interf.shutdown();
				Config.config.put(Config.cookie, null);
				Config.saveConfiguration();
				iLog("ERROR session expired: press ENTER");
				break;
				}
			}
		}
	}
	
	public static void add(Object task) throws InterruptedException {
		queue.put(task);
	}
	
	public static void induceShutdown(String cause) throws InterruptedException {
		Message finalMessage = new Message();
		finalMessage.setType(shuttingDownFinalMessage);
		finalMessage.setArgument(0, cause);
		finalMessage.failed();
		add(finalMessage);
	}
	
	public static void shutdown() {
		running = false;
	}
	
	public void log(String s) {
		System.err.println(this.getClass().getName()+ ": " + s);
	}
	
	public void iLog(String s) {
		System.out.println(this.getClass().getName()+ ": " + s);
	}
}
