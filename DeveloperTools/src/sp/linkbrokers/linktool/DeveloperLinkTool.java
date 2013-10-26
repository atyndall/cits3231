package sp.linkbrokers.linktool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.rmi.ssl.SslRMIClientSocketFactory;

import sp.linkbrokers.linkingserver.ILinkingServer;
import sp.linkbrokers.linktool.support.DeveloperCommandLineParser;
import sp.linkbrokers.linktool.support.LicenseFilter;
import sp.linkbrokers.linktool.support.RunOptions;
import sp.softwarehouse.protectedlibrary.DeveloperLicense;
import sp.common.LinkingRequest;
import sp.common.Node;
import sp.common.SoftwareHouseRequest;
import sp.exceptions.InvalidDeveloperLicenseFileException;
import sp.exceptions.RunOptionException;

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
	
	static final int DEFAULT_LINKING_PORT = 54164;
	static final String DEFAULT_RMI_LINKING_ADDRESS = "localhost";
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
		DeveloperLinkTool developer = null;
		
		try {
			developer = new DeveloperLinkTool(runOptions);
		} catch (RunOptionException e) {
			e.printStackTrace();
		}
	
		developer.sendRequestForExternalLibraries();
	}

	/**
	 * Initiates a new developer instance
	 */
	public DeveloperLinkTool(RunOptions runOptions) throws RunOptionException {
		softwareHouseRequests = new HashMap<String, SoftwareHouseRequest>();
		
		setOptions(runOptions.getOptions());
		createRequestFrom(runOptions.getLibraries());
//
//		try {
//			Registry reg = LocateRegistry.getRegistry(linkBrokerAddress(), linkBrokerPort(), new SslRMIClientSocketFactory());
////			ILinkingServer linkingSvr = (ILinkingServer) reg.lookup(linkingServerClassName);		
//		} catch (RemoteException | NotBoundException e) {
//            e.printStackTrace();
//            return;
//		}
		
		
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
	
	private int linkBrokerPort(){
		return Integer.parseInt(options.get("lbport"));
	}
	
	private String linkBrokerAddress(){
		return options.get("lbaddress");
	}
	
	private List<File> createRequestFrom(HashMap<String, ArrayList<String>> libraries) {
		/**
		 * Contains the licenses used so that they can be deleted later
		 */
		List<File> licensesToUse = new ArrayList<File>();
		
		for(String softwareHouse : libraries.keySet()){
			LinkingRequest linkingRequest = new LinkingRequest();
			int numberOfLibraries = libraries.get(softwareHouse).size();
			List<DeveloperLicense> licences = new ArrayList<DeveloperLicense>(numberOfLibraries);
			
			File licenseDir = new File(options.get("licDir"));
			File[] licenseFiles = licenseDir.listFiles(new LicenseFilter());
			
			// We get as many licenses as we need from the license directory
			for (int i = 0; i < numberOfLibraries; i++) {
				File licenseFileInFocus = licenseFiles[i];
				
				try {
					licences.add(DeveloperLicense.createLicense(licenseFileInFocus));
					licensesToUse.add(licenseFileInFocus);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (InvalidDeveloperLicenseFileException e) {
					logErrorAndExit(e.getMessage());
				} catch (IOException e) {
					logErrorAndExit("Unable to access license file '"+licenseFileInFocus.getName()+"'");
				}
			}
			
			linkingRequest.addLibraries(libraries.get(softwareHouse), licences);
			
			SoftwareHouseRequest request = null;
			
			try {
				request = new SoftwareHouseRequest(linkingRequest, 
						getPublicKey(softwareHouse), getSymmetricEncryption());
				
				request.sign(getPrivateKey());
				
			} catch (NoSuchAlgorithmException e) {
				logErrorAndExit("Unsupported encryption algorithm: " + getSymmetricEncryption());
			}
			
			softwareHouseRequests.put(softwareHouse, request);
			
		}
		
		return licensesToUse;
	}

	private void setOptions(HashMap<String, String> customOptions) throws RunOptionException{
		options = new HashMap<String,String>();
		options.putAll(getDefaultOptions());
		options.putAll(customOptions);
		setSystemOptions(customOptions);
		
		if(getPort() == -1)
			throw new RunOptionException("Port was not defined");
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
			setDefault("lbaddress", DEFAULT_RMI_LINKING_ADDRESS);
			setDefault("lbport", Integer.toString(DEFAULT_LINKING_PORT));
			setSystemOptions(defaultOptions);
		}
		
		return defaultOptions;
	}

	static private void setDefault(String field, String value) {
		defaultOptions.put(field, value);
	}

	
}
