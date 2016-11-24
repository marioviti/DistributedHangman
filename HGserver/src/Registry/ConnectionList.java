package Registry;

import java.util.HashMap;

public class ConnectionList {

	private static HashMap<Integer,Connection> loggedConn = new HashMap<Integer,Connection>();
	private static HashMap<Integer,Connection> expiredConnection = new HashMap<Integer,Connection>();
	
	public static void addConnection(int cookie, Object updater) throws RegistrationExcpetion {
		synchronized(loggedConn) {
			loggedConn.put(cookie, new Connection(cookie, updater));
		}
	}
	
	public static Connection getConnection(int cookie) {
		synchronized(expiredConnection) {
			return loggedConn.get(cookie);
		}
	}

	/**
	 * sends an update to all logged users
	 * @param update
	 */
	public static void sendBroadcastUpdate(String update) {
		synchronized(loggedConn) {
			for(Integer cookie: loggedConn.keySet()) {
				loggedConn.get(cookie).update(update);
			}
		}
	}
	
	/** 
	 * method used by the garbage collector
	 * @return reference to the map of expired connection
	 */
	public static HashMap<Integer,Connection> collectExpired() {
		synchronized(loggedConn) { synchronized(expiredConnection) {
			for(Integer cookie: loggedConn.keySet()) {
				if(loggedConn.get(cookie).expired()) {
					expiredConnection.put(cookie, loggedConn.get(cookie));
				}
			}
			for(Integer cookie: expiredConnection.keySet()) {
				loggedConn.remove(cookie);
			}
			
		} }
		return expiredConnection;
	}

	public static void clearExpiredReferences() {
		synchronized(expiredConnection) {
			expiredConnection.clear();
		}
	} 
}
