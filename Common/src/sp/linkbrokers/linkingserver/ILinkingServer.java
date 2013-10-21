package sp.linkbrokers.linkingserver;

import java.rmi.Remote;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import sp.common.SoftwareHouseRequest;

public interface ILinkingServer extends Remote {
	public JarInputStream performLink(SoftwareHouseRequest req, JarInputStream inJar);
}
