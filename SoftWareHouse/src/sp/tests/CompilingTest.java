package sp.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.KeyStore.ProtectionParameter;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import sp.softwarehouse.libprovidingserver.LibProvidingServer;

public class CompilingTest {

	/**
	 * @param args
	 * @throws KeyStoreException 
	 * @throws IOException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws UnrecoverableEntryException 
	 */
	public static void main(String[] args) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableEntryException {
		URL trustDir = CompilingTest.class.getResource("/truststore-softwarehouse.jks");
		
		String trustPath;
		try {
			trustPath = URLDecoder.decode(trustDir.getPath(), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return;
		}
		
		System.out.println("Truststore: " + trustPath);
		
		System.setProperty("javax.net.ssl.keyStore", trustPath);
		System.setProperty("javax.net.ssl.keyStorePassword", "123456");
		System.setProperty("javax.net.ssl.trustStore", trustPath);
		System.setProperty("javax.net.ssl.trustStorePassword", "123456");
		
		LibProvidingServer svr = new LibProvidingServer();
		
		Map<String, byte[]> checksums = new HashMap<String, byte[]>();
		byte[] bytes = {0x40, 0x30};
		checksums.put("sp/test/seven.java", bytes);
		
		svr.getClassBytes("ScienceLib", checksums);
	}
}
