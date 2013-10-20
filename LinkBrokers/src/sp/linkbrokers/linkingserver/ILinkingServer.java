package sp.linkbrokers.linkingserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ILinkingServer extends Remote {
	public int giveMeCake() throws RemoteException;
}
