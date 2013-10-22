package sp.support;

import java.util.ArrayList;
import java.util.HashMap;

public class DeveloperCommandLineParser extends CommandLineParser{
	public class LibraryNameParser extends ArgumentParser{
		HashMap<String,ArrayList<String>> libraries;
		
		public LibraryNameParser(HashMap<String,ArrayList<String>> libraries) {
			super( null, null, null);
			
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
		
		argumentParser = new DirParser(options, "licDir", "Licensing directory");
		
		if(args.length > 0){
			
			for(int i=0; i<args.length; i++){
				switch(args[i]){
					case "-libs":
						argumentParser = new LibraryNameParser(libraries);
						break;
						
					case "-keyStore":
						argumentParser = new FileParser(options, "keyStoreFile", "Key Store", 
								new String[]{".jks"});
						break;
						
					case "-keyStorePassword": 
						argumentParser = new ArgumentParser(options, "keyStorePassword", "Key Store Password");
						break;
						
					case "-keyStoreType": 
						argumentParser = new ArgumentParser(options, "keyStoreType", "Key Store Type");
						break;
						
					case "-keyStoreAlias": 
						argumentParser = new ArgumentParser(options, "keyStoreAlias", "Key Store Alias");
						break;
						
					case "-symmetricEncryptionType": 
						argumentParser = new ArgumentParser(options, "symmetricEncryptionType", "Symmetric Encryption Type");
						break;
						
					default:
						if(args[i].substring(0, 1).equals("-"))
							logErrorAndExit("Unknown option '" + args[i] + "'");
						i--;
				}
				
				i++;
					
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
		
		if(libraries.isEmpty())
			logErrorAndExit(new LibraryNameParser(libraries).getError() + 
					"Undefined or missing.");
		
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
		display("Developer JarFilePath LicFilePath [options] -libs LibraryNames ");
		emptyLine();
		display("	JarFilepath		- Directory path to the .jar file to link to the remote libraries");	
		display("	LicFilepath		- Directory path to folder containing current licenses");	
		display("	Librarynames	- Space separated list of library names in the format SoftwareHouse:Library");	
		emptyLine();
		display("Options:");
		emptyLine();
		display("	-keyStore					Location of the keystore. Must be a .jks file");
		display("	-keyStorePassword			Password for key store file; Assumed to be the same password to access all entries in the key store file.");
		display("	-keyStoreType				Filetype of key store; currently ignored and defualts to .jks");
		display("	-keyStoreAlias				Alias in key store associated with developer's private key");
		display("	-symmetricEncryptionType 	Type of symmetric encryption to use for linking requests");
	}
}
