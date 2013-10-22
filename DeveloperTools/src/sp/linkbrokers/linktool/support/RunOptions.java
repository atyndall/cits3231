package sp.linkbrokers.linktool.support;

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
}
