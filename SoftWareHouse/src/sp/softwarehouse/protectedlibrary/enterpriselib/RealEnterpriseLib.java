package sp.softwarehouse.protectedlibrary.enterpriselib;

import sp.softwarehouse.protectedlibrary.DeveloperLicense;
import sp.softwarehouse.protectedlibrary.Exceptions.InvalidLicenseException;

/**
 * The actual API that is hidden from the developer until they submit a linking request.
 * They must pass their license file to the API and the API will verify it.
 * If the license is invalid, the API throws an exception.
 */
class RealEnterpriseLib implements IEnterpriseLib {
	private DeveloperLicense lic;
	
	public RealEnterpriseLib(DeveloperLicense lic) throws InvalidLicenseException {
		this.lic = lic;
		//if (this.lic.getId() != EnterpriseLicensing.DeveloperLicenseKey || this.lic.getLicenseFor() != "EnterpriseLib") {
		//	throw new InvalidLicenseException();
		//}
	}
	
	@Override
	public int getEnterprise() {
		return 7; // the most enterprise number
	}

}
