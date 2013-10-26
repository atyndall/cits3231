package sp.common;

import java.io.File;
import java.io.FilenameFilter;


public class LicenseFilter implements FilenameFilter {

	public boolean accept(File dir, String name) {
		return name.endsWith(".lic");
	}

}
