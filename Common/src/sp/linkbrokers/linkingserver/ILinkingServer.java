package sp.linkbrokers.linkingserver;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import sp.common.SoftwareHouseRequest;

public interface ILinkingServer extends Remote {
	public byte[] performLink(SoftwareHouseRequest req, byte[] inJar) throws RemoteException;
}
