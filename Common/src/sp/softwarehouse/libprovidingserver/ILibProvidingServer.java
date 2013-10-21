package sp.softwarehouse.libprovidingserver;


import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.Remote;
import java.util.Map;

import sp.common.SoftwareHouseRequest;
import sp.softwarehouse.protectedlibrary.Exceptions.InvalidLicenseException;

public interface ILibProvidingServer extends Remote {
	public Map<String, InputStream> getClassesToLink(SoftwareHouseRequest req) throws InvalidLicenseException, Exception;
}
