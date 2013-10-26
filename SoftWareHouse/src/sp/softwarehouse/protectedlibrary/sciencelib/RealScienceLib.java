package sp.softwarehouse.protectedlibrary.sciencelib;

import sp.softwarehouse.protectedlibrary.Exceptions.InvalidLicenseException;

/**
 * A non-real version of the API that the developer can use to test their code.
 */
public class RealScienceLib extends AScienceLib {

	private static final String[] paths = {"#PATHS#"};
	private static final String[] checksums = {"#SUMS#"};

	public RealScienceLib() throws InvalidLicenseException {
		verifyChecksums(paths, checksums);
	}
	
	@Override
	public int getScience() {
		return 66; // the most science number
	}

}
