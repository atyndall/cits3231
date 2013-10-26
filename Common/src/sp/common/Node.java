package sp.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
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
	static protected final String DEFAULT_KEYSTORE_LOCATION = "DeveloperTools/bin/truststore-developer1.jks";

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
	
	static protected final String DEFAULT_SYMETRICAL_ENCRYPTION = "AES";

	/**
	 * Contains a reference to the keystore which contains the Developer's protected
	 * key and the public keys of all other entities
	 */
	protected KeyStore keyStore;
	
	
	public Node(){
		options = new HashMap<String, String>();
	}
	
	public String getSymmetricEncryption(){
		return options.get("symmetricEncryptionType");
	}
	
	public int getPort(){
		int port;
		
		try{
			port = Integer.parseInt(options.get("port"));
		} catch(NumberFormatException e){
			port = -1;
		}
		
		return port;
	}

	protected PrivateKey getPrivateKey() {
		PrivateKey privateKey = null;
		
		try {
			
			privateKey = (PrivateKey) getKeyStore().getKey(keyStoreAlias(), keyStorePassword());
			
		} catch (UnrecoverableKeyException | KeyStoreException
				| NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if(privateKey == null)
			logErrorAndExit("Could not find alias '" + keyStoreAlias() + "' in keystore.");
		
		return privateKey;
	}
	
	protected PublicKey getPublicKey(String node) {
		Certificate cert = null;
		
		try {
			
			cert = getKeyStore().getCertificate(node);
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		
		if(cert == null)
			logErrorAndExit("Unable to locate alias '" + node + "' in keyStore. All linking requests were cancelled.");
		
		return cert.getPublicKey();
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
			} catch (KeyStoreException e){
				logErrorAndExit("Unsupported keystore file: " + keyStoreFile());
			} catch(CertificateException e){
				logErrorAndExit("eystore file '" + keyStoreFile() + "' contained a certificate that could not be opened.");
			} catch(IOException  e) {
				if(e.getCause() instanceof UnrecoverableKeyException)
					logErrorAndExit("Incorred password for keystore file: " + keyStoreFile());
				
				logErrorAndExit("Corrupted or inaccessible keystore file: " + keyStoreFile());
			} catch(NoSuchAlgorithmException e){
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

	protected String keyStoreFile() {
		return options.get("keyStoreFile");
	}

	protected String keyStoreType() {
		return options.get("keyStoreType");
	}
}
