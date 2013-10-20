package sp.support;

import java.util.ArrayList;
import java.util.HashMap;

public class DeveloperCommandLineParser extends CommandLineParser{
	public class LibraryNameParser extends ArgumentParser{
		HashMap<String,ArrayList<String>> libraries;
		
		public LibraryNameParser(HashMap<String,ArrayList<String>> libraries) {
			this.libraries = libraries;
			errorPrefix = "Library Error";
		}

		public boolean parse(String parameter){
			recordParsedAtLeastOneArgument();
			
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
	
	/**
	 * Parses the command line arguments and hands the values off to different
	 * modules to verify they are of the correct format
	 * @param args The list of arguments provided on the command line
	 */
	public RunOptions parseArguments(String[] args){
		HashMap<String,String> options = new HashMap<String,String>();
		HashMap<String,ArrayList<String>> libraries = new HashMap<String,ArrayList<String>>();
		
		ArgumentParser argumentParser = new FileParser(options, "jarFile", "Jar File", 
				new String[]{".jar"});
		
		if(args.length > 0){
			
			for(int i=0; i<args.length; i++){
				if(args[i].equals("-libs")){
					argumentParser = new LibraryNameParser(libraries);
					i++;
				} else if (args[i].equals("-keystore")){
					argumentParser = new FileParser(options, "keyStoreFile", "KeyStore", 
							new String[]{".jks"});
					i++;
				}

				if(i >= args.length){ 
					logErrorAndExit(argumentParser.getError() + "Undefined or missing.");
				} else if(!argumentParser.parse(args[i])){
					logErrorAndExit(argumentParser.getError());
				}
			}
		}
		
		if(!argumentParser.hasParsedAnArgument()){
			displayUsage();
			System.exit(1);
		}
		
		
		return new RunOptions(options,libraries);
	}
	
	/**
	 * Displays the correct usage of the program on the command line
	 */
	protected void displayUsage(){
		display("Developer - Client application for linking .jar files with remote libraries."); 
		emptyLine();
		display("Usage:");
		emptyLine();
		display("Developer jar-file-path -keystore [store-location] -libs [library-names]");
		emptyLine();
		display("		jar-file-path	- Directory path to the .jar file to link to the remote libraries");
		display("		keystore		- Location of the keystore. Must be a .jks file");	
		display("		library-names	- Space separated list of library names in the format SoftwareHouse:Library");	
		
	}
}
