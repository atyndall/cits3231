package sp.softwarehouse.libprovidingserver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

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
		return mapShortToClass.get(className).getCanonicalName().replaceAll("\\.", "/") + ".class"; 
	}
	
	
	private byte[] getClassBytes(String className) throws IOException {
		InputStream is = LibProvidingServer.class.getResourceAsStream("/" + getPathName(className));
		
		if (is == null) {
			throw new FileNotFoundException();
		} else {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = is.read(bytes)) != -1) {
				bos.write(bytes, 0, read);
			}
			is.close();
			
			return bos.toByteArray();
		}
	}
	
	public Map<String, byte[]> getClassesToLink(SoftwareHouseRequest req)
			throws InvalidLicenseException, Exception {
		
		LinkingRequest lreq = req.getRequest(key.getPrivateKey(), "AES");

		List<String> libs = lreq.getLibraryList();
		List<DeveloperLicense> licenses = lreq.getLicenses();
		
		Map<String, byte[]> outm = new HashMap<String, byte[]>();
		
		for (int i = 0; i < libs.size(); i++) {
			if (!providedLibsShort.contains(libs.get(i))) throw new Exception("Non-existant lib");
			
			if (lm.validLicense(licenses.get(i))) {
				//lm.consumeLicense(licenses.get(i)); TODO add back
			} else {
				throw new Exception("Invalid licenses");
			}
			
			System.out.println("Got request for " + mapShortToClass.get(libs.get(i)));
			outm.put(getPathName(libs.get(i)), getClassBytes(libs.get(i)));
		}
		
		return outm;
	}
	
}
