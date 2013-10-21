package sp.softwarehouse.protectedlibrary.enterpriselib;

/**
 * The actual API that is hidden from the developer until they submit a linking request.
 * They must pass their license file to the API and the API will verify it.
 * If the license is invalid, the API throws an exception.
 */
class RealEnterpriseLib implements IEnterpriseLib {

	@Override
	public int getEnterprise() {
		return 7; // the most enterprise number
	}

}
