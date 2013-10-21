package sp.tests;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import sp.softwarehouse.protectedlibrary.DeveloperLicense;
import sp.softwarehouse.protectedlibrary.LicenseManager;


public class LicensingTest {

	/**
	 * @param args
	 * @throws KeyStoreException 
	 * @throws IOException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws UnrecoverableEntryException 
	 */
	public static void main(String[] args) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableEntryException {

		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		
		InputStream fis = LicensingTest.class.getResourceAsStream("/truststore-softwarehouse.jks");
		ks.load(fis, "123456".toCharArray());

		ProtectionParameter pp = new KeyStore.PasswordProtection("123456".toCharArray());
		KeyStore.PrivateKeyEntry ken = (KeyStore.PrivateKeyEntry) ks.getEntry("softwarehouse", pp);
		
		PrivateKey privk = ken.getPrivateKey();

		URL url = LicensingTest.class.getResource("/lm.db");
		
		File lmdb = new File(url.getFile());
		
		LicenseManager lm = new LicenseManager(lmdb, privk);
		
		DeveloperLicense lic = lm.generateLicense("Joe Bloggs");
		
		boolean isValid = lm.validLicense(lic);
		assert(isValid == true);
		
		lm.consumeLicense(lic);
		
		boolean isValid2 = lm.validLicense(lic);
		assert(isValid2 == false);
		
		for (int i = 0; i < 10; i++) {
			lm.generateLicense("Joe Bloggs").toFile(new File("developer1-" + i + ".lic"));
		}
		
		System.out.println("ttt");

	}

}
