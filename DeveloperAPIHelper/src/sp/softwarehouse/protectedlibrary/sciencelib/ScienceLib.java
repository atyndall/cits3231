package sp.softwarehouse.protectedlibrary.sciencelib;

import sp.softwarehouse.protectedlibrary.Exceptions.UnsuccessfulLinkingException;
import sp.softwarehouse.protectedlibrary.RealLibLinker;

/**
 * API class the developer interfaces with.
 * Uses Java reflection to prevent compile-time errors because the Real API is absent.
 */
public class ScienceLib extends AScienceLib {

	private AScienceLib actualAPI;
	
	public ScienceLib() throws UnsuccessfulLinkingException  {
		this.actualAPI = RealLibLinker.<AScienceLib>getRealLib("sp.softwarehouse.protectedlibrary.sciencelib.RealScienceLib");
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
