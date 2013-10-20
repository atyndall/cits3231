package sp.softwarehouse.libprovidingserver;


import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ILibProvidingServer extends Remote {
	public int giveMeLib() throws RemoteException;
}
