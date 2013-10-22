package sp.softwarehouse.protectedlibrary.sciencelib;

/**
 * A non-real version of the API that the developer can use to test their code.
 */
public class RealScienceLib implements IScienceLib {
	
	@Override
	public int getScience() {
		return 66; // the most science number
	}

}
