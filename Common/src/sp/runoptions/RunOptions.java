package sp.runoptions;

import java.util.ArrayList;
import java.util.HashMap;

public class RunOptions {
	private HashMap<String,String> options;
	private HashMap<String,ArrayList<String>> libraries;
	
	public HashMap<String,String> getOptions(){
		return options;
	}
	
	public HashMap<String,ArrayList<String>> getLibraries(){
		return libraries;
	}
	
	public RunOptions(HashMap<String,String> options, 
			HashMap<String,ArrayList<String>> libraries){
		this.options = options;
		this.libraries = libraries;
	}

	public RunOptions subset(String[] parameters) {
		HashMap<String,String> subset = new HashMap<String,String>();
		
		for(String parameter : parameters){
			subset.put(parameter, options.get(parameter));
		}
		
		return new RunOptions(subset,null);
	}

	public int getOption(String paramater) {
		if(options == null)
			return -1;
		
		return Integer.parseInt(options.get(paramater));
	}
}
