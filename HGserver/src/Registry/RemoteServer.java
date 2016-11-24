package Registry;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import RMI.Client;
import RMI.Server;

public class RemoteServer extends UnicastRemoteObject implements Server{
	
	public RemoteServer() throws RemoteException {
		super();
	}
	
	private static final long serialVersionUID = 112L;

	@Override
	public void register(String user, String password) throws RemoteException {
		if(user == null || password == null) throw new RemoteException("null Object passed");
		if(user.equals("") || password.equals("")) throw new RemoteException("at least one char");
		try {
			Lobby.register(user, password);
			Lobby.saveLobby();
		} catch (RegistrationExcpetion e) {
			throw new RemoteException(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int login(String user, String password)throws RemoteException {
		if(user == null || password == null) throw new RemoteException("null Object passed");
		try {
			int cookie = Lobby.login(user, password);
			Lobby.saveLobby();
			return cookie;
		} catch (RegistrationExcpetion e) {
			throw new RemoteException(e.getMessage());
		} catch (IOException e) {
			throw new RemoteException(e.getMessage());
		}
	}

	@Override
	public void registerClient(int connectionID, Object updater) throws RemoteException {
		((Client) updater).update(Lobby.getGamesList());
		try {
			ConnectionList.addConnection(connectionID,updater);
		} catch (RegistrationExcpetion e) {
			throw new RemoteException(e.getMessage());
		}
	}
}