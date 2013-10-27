package sp.linkbrokers.linktool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.security.SignedObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.jar.JarInputStream;

import javax.rmi.ssl.SslRMIClientSocketFactory;

import sp.linkbrokers.linkingserver.ILinkingServer;
import sp.linkbrokers.linktool.support.DeveloperCommandLineParser;
import sp.linkbrokers.linktool.support.LicenseFilter;
import sp.linkbrokers.linktool.support.RunOptions;
import sp.softwarehouse.protectedlibrary.DeveloperLicense;
import sp.common.ChecksumGenerator;
import sp.common.LinkingRequest;
import sp.common.Node;
import sp.common.SoftwareHouseRequest;

/**
 * Program to replace .class files in a .jar compiled archive with those supplied
 * by third party vendors called Software Houses, so they are preferentially
 * loaded when the main .class file is executed. A mediary entity, called Link 
 * Brokers negotiate the transfer and final compilation of the .jar file and
 * external libraries. 
 * 
 * The transfer is performed over SSL, using requests encrypted using an asymmetrical
 * algorithm. The Link Broker knows only the recipient Software House, not
 * the details of the request. 
 * 
 *  In order to link to a Software House's libraries, it must present a valid
 *  license.
 *  
 * @author Ash Tyndall, Aleck Greenham
 */
public class DeveloperLinkTool extends Node{
	static private HashMap<String, String> defaultOptions;
	
	static final int linkingPort = 54164;
	static final String rmiRegistryAddress = "localhost";
	static final String linkingServerClassName = "LinkingServer";
	
	private ILinkingServer linkingSvr;
	
	/**
	 * Contains the location of the .jar file to link external libraries to. This
	 * is a required command argument.
	 */
	String jarFilePath;

	/**
	 * Contains the list of encrypted Software House linking requests
	 */
	private HashMap<String, SoftwareHouseRequest> softwareHouseRequests;
	
	/**
	 * Creates a new developer instance, parses the command line arguments
	 * and sends a request for the linked libraries to a Link Broker
	 * @param args
	 */
	public static void main(String[] args) {
		RunOptions runOptions = new DeveloperCommandLineParser().parseArguments(args);
		DeveloperLinkTool developer = new DeveloperLinkTool(runOptions);

		developer.sendRequestForExternalLibraries();
	}
	
	/**
	 * Initiates a new developer instance
	 */
	public DeveloperLinkTool(RunOptions runOptions) {
		softwareHouseRequests = new HashMap<String, SoftwareHouseRequest>();
		
		setOptions(runOptions.getOptions());

		try {
			Registry reg = LocateRegistry.getRegistry(rmiRegistryAddress, linkingPort, new SslRMIClientSocketFactory());
			ILinkingServer linkingSvr = (ILinkingServer) reg.lookup(linkingServerClassName);		
		} catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
            return;
		}
		
		createRequestFrom(runOptions.getLibraries());
	}

	/**
	 * Encrypts and sends a request for the LinkBroker for the external libraries
	 */
	public void sendRequestForExternalLibraries() {
//		List<File> licsPath = createRequestFrom();// TODO: What is this??
//		
//		
//
//		// we check to see if the request was successful, if so, we know the licenses we submitted have been used
//		// thus we rename them
//		for (File lic : licsPath) {
//			File toName = new File(lic.getParentFile() + File.separator + lic.getName() + ".used");
//			
//			if (toName.exists()) throw new RuntimeException("File exists, can't rename");
//			lic.renameTo(toName);
//			
//		}
	}

	
	
	
	protected void log(String message){
		System.out.print(message);
	}
	
	protected void logError(String error){
		log("Error: " + error);
	}
	
	private List<File> createRequestFrom(HashMap<String, ArrayList<String>> libraries) {
		List<File> licsPath = new ArrayList<File>(); // we need to keep track of which licenses we use, because we must delete them afterwards
		
		for(String softwareHouse : libraries.keySet()){
			LinkingRequest linkingRequest = new LinkingRequest();
			
			int nlibs = libraries.get(softwareHouse).size();

			List<DeveloperLicense> lics = new ArrayList<DeveloperLicense>(nlibs);
			
			File f = new File(options.get("licDir"));
			File[] licenses = f.listFiles(new LicenseFilter());
			
			// We get as many licenses as we need from the license directory
			for (int i = 0; i < nlibs; i++) {
				try {
					lics.add(DeveloperLicense.fromStream(new FileInputStream(licenses[i].getAbsolutePath())));
					licsPath.add(licenses[i]);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return null;
				}
			}
			
			linkingRequest.addLibraries(libraries.get(softwareHouse), lics);
			
			SoftwareHouseRequest request = null;
			
			SignedObject signedChecksums = ChecksumGenerator.getSignedChecksums(getPrivateKey(),  new JarInputStream(new FileInputStream(jarFilePath)));
			
			try {
				request = new SoftwareHouseRequest(linkingRequest, signedChecksums, getCertificate(keyStoreAlias()),
						getPublicKey(softwareHouse), symmetricEncryption());
				
				request.sign(getPrivateKey());
				
			} catch (NoSuchAlgorithmException e) {
				logErrorAndExit("Unsupported encryption algorithm: " + symmetricEncryption());
			}
			
			softwareHouseRequests.put(softwareHouse, request);
			
		}
		
		return licsPath;
	}

	private void setOptions(HashMap<String, String> customOptions) {
		options = new HashMap<String,String>();
		options.putAll(getDefaultOptions());
		options.putAll(customOptions);
		setSystemOptions(customOptions);
	}
	
	static private void setSystemOptions(HashMap<String, String> customOptions) {
		for (Entry<String, String> e : customOptions.entrySet()) {
			switch (e.getKey()) {
			case "keyStoreFile":
				System.setProperty("javax.net.ssl.keyStore", e.getValue());
				System.setProperty("javax.net.ssl.trustStore", e.getValue());
				break;
				
			case "keyStorePassword":
				System.setProperty("javax.net.ssl.keyStorePassword", e.getValue());
				System.setProperty("javax.net.ssl.trustStorePassword", e.getValue());
				break;
			}
		}
	}

	static private HashMap<String, String> getDefaultOptions(){
		if(defaultOptions == null){
			defaultOptions = new HashMap<String, String>();
			setDefault("keyStoreFile", DEFAULT_KEYSTORE_LOCATION);
			setDefault("keyStorePassword", DEFAULT_KEYSTORE_PASSWORD);
			setDefault("keyStoreType", DEFAULT_KEYSTORE_TYPE);
			setDefault("keyStoreAlias", DEFAULT_KEYSTORE_ALIAS);
			setDefault("symmetricEncryptionType", DEFAULT_SYMETRICAL_ENCRYPTION);
			setSystemOptions(defaultOptions);
		}
		
		return defaultOptions;
	}

	static private void setDefault(String field, String value) {
		defaultOptions.put(field, value);
	}
	
}
