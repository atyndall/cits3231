package sp.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import org.junit.BeforeClass;

public class TestHelper {
	static final char[] KEYSTORE_PASSWORD = "123456".toCharArray();
	static protected String KEYSTORE_ALGORITHM = "jks";
	static protected String KEYSTORE_FILE = "DeveloperTools/config/truststore-developer1.jks";
	static private String DEVELOPER_ALIAS = "developer1";
	static final String ENCRYPTION_TYPE = "RSA";
	static KeyStore keyStore;
	public static File validLicenseFile;
	
	private static final String LICENSE_DELIMITER = "|";
	protected static final String validEncryptedLicense = "123456";
	protected static final String validIdentifier = "1";
	protected static final String validDeveloperName = "George";
	
	@BeforeClass
	public static void setUpClass(){
    	validLicenseFile = createLicenseFile(validEncryptedLicense, validIdentifier, validDeveloperName);
	}

	protected static Certificate getDevelopersCertificate(){
		keyStore = getKeyStore();
		Certificate cert = null;
		
		try {
			cert = keyStore.getCertificate(DEVELOPER_ALIAS);
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		
		return cert;
	}
	
	protected static KeyStore getKeyStore(){
		KeyStore returnValue = null;
		
		if(keyStore == null){
			try {
				FileInputStream keyStoreFile = new FileInputStream(KEYSTORE_FILE);
				returnValue = KeyStore.getInstance(KEYSTORE_ALGORITHM);
				returnValue.load(keyStoreFile, KEYSTORE_PASSWORD);
			} catch (KeyStoreException | NoSuchAlgorithmException
					| CertificateException | IOException e) {
				e.printStackTrace();
			}
		}
		
		return returnValue;
	}
	
	protected static File createLicenseFile(String license, String identifier, String developerName) {
		File licenseFile = null;
		String[] licenseAttribtes = new String[]{validEncryptedLicense, identifier, developerName};
		
		try{
			licenseFile = File.createTempFile("validLicense", ".tmp");
			FileOutputStream out = new FileOutputStream(licenseFile);

			for(String attribute: licenseAttribtes){
				out.write((attribute + LICENSE_DELIMITER).getBytes("UTF-8"));
			}
				
			out.close();
    	} catch(IOException e){
    	   e.printStackTrace();
    	}
		
		return licenseFile;
	}
	
}
