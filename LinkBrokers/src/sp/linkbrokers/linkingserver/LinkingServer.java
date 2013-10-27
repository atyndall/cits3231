package sp.linkbrokers.linkingserver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import javax.security.cert.Certificate;


import sp.common.ChecksumGenerator;
import sp.requests.SoftwareHouseRequest;
import sp.runoptions.RunOptions;
import sp.softwarehouse.libprovidingserver.ILibProvidingServer;

public class LinkingServer extends UnicastRemoteObject implements
		RemoteLinkingServer {

	private static final long serialVersionUID = -1076731227201413447L;
	public static final int port = 54164;
	private HashMap<String, String> options;
	private HashMap<String, String> defaultOptions;
	private ILibProvidingServer libProvidingServer;
	private static final String DEFAULT_LINKING_PORT = "54165";
	private static final String DEFAULT_REGISTRY_PORT = "1099";


	public LinkingServer(RunOptions runOptions) throws RemoteException {
		super(runOptions.getOption("port"), new SslRMIClientSocketFactory(),
				new SslRMIServerSocketFactory(null, null, true));
		
		Registry rmiRegistry = null;
		
		this.options = setOptions(runOptions.getOptions());
		
		try{
			rmiRegistry = LocateRegistry.getRegistry("localhost",
					getRegistryPort(), new SslRMIClientSocketFactory());
		} catch (RemoteException e){
			if(e.getCause() instanceof BindException){
				logError("Port selected for registry is already binded " +
						"to another process: " + getRegistryPort());
			}
		}

		try {
			this.libProvidingServer = (ILibProvidingServer) rmiRegistry
					.lookup("LibProvidingServer");
		} catch (NotBoundException e) {
			e.printStackTrace();
			return;
		}
	}

	public static void main(String args[]) {
		RunOptions runOptions = new LinkingServerArgumentParser().parseArguments(args);
		
		initialiseKeyStore();

		LinkingServer linkingServer = null;
		try {
			linkingServer = new LinkingServer(runOptions);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		
		log("Using trust store: " + linkingServer.getKeyStore());
		
		try {
			Registry linkingReg = LocateRegistry.createRegistry(linkingServer.getRegistryPort(),
					new SslRMIClientSocketFactory(),
					new SslRMIServerSocketFactory(null, null, true));


			String addr = "LinkingServer";
			linkingReg.bind(addr, linkingServer);

			System.out.println("The RMI registry is currently running on port "
					+ linkingServer.getPort());
			System.out.println("Clients can bind to the linking server with '"
					+ addr + "'");
		} catch (Exception e) {
			System.out.println("LinkingServer err: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// http://stackoverflow.com/questions/13575795/programatically-add-class-to-existing-jar-file
	// http://stackoverflow.com/questions/3048669/how-can-i-add-entries-to-an-existing-zip-file-in-java
	public static void addFilesToExistingJar(File jarFile,
			Map<String, File> filesToAdd) throws IOException {
	
		File tempFile = LinkingServer.createTempFileFromJar(jarFile);
	
		ZipInputStream jarIn = new ZipInputStream(new FileInputStream(tempFile));
		ZipOutputStream jarOut = new ZipOutputStream(new FileOutputStream(
				jarFile));
	
		ZipEntry fileAlreadyInJar = null;
	
		while ((fileAlreadyInJar = jarIn.getNextEntry()) != null){
			String entryName = fileAlreadyInJar.getName();
	
			if (!LinkingServer.isFileNameInList(entryName, filesToAdd)) {
				LinkingServer.replaceFileInJar(entryName, jarIn, jarOut);
			}
	
		}
	
		jarIn.close();
	
		final int jarBufferSize = 1024;
		byte[] newJarFileBuffer = new byte[jarBufferSize];
	
		for (Entry<String, File> fileToAddToJar : filesToAdd.entrySet()) {
			InputStream in = new FileInputStream(fileToAddToJar.getValue());
	
			// Add ZIP entry to output stream.
			jarOut.putNextEntry(new ZipEntry(fileToAddToJar.getKey()));
			// Transfer bytes from the file to the ZIP file
	
			int len;
			while ((len = in.read(newJarFileBuffer)) > 0) {
				jarOut.write(newJarFileBuffer, 0, len);
			}
	
			// Complete the entry
			jarOut.closeEntry();
			in.close();
		}
	
		jarOut.close();
		tempFile.delete();
	}

	static void replaceFileInJar(String entryName,
			ZipInputStream zipIn, ZipOutputStream zipOut) throws IOException {
	
		final int jarBufferSize = 1024;
		byte[] newJarFileBuffer = new byte[jarBufferSize];
	
		zipOut.putNextEntry(new ZipEntry(entryName));
	
		int numberOfNewZipBytesReadIn;
	
		while ((numberOfNewZipBytesReadIn = zipIn.read(newJarFileBuffer)) > 0) {
			zipOut.write(newJarFileBuffer, 0, numberOfNewZipBytesReadIn);
		}
	}

	static File createTempFileFromJar(File jarFile) throws IOException {
		/**
		 * Create a temporary file, delete it so existing jar file can be
		 * renamed to the current temporary file
		 */
		File tempFile = File.createTempFile("link", ".jar");
		tempFile.delete();
		
		if ( !(jarFile).renameTo(tempFile) ) 
			throw new RuntimeException("Could not rename the file "
					+ jarFile.getAbsolutePath() + " to "
					+ tempFile.getAbsolutePath());
		
		return tempFile;
	}

	static boolean isFileNameInList(String entryName,
			Map<String, File> files) {
		for (Entry<String, File> fileEntry : files.entrySet()) {
			if (fileEntry.getKey().equals(entryName))
				return true;
		}
	
		return false;
	}

	private static void log(String message) {
		System.out.println(message);
	}

	private static void logError(String message) {
		log("Error: " + message);
	}

	private static void initialiseKeyStore() {
		URL trustDir = LinkingServer.class
				.getResource("/truststore-linkbrokers.jks");

		System.setProperty("javax.net.ssl.keyStore", trustDir.getPath());
		System.setProperty("javax.net.ssl.keyStorePassword", "123456");
		System.setProperty("javax.net.ssl.trustStore", trustDir.getPath());
		System.setProperty("javax.net.ssl.trustStorePassword", "123456");
	}

	private int getRegistryPort() {
		return Integer.parseInt(options.get("regport"));
	}

	private String getKeyStore() {
		return options.get("ks");
	}

	public int getPort() {
		return Integer.parseInt(options.get("port"));
	}

	public String getAddress() throws UnknownHostException {
		return InetAddress.getLocalHost().toString();
	
	}

	@Override
	public byte[] performLink(SoftwareHouseRequest[] softwareHouseRequests,
			byte[] inJar) throws RemoteException {
		return null;
	}

	public byte[] performLink(SoftwareHouseRequest req, byte[] inJarByte) {
		// we read the jar file to a location on the disk
		try {
			File outJarF = File.createTempFile("link", ".jar");
			FileOutputStream outJar = new FileOutputStream(outJarF);
			outJar.write(inJarByte);
			outJar.close();
			
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
			
			// verify checksums
			Map<String, byte[]> providedChecksums = req.getChecksums(developerKey);
			Map<String, byte[]> generatedChecksums = ChecksumGenerator.getChecksums(new JarInputStream(new FileInputStream(outJarF.getAbsolutePath())));
			
			if (!ChecksumGenerator.verifyByteMapsEqual(providedChecksums, generatedChecksums)) {
				// TODO: panic
			}
			
			
			Map<String, byte[]> clss = libProvidingServer.getClassesToLink(req);
			Map<String, File> oclss = new HashMap<String, File>();
			
			// convert inputstreams to tmp files
			for(Entry<String, byte[]> e : clss.entrySet()) {
				File tmp = File.createTempFile("link", ".class");
				FileOutputStream fos = new FileOutputStream(tmp);
				fos.write(e.getValue());				
				oclss.put(e.getKey(), tmp);
			}
			
			addFilesToExistingJar(outJarF, oclss);
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			FileInputStream outJarIn = new FileInputStream(outJarF);
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = outJarIn.read(bytes)) != -1) {
				bos.write(bytes, 0, read);
			}
			outJarIn.close();
			
			return bos.toByteArray();			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	private HashMap<String, String> getDefaultOptions() {
		if(defaultOptions == null){
			defaultOptions = new HashMap<String, String>();
			defaultOptions.put("port", DEFAULT_LINKING_PORT  );
			defaultOptions.put("regport", DEFAULT_REGISTRY_PORT  );
		}
		
		return defaultOptions;
	}

	private HashMap<String,String> setOptions(HashMap<String, String> customOptions){
		HashMap<String,String> options = new HashMap<String,String>();
		options.putAll(getDefaultOptions());
		options.putAll(customOptions);
		return options;
	}

}
