package sp.softwarehouse.libprovidingserver;

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
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyStore;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import sp.common.ChecksumGenerator;
import sp.common.LinkingRequest;
import sp.common.SoftwareHouseRequest;
import sp.softwarehouse.protectedlibrary.DeveloperLicense;
import sp.softwarehouse.protectedlibrary.Exceptions.InvalidLicenseException;
import sp.softwarehouse.protectedlibrary.LicenseManager;
import sp.softwarehouse.protectedlibrary.ProtectedLibrary;
import sp.tests.LicensingTest;

public class LibProvidingServer extends UnicastRemoteObject implements ILibProvidingServer {
	
	private static final long serialVersionUID = 9214577972245950200L;
	public static final int libProvidingPort = 54165;
	
	private static final List<String> providedLibsShort = Arrays.asList(
			"ScienceLib", 
			"EnterpriseLib"
	);
	
	private static final List<Class<? extends ProtectedLibrary>> providedLibClasses = Arrays.asList(
			sp.softwarehouse.protectedlibrary.sciencelib.RealScienceLib.class,
			sp.softwarehouse.protectedlibrary.enterpriselib.RealEnterpriseLib.class
	);
			
	
	private KeyStore.PrivateKeyEntry key;
	private LicenseManager lm;
	private Map<String, Class<? extends ProtectedLibrary>> mapShortToClass;
	
	protected LibProvidingServer() throws RemoteException {
		super(libProvidingPort, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory(null, null, true));
		
		mapShortToClass = new HashMap<String, Class<? extends ProtectedLibrary>>();
		
		for (int i = 0; i < providedLibsShort.size(); i++) {
			mapShortToClass.put(providedLibsShort.get(i), providedLibClasses.get(i));
		}
		
		KeyStore ks;
		try {
			ks = KeyStore.getInstance(KeyStore.getDefaultType());
		} catch (KeyStoreException e) {
			e.printStackTrace();
			return;
		}
		
		InputStream fis;
		try {
			fis = new FileInputStream(System.getProperty("javax.net.ssl.keyStore"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		};
		
		try {
			ks.load(fis, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());
		} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
			e.printStackTrace();
			return;
		}

		ProtectionParameter pp = new KeyStore.PasswordProtection(System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());
		
		try {
			this.key = (KeyStore.PrivateKeyEntry) ks.getEntry("softwarehouse", pp);
		} catch (NoSuchAlgorithmException | UnrecoverableEntryException
				| KeyStoreException e) {
			e.printStackTrace();
			return;
		}
		
		URL url = LicensingTest.class.getResource("/lm.db");
		File lmdb = new File(url.getFile());
		this.lm = new LicenseManager(lmdb, key.getPrivateKey());
	}

	public static void main(String args[]) {
		URL trustDir = LibProvidingServer.class.getResource("/truststore-softwarehouse.jks");
		
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
		
	    try {
			Registry reg = LocateRegistry.createRegistry(libProvidingPort,
							new SslRMIClientSocketFactory(),
							new SslRMIServerSocketFactory(null, null, true));
			
			LibProvidingServer obj = new LibProvidingServer();
			
			String addr = "LibProvidingServer";
			reg.bind(addr, obj);
			
			System.out.println("The RMI registry is currently running on port " + libProvidingPort);
			System.out.println("Clients can bind to the lib providing server with '"+addr+"'");
		
        } catch (Exception e) {
        	System.out.println("LibProvidingServer err: " + e.getMessage());
            e.printStackTrace();
        }
	}
	
	private String getPathName(String className) {
		return mapShortToClass.get(className).getCanonicalName().replaceAll("\\.", "/") + ".java"; 
	}
	
	
	private byte[] getClassBytes(String className, Map<String, byte[]> checksums) throws IOException {
		StringBuilder spaths = new StringBuilder();
		StringBuilder schecksums = new StringBuilder();
		
		for (Entry<String, String> e : ChecksumGenerator.checksumsToEnc(checksums).entrySet()) {
			spaths.append("\"" + e.getKey() + "\",");
			schecksums.append("\"" + e.getValue() + "\",");
		}
		
		// remove last ","
		spaths.deleteCharAt(spaths.length() - 1);
		schecksums.deleteCharAt(schecksums.length() - 1);
		
		
		InputStream is = LibProvidingServer.class.getResourceAsStream("/" + getPathName(className));
		
		if (is == null) throw new FileNotFoundException();
		
		ByteArrayOutputStream outb = new ByteArrayOutputStream();
		
		byte[] buffer = new byte[1024];
		for (;;) {
			int rsz = is.read(buffer, 0, buffer.length);
	        if (rsz < 0)
	          break;
	        outb.write(buffer, 0, rsz);
		}
		
		String src = new String(outb.toByteArray(), "UTF-8");
		src = src.replaceFirst("\"#PATHS#\"", spaths.toString());
		src = src.replaceFirst("\"#SUMS#\"", schecksums.toString());
		
		File tmpJava = File.createTempFile(className + "-custom", ".java");
		System.out.println(tmpJava.getAbsolutePath());
		FileOutputStream fos = new FileOutputStream(tmpJava.getAbsolutePath());
		fos.write(src.getBytes("UTF-8"));
		fos.close();
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		compiler.run(null, null, null, tmpJava.getPath());
		
		FileInputStream compis = new FileInputStream(tmpJava.getParent() + File.separator + tmpJava.getName().replace("\\.java", ".class"));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int read = 0;
		byte[] bytes = new byte[1024];
		while ((read = compis.read(bytes)) != -1) {
			bos.write(bytes, 0, read);
		}
		compis.close();
		
		return bos.toByteArray();
	}
	
	public Map<String, byte[]> getClassesToLink(SoftwareHouseRequest req)
			throws InvalidLicenseException, Exception {
		
		LinkingRequest lreq = req.getRequest(key.getPrivateKey(), "AES");

		List<String> libs = lreq.getLibraryList();
		List<DeveloperLicense> licenses = lreq.getLicenses();
		
		Map<String, byte[]> outm = new HashMap<String, byte[]>();
		
		// TODO: We need to pass the developer's certificate and public key in the request
		// then we just verify that their cert is signed by SoftwareHouse
		// right now this will fail because we can't access ths information
		Certificate developerCert = req.getCertificate();
		String softwareHouseName = "softwarehouse"; // TODO: this will need to be customised based on actual name of the software house
		PublicKey softWareHouseKey = null; // TODO we must get the softwarehouse public key somehow
		
		try {
			developerCert.verify(softWareHouseKey);
		} catch (SignatureException e) {
			// panic
		}
		
		PublicKey developerKey = developerCert.getPublicKey();

		for (int i = 0; i < libs.size(); i++) {
			if (!providedLibsShort.contains(libs.get(i))) throw new Exception("Non-existant lib");
			
			if (lm.validLicense(licenses.get(i))) {
				//lm.consumeLicense(licenses.get(i)); TODO add back
			} else {
				throw new Exception("Invalid licenses");
			}
			
			System.out.println("Got request for " + mapShortToClass.get(libs.get(i)));
			outm.put(getPathName(libs.get(i)), getClassBytes(libs.get(i), req.getChecksums(developerKey)));
		}
		
		return outm;
	}
	
}
