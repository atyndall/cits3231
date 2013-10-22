package sp.developer.exampleusage;

import sp.softwarehouse.protectedlibrary.*;
import sp.softwarehouse.protectedlibrary.Exceptions.*;
import sp.softwarehouse.protectedlibrary.sciencelib.ScienceLib;

public class ExampleLinking {

	public static void runExample() {
		try {
			// We try to run the API
			DeveloperLicense lic = DeveloperLicense.fromStream(ExampleLinking.class.getResourceAsStream("licenses/developer1-0.lic"));
			ScienceLib s = new ScienceLib(lic);
			System.out.println(s.getScience());
		} catch (UnsuccessfulLinkingException e) {
			// We fallback to the debug API if the real API is not yet linked
			ScienceLib s = new ScienceLib(true);
			System.out.println(s.getScience());
		} catch (InvalidLicenseException e) {
			// If we don't have a license, we panic
			e.printStackTrace();
			System.exit(1);
		}
	}
	
}
