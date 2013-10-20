package sp.common;

public class LoggedItem {
	protected void displayUsage(){;
		display("This node's usage has not been defined yet");
	}
	
	protected void log(String message){
		System.out.print(message);
	}
	
	protected void display(String message){
		System.out.println(message);
	}
	
	protected void logError(String error){
		log("Error: " + error);
	}
	
	protected void logErrorAndExit(String error){
		displayUsage();
		emptyLine();
		logError(error);
		
		System.exit(1);
	}
	
	protected void emptyLine(){
		display("");
	}
}
