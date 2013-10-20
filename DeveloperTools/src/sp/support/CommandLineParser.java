package sp.support;

import java.util.HashMap;

import sp.common.LoggedItem;

public abstract class CommandLineParser extends LoggedItem {
	protected abstract void displayUsage();
	
	public abstract class ArgumentParser{
		String error = "";
		String errorPrefix;
		boolean parsedAnArgument = false;
		
		public abstract boolean parse(String paramter);
		
		public void recordParsedAtLeastOneArgument(){
			parsedAnArgument = true;
		}
		
		public boolean hasParsedAnArgument(){
			return parsedAnArgument;
		}
		
		public String getError() {
			return errorPrefix + ": " + error;
		}
	}
	
	public class FileParser extends ArgumentParser {
		private HashMap<String,String>	options;
		private String parameterName;
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
		
		protected void recordParameter(String parameter){
			options.put(parameterName, parameter);
		}
		
		public FileParser(HashMap<String,String> options, String parameterName, 
				String errorPrefix, String[] acceptableFiletypes){
			this.options = options;
			this.errorPrefix = errorPrefix;
			this.parameterName = parameterName;
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
