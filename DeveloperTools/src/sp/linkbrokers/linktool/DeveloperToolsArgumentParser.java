package sp.linkbrokers.linktool;

import java.util.ArrayList;
import java.util.HashMap;

import sp.common.ArgumentParser;
import sp.common.RunOptions;

public class DeveloperToolsArgumentParser extends ArgumentParser{
	public class LibraryNameRecorder extends ArgumentRecorder{
		HashMap<String,ArrayList<String>> libraries;
		
		public LibraryNameRecorder(HashMap<String,ArrayList<String>> libraries) {
			super(null);
			
			this.libraries = libraries;
			errorPrefix = "Library Error";
		}

		public boolean parse(String parameter){
			String[] libraryDetails = parameter.split(":");
			
			if(libraryDetails.length == 2){
				addLibraryDetailsToRequestList(libraryDetails);
				return true;
			} else {
				error = "Invalid format: '" + parameter + "'"; 
				return false;
			}
		}
		
		private void addLibraryDetailsToRequestList(String[] libraryDetails) {
			final String softwareHouseName = libraryDetails[0];
			final String libraryName = libraryDetails[0];

			ArrayList<String> softwareHouseRequest = libraries.get(softwareHouseName);
			
			if(softwareHouseRequest == null){
				libraries.put(softwareHouseName, new ArrayList<String>());
				softwareHouseRequest = libraries.get(softwareHouseName); 
			} 
			
			softwareHouseRequest.add(libraryName);
		}
	}

	private AllowedArguments allowedOptions;
	
	/**
	 * Parses the command line arguments and hands the values off to different
	 * modules to verify they are of the correct format
	 * @param args The list of arguments provided on the command line
	 */
	public RunOptions parseArguments(String[] args){
		HashMap<String,String> options = new HashMap<String,String>();
		HashMap<String,ArrayList<String>> libraries = new HashMap<String,ArrayList<String>>();
		
		AllowedArguments allowed = allowedArguments();
		
		ArgumentRecorder argumentRecorder = new ArgumentRecorder(options);
		argumentRecorder.setParameter("jarFile", allowed.getPrefix("jarFile"));
		
		for(int i=0; i<args.length; i++){
			if(args[i].length() < 1)
				continue;
			
			if(args[i].substring(0,1).equals("-")){
				String tokenInFocus = args[i].substring(1,args[i].length());
				
				if(tokenInFocus.equals("libs")){
						argumentRecorder = new LibraryNameRecorder(libraries);
				} else {		
					String prefix;
					
					if( (prefix = allowed.getPrefix(tokenInFocus)) != null){
						if(argumentRecorder instanceof LibraryNameRecorder)	
							argumentRecorder = new ArgumentRecorder(options);
						
						argumentRecorder.setParameter(tokenInFocus, prefix);
					} else {
						logErrorAndExit("Unrecognised argument: -" + tokenInFocus);
					}
				}
			} else {
				argumentRecorder.parse(args[i]);
				
				if(argumentRecorder instanceof LibraryNameRecorder)
					options.put("libs", "");
			}
			
		}
		
		for(String argumentName : allowed.getRequiredArguments()){
			if(options.get(argumentName) == null)
				logErrorAndExit(allowed.getPrefix(argumentName) + " undefined or missing");
		}
		
		if(libraries.isEmpty())
			logErrorAndExit(new LibraryNameRecorder(libraries).getError() + 
					"Undefined or missing.");
		
		return new RunOptions(options,libraries);
	}
		
	/**
	 * Displays the correct usage of the program on the command line
	 */
	protected void displayUsage(){
		String usageString = "DeveloperTools";
		
		for(String argumentName : allowedArguments().getRequiredArguments()){
			usageString += " -" + argumentName + " " + allowedArguments().getPrefix(argumentName).replaceAll("\\s", "");
		}
		
		display("Developer - Client application for linking .jar files with remote libraries."); 
		emptyLine();
		display("Usage:");
		emptyLine();
		display(usageString);
		emptyLine();
		display("	LicFilepath		- Directory path to folder containing current licenses");	
		emptyLine();
		display("Options:");
		emptyLine();
		
		for(String option: allowedArguments().getArgumentNames()){
			display(allowedArguments().getUsage(option));
		}
	}
	
	private AllowedArguments allowedArguments(){
		if(allowedOptions != null)
			return allowedOptions;
		
		allowedOptions = new AllowedArguments();
		allowedOptions.addRequired("jar", "Jar File", "Directory path to the .jar file to link to the remote libraries");
		allowedOptions.addRequired("libs", "Libraries", "Space separated list of libraries to link with in the format SoftwareHouse:LibraryName");
		allowedOptions.addRequired("port", "Port", "Port to send request to Link Broker from");
		allowedOptions.addRequired("lbaddress", "Link Broker Address", "Address Link Broker can be reached on");
		allowedOptions.addRequired("lbport", "Link Broker Port", "Port number Link Broker can be reached on");
		allowedOptions.addOptional("ks", "Key Store", "Location of the keystore.");
		allowedOptions.addOptional("ksPassword", "Key Store Password", "Password for key store file; Assumed to be the same password to access all entries in the key store file.");
		allowedOptions.addOptional("ksType", "Key Store Type", "Filetype of key store; currently ignored and defualts to .jks");
		allowedOptions.addOptional("ksAlias", "Key Store Alias", "Alias in key store associated with developer's private key");
		allowedOptions.addOptional("encryption", "Encryption", "Type of symmetric encryption to use for linking requests");
		
		return allowedOptions;
	}
}
