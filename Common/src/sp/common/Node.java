package sp.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;

public abstract class Node extends LoggedItem {
	protected HashMap<String, String> options;
	/**
	 * The location of the keystore used to contain the public keys of Link Brokers
	 * and Software Houses, as well as the protected key of the Developer client. This
	 * can be set on the comman line using:
	 *  	-keystore FileLocation
	 */
	static protected final String DEFAULT_KEYSTORE_LOCATION = "DeveloperTools/config/truststore-developer1.jks";

	/**
	 * The default password for the keystore. This can be se using:
	 * 		-password Password
	 */
	static protected final String DEFAULT_KEYSTORE_PASSWORD = "123456";
	
	/**
	 * The type of keystore file to use.
	 */
	static protected final String DEFAULT_KEYSTORE_TYPE = "jks";
	
	static protected final String DEFAULT_KEYSTORE_ALIAS = "developer1";

	/**
	 * The encryption parameters to use for encrypting the Software House linking
	 * requests
	 */
	static protected final String DEFAULT_ENCRYPTION = "RSA";
	
	static protected final String DEFAULT_SYMETRICAL_ENCRYPTION = "AES";

	/**
	 * Contains a reference to the keystore which contains the Developer's protected
	 * key and the public keys of all other entities
	 */
	protected KeyStore keyStore;
	
	
	public Node(){
		options = new HashMap<String, String>();
	}
	
	protected Key getPrivateKey() {
		Key privateKey = null;
		
		try {
			
			privateKey = getKeyStore().getKey(keyStoreAlias(), keyStorePassword());
			
		} catch (UnrecoverableKeyException | KeyStoreException
				| NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return privateKey;
	}
	
	protected Key getPublicKey(String node) {
		Key publicKey = null;
		
		try {
			
			publicKey = getKeyStore().getKey(node, keyStorePassword());
			
		} catch (UnrecoverableKeyException | KeyStoreException
				| NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return publicKey;
	}
	

	/**
	 * Returns the key store that contains the developer's protected key and all the
	 * other entitie's public keys
	 * @return Keystore object that contains all of the keys for encryption and 
	 * decryption
	 */
	protected KeyStore getKeyStore() {
		if(keyStore == null){
			try {
				FileInputStream keyStoreFile = new FileInputStream(keyStoreFile());
				keyStore = KeyStore.getInstance(keyStoreType());
				keyStore.load(keyStoreFile, keyStorePassword() );
			} catch (FileNotFoundException e) {
				logErrorAndExit("Unable to find keystore file: " + keyStoreFile());
			} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException  e) {
				e.printStackTrace();
			} 
		}
		
		return keyStore;
	}
	

	protected char[] keyStorePassword() {
		return options.get("keyStorePassword").toCharArray();
	}
	
	protected String keyStoreAlias(){
		return options.get("keyStoreAlias");
	}

	protected String encryption() {
		return options.get("encryption");
	}
	
	protected String symmetricEncryption(){
		return options.get("symmetricEncryption");
	}

	protected String keyStoreFile() {
		return options.get("keyStoreFile");
	}

	protected String keyStoreType() {
		return options.get("keyStoreType");
	}
}
