package RMI;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public interface Server extends Remote {

	String registryName = "RemoteRequest";
	
	/**
	 * 
	 * @param userName
	 * @param userPasswd
	 * @throws RemoteException
	 */
	public void register ( String userName, String userPasswd ) throws RemoteException;
	
	/**
	 * 
	 * @param userName
	 * @param userPasswd
	 * @return
	 * @throws RemoteException
	 */
	public int login ( String userName, String userPasswd ) throws RemoteException;
	
	/**
	 * 
	 * @param connectionID
	 * @param callback
	 * @throws RemoteException
	 */
	public void registerClient( int connectionID, Object callback) throws RemoteException;
	
}
