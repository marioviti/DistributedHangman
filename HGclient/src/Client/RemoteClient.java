package Client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import RMI.Client;

public class RemoteClient extends UnicastRemoteObject implements Client {
	
	public static String gameList;

	public RemoteClient() throws RemoteException {
		super();
	}
	
	private static final long serialVersionUID = 112L;

	@Override
	public String update(String update) throws RemoteException{
		System.out.println(update);
		RemoteClient.gameList = update;
		return null;
	}
}
