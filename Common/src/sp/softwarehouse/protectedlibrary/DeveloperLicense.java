package sp.softwarehouse.protectedlibrary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DeveloperLicense {

	private String lic;
	private String developerName;
	private String identifier;
	
	
	public DeveloperLicense(String encryptedLicense, String identifier, String developerName) {
		this.lic = encryptedLicense;
		this.identifier = identifier;
		this.developerName = developerName;
	}

	public String getLicense() {
		return lic;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public String getDeveloperName() {
		return developerName;
	}
	
	private static String convertStreamToString(InputStream is) {
	    Scanner s = new Scanner(is).useDelimiter("\\A"); 
	    return s.hasNext() ? s.next() : "";
	}
	
	public static DeveloperLicense fromStream(InputStream f) {
		String lic = convertStreamToString(f);
		String[] licParts = lic.split("|");
		return new DeveloperLicense(licParts[0], licParts[1], licParts[2]);
	}
	
	public void toFile(File f) throws IOException {
		List<String> lst = new ArrayList<String>(1);
		lst.add(this.lic + "|" + this.identifier + "|" + this.developerName);
		Files.write(f.toPath(), lst, Charset.forName("UTF-8"));
	}
	
}
