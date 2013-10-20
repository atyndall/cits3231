package sp.softwarehouse.protectedlibrary.sciencelib;

import sp.softwarehouse.protectedlibrary.DeveloperLicense;
import sp.softwarehouse.protectedlibrary.Exceptions.InvalidLicenseException;

/**
 * A non-real version of the API that the developer can use to test their code.
 */
class RealScienceLib implements IScienceLib {

	private DeveloperLicense lic;
	
	public RealScienceLib(DeveloperLicense lic) throws InvalidLicenseException {
		this.lic = lic;
		//if (this.lic.getId() != ScienceLicensing.DeveloperLicenseKey || this.lic.getLicenseFor() != "ScienceLib") {
		//	throw new InvalidLicenseException();
		//}
	}
	
	@Override
	public int getScience() {
		return 66; // the most science number
	}

}
