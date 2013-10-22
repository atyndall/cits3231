package sp.softwarehouse.protectedlibrary.enterpriselib;


import sp.softwarehouse.protectedlibrary.enterpriselib.IEnterpriseLib;

/**
 * A non-real version of the API that the developer can use to test their code.
 */
class FakeEnterpriseLib implements IEnterpriseLib {

	public FakeEnterpriseLib() {
		
	}
	
	@Override
	public int getEnterprise() {
		return 3; // the least enterprise number
	}

}
