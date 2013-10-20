package sp.developer;

import java.util.ArrayList;
import java.util.HashMap;

import sp.support.DeveloperCommandLineParser;
import sp.support.RunOptions;
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
public class Developer extends Node{
	static private HashMap<String, String> defaultOptions;
	
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
		Developer developer = new Developer(runOptions);
		developer.sendRequestForExternalLibraries();
	}
	
	/**
	 * Initiates a new developer instance
	 */
	public Developer(RunOptions runOptions) {
		softwareHouseRequests = new HashMap<String, SoftwareHouseRequest>();
		
		setOptions(runOptions.getOptions());
		createRequestFrom(runOptions.getLibraries());
	}

	/**
	 * Encrypts and sends a request for the LinkBroker for the external libraries
	 */
	public void sendRequestForExternalLibraries() {
		//TODO: Write code to send to Link Broker
	}

	private void createRequestFrom(HashMap<String, ArrayList<String>> libraries) {
		for(String softwareHouseName : libraries.keySet()){
			LinkingRequest linkingRequest = new LinkingRequest();
			linkingRequest.addLibraries(libraries.get(softwareHouseName));
			
			SoftwareHouseRequest request = new SoftwareHouseRequest(linkingRequest, 
					encryption(), getPublicKey(softwareHouseName), 
					symmetricEncryption());
			
			addSoftwareHouseRequest(softwareHouseName, request);
		}
	}

	/**
	 * Adds an encrypted request to the list of encrypted requests ready to be
	 * send to the Link Broker.
	 * @param softwareHouse Name of the Software House to add the encrypted request
	 * under
	 * @param encryptedRequest The encrypted request itself
	 */
	private void addSoftwareHouseRequest(String softwareHouse,
			SoftwareHouseRequest softwareHouseRequest) {
		softwareHouseRequests.put(softwareHouse, softwareHouseRequest);
	}

	private void setOptions(HashMap<String, String> customOptions) {
		options = new HashMap<String,String>();
		options.putAll(getDefaultOptions());
		options.putAll(customOptions);
	}

	static private HashMap<String, String> getDefaultOptions(){
		if(defaultOptions == null){
			defaultOptions = new HashMap<String, String>();
			setDefault("keyStoreFile", DEFAULT_KEYSTORE_LOCATION);
			setDefault("keyStorePassword", DEFAULT_KEYSTORE_PASSWORD);
			setDefault("keyStoreType", DEFAULT_KEYSTORE_TYPE);
			setDefault("keyStoreAlias", DEFAULT_KEYSTORE_ALIAS);
			setDefault("encryption", DEFAULT_ENCRYPTION);
			setDefault("symmetricEncryption", DEFAULT_SYMETRICAL_ENCRYPTION);
		}
		
		return defaultOptions;
	}

	static private void setDefault(String field, String value) {
		defaultOptions.put(field, value);
	}
	
}