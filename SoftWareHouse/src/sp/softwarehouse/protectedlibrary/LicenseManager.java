package sp.softwarehouse.protectedlibrary;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.List;


/**
 * Manages licensing information.
 */
public class LicenseManager {

	Path p;
	String hexKey;
	List<String> database;
	SecureRandom random;
	MessageDigest md;
	
	/**
	 * @param f Place to store licensing info
	 * @param key Private Key used to encrypt license info
	 */
	public LicenseManager(File f, PrivateKey key) {
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException("We need this algo");
		}
		
		try {
			this.p = Paths.get(URLDecoder.decode(f.getAbsolutePath(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException("We need UTF-8");
		}
		
		StringBuilder sb = new StringBuilder();
	    for (byte b : key.getEncoded()) {
	        sb.append(String.format("%02X", b));
	    }
		this.hexKey = sb.toString();
		
		this.random = new SecureRandom();
		pullDatabase();
	}
	
	private void pullDatabase() {
		try {
			this.database = Files.readAllLines(p, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Database cannot be read");
		}
	}
	
	private void pushDatabase() {
		try {
			Files.write(p, this.database, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Database cannot be written");
		}
	}
	
	private String hashHex(String in) {
		StringBuilder sb = new StringBuilder();
	    for (byte b : md.digest(in.getBytes(Charset.forName("UTF-8")))) {
	        sb.append(String.format("%02X", b));
	    }
		return sb.toString();
	}
	
	private String getEnc(String id, String developerName) {
		return hexKey + "|" + id + "|" + developerName;
	}
	
	public DeveloperLicense generateLicense(String developerName) {
		String id = new BigInteger(130, random).toString(32);
		String enc = getEnc(id, developerName);

		return new DeveloperLicense(hashHex(enc), id, developerName);
	}
	
	public boolean validLicense(DeveloperLicense l) {
		String enc = getEnc(l.getIdentifier(), l.getDeveloperName());
		return !database.contains(l.getIdentifier()) && hashHex(enc).equals(l.getLicense());
	}
	
	public void consumeLicense(DeveloperLicense l) {
		String enc = getEnc(l.getIdentifier(), l.getDeveloperName());
		database.add(l.getIdentifier());
		pushDatabase();
	}
	
}
