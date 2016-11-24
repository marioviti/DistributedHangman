package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Client extends Remote {
	
	/**
	 * Callback
	 * 
	 * @param update
	 * @return
	 * @throws RemoteException
	 */
	public String update(String update) throws RemoteException;
}
