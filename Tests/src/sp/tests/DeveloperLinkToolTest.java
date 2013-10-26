package sp.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import sp.exceptions.RunOptionException;
import sp.linkbrokers.linktool.DeveloperLinkTool;
import sp.linkbrokers.linktool.support.RunOptions;

public class DeveloperLinkToolTest {
	private static final int DEVELOPER_PORT = 1234; 
	private static HashMap<String, String> argumentList;
	private HashMap<String, ArrayList<String>> libraries;
	private RunOptions emptyRunOptions;
	
	@Before
	public void setUp(){
		argumentList = new HashMap<String, String>();
		libraries = new HashMap<String, ArrayList<String>>();
		emptyRunOptions = new RunOptions(argumentList, libraries);
	}
	
	@Test
	public void shouldThrowExceptionIfPortNotDefined(){
		try{
			new DeveloperLinkTool(emptyRunOptions);
		} catch(RunOptionException e){
			assertEquals("Port was not defined", e.getMessage());
		}
	}
	
	@Test
	public void shouldCorrectlySetPort() {
		HashMap<String,String> options = new HashMap<String,String>();
		options.put("port", String.valueOf(DEVELOPER_PORT));
		HashMap<String,ArrayList<String>> libraries = new HashMap<String,ArrayList<String>>();
		
		RunOptions runOptions = new RunOptions(options, libraries);
		
		DeveloperLinkTool developer = null;
		
		try{
			developer = new DeveloperLinkTool(runOptions);
		} catch(RunOptionException e){
			System.out.println(e.getMessage());
		}
		
		assertEquals(DEVELOPER_PORT, developer.getPort());
	}

}
