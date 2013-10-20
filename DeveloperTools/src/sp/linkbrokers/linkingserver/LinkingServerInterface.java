package sp.linkbrokers.linkingserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LinkingServerInterface extends Remote {
	public int giveMeCake() throws RemoteException;
}
