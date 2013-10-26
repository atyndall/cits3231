package sp.linkbrokers.linkingserver;

import java.util.ArrayList;
import java.util.HashMap;

import sp.common.ArgumentParser;
import sp.common.RunOptions;

public class LinkBrokerArgumentParser extends ArgumentParser{
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
			
			if(args[i].substring(0,1).equals("-")){
				String tokenInFocus = args[i].substring(1,args[i].length());
				
				String prefix;
				
				if( (prefix = allowed.getPrefix(tokenInFocus)) != null){
						argumentRecorder = new ArgumentRecorder(options);
					
					argumentRecorder.setParameter(tokenInFocus, prefix);
				} else {
					logErrorAndExit("Unrecognised argument: -" + tokenInFocus);
				}
			} else {
				argumentRecorder.parse(args[i]);
			}
			
		}
		
		for(String argumentName : allowed.getRequiredArguments()){
			if(options.get(argumentName) == null)
				logErrorAndExit(allowed.getPrefix(argumentName) + " undefined or missing");
		}
		
		return new RunOptions(options,libraries);
	}
		
	/**
	 * Displays the correct usage of the program on the command line
	 */
	protected void displayUsage(){
		String usageString = "LinkingServer ";
		
		for(String argumentName : allowedArguments().getRequiredArguments()){
			usageString += " -" + argumentName + " " + allowedArguments().getPrefix(argumentName).replaceAll("\\s", "");
		}
		
		display("LinkingServer - Server application for linking .jar files with remote libraries, request by Developers and delivered by SoftwareHouses."); 
		emptyLine();
		display("Usage:");
		emptyLine();
		display(usageString);
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
		allowedOptions.addRequired("port", "Port", "Port to send request to Link Broker from");
		allowedOptions.addOptional("ks", "Key Store", "Location of the keystore.");
		allowedOptions.addOptional("ksPassword", "Key Store Password", "Password for key store file; Assumed to be the same password to access all entries in the key store file.");
		allowedOptions.addOptional("ksType", "Key Store Type", "Filetype of key store; currently ignored and defualts to .jks");
		allowedOptions.addOptional("ksAlias", "Key Store Alias", "Alias in key store associated with developer's private key");
		allowedOptions.addOptional("encryption", "Encryption", "Type of symmetric encryption to use for linking requests");
		
		return allowedOptions;
	}
}
