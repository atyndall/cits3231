package sp.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public abstract class ArgumentParser extends LoggedItem {
	protected class ArgumentProperties{
		private String description;
		private String errorPrefix;
		private boolean required;
		
		public String getDescription() { return description; }
		public String getErrorPrefix() { return errorPrefix; }
		public boolean isRequired() { return required; }
		
		public ArgumentProperties(String errorPrefix, String description, boolean required){
			this.description = description;
			this.errorPrefix = errorPrefix;
			this.required = required;
			
		}
	}
	
	protected class AllowedArguments{
		HashMap<String, ArgumentProperties> argumentList;
		ArrayList<String> requiredArguments;
		
		public AllowedArguments(){
			argumentList = new HashMap<String, ArgumentProperties>();
			requiredArguments = new ArrayList<String>();
		}
		
		public Set<String> getArgumentNames() { return argumentList.keySet(); }
		
		public ArrayList<String> getRequiredArguments(){ return requiredArguments; }

		public String getPrefix(String argument){
			ArgumentProperties argumentProperties = argumentList.get(argument);
			String prefix = null;
					
			if(argumentProperties != null)
				prefix = argumentProperties.getErrorPrefix();
			
			return prefix;
		}
		
		public String getUsage(String argument) {
			ArgumentProperties argumentProperties = argumentList.get(argument);
			
			if(argument == null)
				throw new IllegalArgumentException("Not an allowed argument: " + argument);
			
			return "	-" + argument + (argument.length() > 6 ? "": "	") + "	" + argumentProperties.getDescription(); 
		}

		public boolean isAllowed(String argumentName) {
			return getArgumentNames().contains(argumentName);
		}

		public void addOptional(String argument, String errorPrefix, String description){
			add(argument,errorPrefix,description,false);
		}

		public void addRequired(String argument, String errorPrefix, String description){
			add(argument,errorPrefix,description,true);
		}

		private void add(String argument, String errorPrefix, String description, boolean required){
			ArgumentProperties argumentProperties = new ArgumentProperties(errorPrefix, description, required);
			argumentList.put(argument, argumentProperties);
			
			if(required)
				requiredArguments.add(argument);
		}
	}
	
	public class ArgumentRecorder{
		private HashMap<String,String>	options;
		protected String error = "";
		protected String errorPrefix;
		private String parameterName;
		
		public boolean parse(String parameter){
			recordParameter(parameter);
			return true;
		}
		
		public ArgumentRecorder(HashMap<String,String> options){
			this.options = options;
		}
		
		public void setParameter(String parameterName, String errorPrefix){
			this.errorPrefix = errorPrefix;
			this.parameterName = parameterName;
		}
		
		protected void recordParameter(String parameter){
			if(parameterName == null || errorPrefix == null)
				throw new IllegalStateException("Parameter hasn't been set");
			
			options.put(parameterName, parameter);
		}
		
		public String getError() {
			return errorPrefix + ": " + error;
		}
	}
	
	protected abstract void displayUsage();
	
	protected void log(String message){
		System.out.print(message);
	}
	
	protected void logError(String error){
		log("Error: " + error);
	}
	
	@Override
	protected void logErrorAndExit(String error){
		displayUsage();
		emptyLine();
		logError(error);
		
		System.exit(1);
	}
}
