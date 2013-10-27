package sp.tests;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class TestHelper {
	static final char[] KEYSTORE_PASSWORD = "123456".toCharArray();
	static protected String KEYSTORE_ALGORITHM = "jks";
	static protected String KEYSTORE_FILE = "DeveloperTools/config/truststore-developer1.jks";
	static private String DEVELOPER_ALIAS = "developer1";
	static final String ENCRYPTION_TYPE = "RSA";
	static KeyStore keyStore;

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
	
}
