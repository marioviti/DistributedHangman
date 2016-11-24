package RMI;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public interface Server extends Remote {

	String registryName = "RemoteRequest";
	
	public void register ( String userName, String userPasswd ) throws RemoteException;
	
	public int login ( String userName, String userPasswd ) throws RemoteException;
	
	public void registerClient( int connectionID, Object callback) throws RemoteException;
	
}
