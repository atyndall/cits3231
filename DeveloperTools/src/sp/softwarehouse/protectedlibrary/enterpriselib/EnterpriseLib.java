package sp.softwarehouse.protectedlibrary.enterpriselib;

import sp.softwarehouse.protectedlibrary.DeveloperLicense;
import sp.softwarehouse.protectedlibrary.Exceptions.InvalidLicenseException;
import sp.softwarehouse.protectedlibrary.Exceptions.UnsuccessfulLinkingException;
import sp.softwarehouse.protectedlibrary.RealLibLinker;



/**
 * API class the developer interfaces with.
 * Uses Java reflection to prevent compile-time errors because the Real API is absent.
 */
public class EnterpriseLib implements IEnterpriseLib {

	private IEnterpriseLib actualAPI;
	
	public EnterpriseLib(DeveloperLicense lic) throws UnsuccessfulLinkingException, InvalidLicenseException  {
		this.actualAPI = RealLibLinker.getRealLib(IEnterpriseLib.class, "sp.softwarehouse.protectedlibrary.enterpriselib.RealEnterpriseLib", lic);
	}
	
	public EnterpriseLib(boolean debugMode) {
		if (debugMode) {
			this.actualAPI = new FakeEnterpriseLib();
		} else {
			throw new RuntimeException("debugMode must be true if called");
		}
	}
	
	public int getEnterprise() {
		return actualAPI.getEnterprise();
	}
	
}
