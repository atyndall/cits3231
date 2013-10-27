package sp.linkbrokers.linkingserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

import sp.requests.SoftwareHouseRequest;

public interface RemoteLinkingServer extends Remote {
	public byte[] performLink(SoftwareHouseRequest[] softwareHouseRequests, byte[] inJar) throws RemoteException;
}
