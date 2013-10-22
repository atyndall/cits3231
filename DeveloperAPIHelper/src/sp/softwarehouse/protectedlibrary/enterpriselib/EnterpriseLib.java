package sp.softwarehouse.protectedlibrary.enterpriselib;

import sp.softwarehouse.protectedlibrary.Exceptions.UnsuccessfulLinkingException;
import sp.softwarehouse.protectedlibrary.enterpriselib.FakeEnterpriseLib;
import sp.softwarehouse.protectedlibrary.enterpriselib.IEnterpriseLib;
import sp.softwarehouse.protectedlibrary.RealLibLinker;



/**
 * API class the developer interfaces with.
 * Uses Java reflection to prevent compile-time errors because the Real API is absent.
 */
public class EnterpriseLib implements IEnterpriseLib {

	private IEnterpriseLib actualAPI;
	
	public EnterpriseLib() throws UnsuccessfulLinkingException  {
		this.actualAPI = RealLibLinker.<IEnterpriseLib>getRealLib("sp.softwarehouse.protectedlibrary.enterpriselib.RealEnterpriseLib");
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
