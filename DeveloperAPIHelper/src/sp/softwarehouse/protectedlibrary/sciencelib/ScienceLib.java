package sp.softwarehouse.protectedlibrary.sciencelib;


import sp.softwarehouse.protectedlibrary.DeveloperLicense;
import sp.softwarehouse.protectedlibrary.Exceptions.InvalidLicenseException;
import sp.softwarehouse.protectedlibrary.Exceptions.UnsuccessfulLinkingException;
import sp.softwarehouse.protectedlibrary.RealLibLinker;



/**
 * API class the developer interfaces with.
 * Uses Java reflection to prevent compile-time errors because the Real API is absent.
 */
public class ScienceLib implements IScienceLib {

	private IScienceLib actualAPI;
	
	public ScienceLib(DeveloperLicense lic) throws UnsuccessfulLinkingException, InvalidLicenseException  {
		this.actualAPI = RealLibLinker.getRealLib(IScienceLib.class, "sp.softwarehouse.protectedlibrary.enterpriselib.RealScienceLib", lic);
	}
	
	public ScienceLib(boolean debugMode) {
		if (debugMode) {
			this.actualAPI = new FakeScienceLib();
		} else {
			throw new RuntimeException("debugMode must be true if called");
		}
	}
	
	public int getScience() {
		return actualAPI.getScience();
	}
	
}
