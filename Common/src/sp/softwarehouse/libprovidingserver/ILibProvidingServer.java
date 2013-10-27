package sp.softwarehouse.libprovidingserver;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import sp.common.SoftwareHouseRequest;
import sp.softwarehouse.protectedlibrary.Exceptions.InvalidLicenseException;

public interface ILibProvidingServer extends Remote {
	public Map<String, byte[]> getClassesToLink(SoftwareHouseRequest req) throws InvalidLicenseException, Exception, RemoteException;
}
