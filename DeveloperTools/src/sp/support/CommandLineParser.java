package sp.support;

import java.util.HashMap;

import sp.common.LoggedItem;

public abstract class CommandLineParser extends LoggedItem {
	protected abstract void displayUsage();
	
	@Override
	protected void logErrorAndExit(String error){
		displayUsage();
		emptyLine();
		logError(error);
		
		System.exit(1);
	}
	
	public class ArgumentParser{
		private HashMap<String,String>	options;
		String error = "";
		String errorPrefix;
		boolean parsedAnArgument = false;
		private String parameterName;
		
		public boolean parse(String parameter){
			recordParameter(parameter);
			return true;
		}
		
		public ArgumentParser(HashMap<String,String> options, String parameterName, 
				String errorPrefix){
			this.options = options;
			this.errorPrefix = errorPrefix;
			this.parameterName = parameterName;
		}
		
		public void recordParsedAtLeastOneArgument(){
			parsedAnArgument = true;
		}
		
		protected void recordParameter(String parameter){
			options.put(parameterName, parameter);
		}
		
		public boolean hasParsedAnArgument(){
			return parsedAnArgument;
		}
		
		public String getError() {
			return errorPrefix + ": " + error;
		}
	}
	
	public class FileParser extends ArgumentParser {
		private String[] acceptableFileTypes;

		@Override
		public boolean parse(String parameter) {	
			recordParsedAtLeastOneArgument();
			
			if(isAcceptableFileType(parameter)){
				recordParameter(parameter);
				return true;
			} else {
				error = "Invalid filetype for '" + parameter + "'. Must be " + acceptableFileTypesAsString();
				return false;
			}
			
		}
		
		public FileParser(HashMap<String,String> options, String parameterName, 
				String errorPrefix, String[] acceptableFiletypes){
			
			super(options, parameterName, errorPrefix);
			
			this.acceptableFileTypes = acceptableFiletypes;
		}
		
		private boolean isAcceptableFileType(String filetype){
			for(String acceptableType: acceptableFileTypes){
				
				if(filetype.matches(".+" + acceptableType)){
					return true;
				}
			}
			
			return false;
		} 
		
		private String acceptableFileTypesAsString(){
			String list = "";
			
			for(String filetype: acceptableFileTypes){
				list += (list.length() == 0 ? "" : ", ") + filetype;
			}
			
			return list;
		}
	}
}
