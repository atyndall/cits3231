package sp.softwarehouse.protectedlibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class DeveloperLicense implements Serializable {

	private static final long serialVersionUID = -865416936996786148L;
	static final String DELIMITER = "\\|";
	
	private String license;
	private String developerName;
	private String identifier;
	
	
	public static DeveloperLicense createLicense(File file) throws IOException {
		String license = fileToString(file);
		String[] licParts = license.split(DELIMITER);
		
		if(licParts.length != 3)
			throw new IllegalArgumentException("Malformed license: '" + license + "'");
		
		String encryptedLicense = licParts[0];
		String identifier = licParts[1];
		String developerName = licParts[2].trim();
		
		return new DeveloperLicense(encryptedLicense, identifier, developerName);
	}

	public String getLicense() {
		return license;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public String getDeveloperName() {
		return developerName;
	}
	
	public void toFile(File f) throws IOException {
		List<String> lst = new ArrayList<String>(1);
		lst.add(this.license + "|" + this.identifier + "|" + this.developerName);
		Files.write(f.toPath(), lst, Charset.forName("UTF-8"));
	}

	private DeveloperLicense(String encryptedLicense, String identifier, String developerName) {
		this.license = encryptedLicense;
		this.identifier = identifier;
		this.developerName = developerName;
	}

	private static String fileToString(File file) throws IOException {
		InputStream inputStream = new FileInputStream(file);
		
		int fileLength = (int) file.length();
		byte[] fileAsCharArray  = new byte[fileLength];
		
		int bytesReadSoFar = 0;
	
		while (bytesReadSoFar < fileLength) {
		  int result = inputStream.read(fileAsCharArray, bytesReadSoFar, fileLength - bytesReadSoFar);
		  
		  if (result == -1)
		    break;
		  
		  bytesReadSoFar += result;
		}
	
		inputStream.close();
		
		return new String(fileAsCharArray, "UTF-8");
	}
	
}
