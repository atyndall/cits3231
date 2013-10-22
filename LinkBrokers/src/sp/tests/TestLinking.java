package sp.tests;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.KeyStore.ProtectionParameter;
import java.security.cert.CertificateException;
import java.util.jar.JarInputStream;

import sp.common.LinkingRequest;
import sp.common.SoftwareHouseRequest;
import sp.linkbrokers.linkingserver.LinkingServer;
import sp.softwarehouse.protectedlibrary.DeveloperLicense;

public class TestLinking {

	public static String getResourceAsPath(String resourceName) {
		URL r = TestLinking.class.getResource(resourceName);
		
		String rp;
		try {
			rp = URLDecoder.decode(r.getPath(), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return null;
		}
		
		return rp;
	}
	
	public static byte[] streamToByte(InputStream s) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int read = 0;
		byte[] bytes = new byte[1024];
		while ((read = s.read(bytes)) != -1) {
			bos.write(bytes, 0, read);
		}
		
		return bos.toByteArray();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		InputStream fis = TestLinking.class.getResourceAsStream("/truststore-linkbrokers.jks");
		String trustPath = getResourceAsPath("/truststore-linkbrokers.jks");
		
		System.out.println("Truststore: " + trustPath);
		
		System.setProperty("javax.net.ssl.keyStore", trustPath);
		System.setProperty("javax.net.ssl.keyStorePassword", "123456");
		System.setProperty("javax.net.ssl.trustStore", trustPath);
		System.setProperty("javax.net.ssl.trustStorePassword", "123456");
		
		KeyStore ks;
		try {
			ks = KeyStore.getInstance(KeyStore.getDefaultType());
		} catch (KeyStoreException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			ks.load(fis, "123456".toCharArray());
		} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
			e.printStackTrace();
			return;
		}

		ProtectionParameter pp = new KeyStore.PasswordProtection("123456".toCharArray());
		
		KeyStore.TrustedCertificateEntry key;
		try {
			key = (KeyStore.TrustedCertificateEntry) ks.getEntry("softwarehouse", null);
		} catch (NoSuchAlgorithmException | UnrecoverableEntryException
				| KeyStoreException e) {
			e.printStackTrace();
			return;
		}
		
		
		try {
			LinkingRequest lnk = new LinkingRequest();
			lnk.add("ScienceLib", DeveloperLicense.fromFile(new File(getResourceAsPath("/developer1-0.lic"))));
			SoftwareHouseRequest req = new SoftwareHouseRequest(lnk, key.getTrustedCertificate().getPublicKey(), "AES");
			
			LinkingServer ls = new LinkingServer();
			
			
			
			byte[] jis = ls.performLink(req, streamToByte(TestLinking.class.getResourceAsStream("/sampleproject.jar")));
			
			
			File tempFile = File.createTempFile("result", ".jar");
			FileOutputStream fout = new FileOutputStream(tempFile);
			fout.write(jis);
			fout.close();
			System.out.println("Result written to " + tempFile.getAbsolutePath());
			
			System.out.println("Done");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}

}
