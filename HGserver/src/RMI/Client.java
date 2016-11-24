package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Client extends Remote {
	public String update(String update) throws RemoteException;
}
