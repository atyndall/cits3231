package sp.developer.sampleproject;

import sp.softwarehouse.protectedlibrary.Exceptions.*;
import sp.softwarehouse.protectedlibrary.sciencelib.ScienceLib;

public class SampleProject {

	public static void main(String[] args) {
		try {
			// We try to run the API
			ScienceLib s = new ScienceLib();
			System.out.println("Attempting to link against real library: SUCCESS");
			System.out.println(s.getScience());
		} catch (UnsuccessfulLinkingException e) {
			// We fallback to the debug API if the real API is not yet linked
			System.out.println("Real linking unsuccessful; using fake library");
			ScienceLib s = new ScienceLib(true);
			System.out.println(s.getScience());
		}
	}
	
}
