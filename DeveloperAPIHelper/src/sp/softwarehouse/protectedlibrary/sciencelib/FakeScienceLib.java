package sp.softwarehouse.protectedlibrary.sciencelib;


import sp.softwarehouse.protectedlibrary.sciencelib.IScienceLib;

/**
 * A non-real version of the API that the developer can use to test their code.
 */
class FakeScienceLib implements IScienceLib {

	public FakeScienceLib() {
		
	}
	
	@Override
	public int getScience() {
		return 59; // the least science number
	}

}
