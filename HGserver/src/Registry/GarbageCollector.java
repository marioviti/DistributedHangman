package Registry;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import Registry.Connection;
import Registry.ConnectionList;
import Registry.RequestReactor;

public class GarbageCollector extends Thread {
	
	private volatile boolean running;
	private static Executor reactorsPool;
	
	public void run() {
		reactorsPool = Executors.newCachedThreadPool(); 
		running = true;
		HashMap<Integer,Connection> expiredConnections;
		try {
			while(running) {
				ConnectionList.clearExpiredReferences();
				sleep(1*60*1000);
				expiredConnections = ConnectionList.collectExpired();
				for(Integer cookie: expiredConnections.keySet()) {
					log("removing " + cookie);
					reactorsPool.execute(new RequestReactor(null, expiredConnections.get(cookie)));
				}
				log("collector done");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void shutdown() {
		running = false;
	}
	
	public void log(String s) {
		System.err.println(this.getClass().getName()+ ": " + s);
	}

}
