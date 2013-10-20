package sp.common;

public abstract class LoggedItem {
	
	protected abstract void logError(String error);
	protected abstract void log(String message);
	
	protected void display(String message){
		System.out.println(message);
	}
	
	protected void logErrorAndExit(String error){
		emptyLine();
		logError(error);
		
		System.exit(1);
	}
	
	protected void emptyLine(){
		display("");
	}
}
