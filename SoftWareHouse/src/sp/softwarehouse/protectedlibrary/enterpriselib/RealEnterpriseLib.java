package sp.softwarehouse.protectedlibrary.enterpriselib;

import sp.softwarehouse.protectedlibrary.Exceptions.InvalidLicenseException;

/**
 * The actual API that is hidden from the developer until they submit a linking request.
 * They must pass their license file to the API and the API will verify it.
 * If the license is invalid, the API throws an exception.
 */
public class RealEnterpriseLib extends AEnterpriseLib {

	private static final String[] paths = {"#PATHS#"};
	private static final String[] checksums = {"#SUMS#"};

	public RealEnterpriseLib() throws InvalidLicenseException {
		verifyChecksums(paths, checksums);
	}

	@Override
	public int getEnterprise() {
		return 7; // the most enterprise number
	}

}
