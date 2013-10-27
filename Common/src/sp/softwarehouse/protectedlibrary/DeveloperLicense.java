package sp.softwarehouse.protectedlibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DeveloperLicense implements Serializable {

	private static final long serialVersionUID = -865416936996786148L;
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
	
	private static String convertFileToString(File file) throws IOException {
		InputStream in = new FileInputStream(file);
		byte[] b  = new byte[(int)file.length()];
		int len = b.length;
		int total = 0;

		while (total < len) {
		  int result = in.read(b, total, len - total);
		  if (result == -1) {
		    break;
		  }
		  total += result;
		}

		return new String(b, "UTF-8");
	}
	
	public static DeveloperLicense fromFile(File f) throws IOException {
		String lic = convertFileToString(f);
		String[] licParts = lic.split("\\|");
		return new DeveloperLicense(licParts[0], licParts[1], licParts[2].trim());
	}
	
	public void toFile(File f) throws IOException {
		List<String> lst = new ArrayList<String>(1);
		lst.add(this.lic + "|" + this.identifier + "|" + this.developerName);
		Files.write(f.toPath(), lst, Charset.forName("UTF-8"));
	}
	
}
