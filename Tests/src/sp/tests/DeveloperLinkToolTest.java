package sp.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import sp.common.RunOptions;
import sp.exceptions.RunOptionException;
import sp.linkbrokers.linktool.DeveloperLinkTool;

public class DeveloperLinkToolTest{
	private static final int DEVELOPER_PORT = 1234; 
	private static HashMap<String, String> argumentList;
	private HashMap<String, ArrayList<String>> libraries;
	private RunOptions runOptions;
	private final String jarFilePath = "jarFile";
	
	private static final String LINKBROKER_DEVELOPER_PORT = "1235";
	private static final String LINKBROKER_ADDRESS = "localhost";
	
	@Before
	public void setUp(){
		runOptions = validDeveloperRunOptions();
	}

	@Test
	public void shouldThrowExceptionIfPortNotDefined(){
		try{
			new DeveloperLinkTool(runOptionsWithout("port"));
			fail();
		} catch(RunOptionException e){
			assertEquals("Port was not defined", e.getMessage());
		}
	}

	@Test
	public void shouldCorrectlySetPort() {
		DeveloperLinkTool developer = null;
		
		try{
			developer = new DeveloperLinkTool(runOptions);
		} catch(RunOptionException e){
			System.out.println(e.getMessage());
		}
		
		assertEquals(DEVELOPER_PORT, developer.getPort());
	}

	@Test
	public void shouldCorrectlySetLinkPort(){
		DeveloperLinkTool developer = null;
		
		try{
			developer = new DeveloperLinkTool(runOptions);
		} catch(RunOptionException e){
			System.out.println(e.getMessage());
		}
		
		assertEquals(Integer.parseInt(LINKBROKER_DEVELOPER_PORT), developer.getLinkBrokerPort());
	}
	
	@Test
	public void shouldThrowExceptionIfLinkBrokerPortNotDefined(){
		try{
			new DeveloperLinkTool(runOptionsWithout("lbport"));
			fail();
		} catch(RunOptionException e){
			assertEquals("Link Broker port was not defined", e.getMessage());
		}
	}
	
	@Test
	public void shouldUseLocalHostAsDefaltLinkBrokerAddress(){
		assertEquals("localhost", new DeveloperLinkTool(runOptions).getLinkBrokerAddress());
	}
	
	@Test
	public void shouldThrowExceptionIfJarUndefined(){
		try{
			new DeveloperLinkTool(runOptionsWithout("jar"));
			fail();
		} catch(RunOptionException e){
			assertEquals("Jar file was not defined", e.getMessage());
		}
	}
	
	private RunOptions runOptionsWithout(String parameterToExclude) {
		RunOptions completeRunOptions = validDeveloperRunOptions();
		HashMap<String,String> newOptions = new HashMap<String,String>(completeRunOptions.getOptions());
		newOptions.remove(parameterToExclude);
		
		return new RunOptions(newOptions,completeRunOptions.getLibraries());
	}

	private RunOptions validDeveloperRunOptions(){
		libraries = new HashMap<String, ArrayList<String>>();
		
		argumentList = new HashMap<String, String>();
		argumentList.put("port", String.valueOf(DEVELOPER_PORT));
		argumentList.put("lbport", LINKBROKER_DEVELOPER_PORT);
		argumentList.put("lbaddress", LINKBROKER_ADDRESS );
		argumentList.put("jar", jarFilePath  );
		
		return new RunOptions(argumentList, libraries);
	}
}
