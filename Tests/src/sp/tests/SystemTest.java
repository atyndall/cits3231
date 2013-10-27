package sp.tests;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import sp.exceptions.RunOptionException;
import sp.linkbrokers.linkingserver.LinkingServer;
import sp.linkbrokers.linktool.DeveloperLinkTool;
import sp.runoptions.RunOptions;

public class SystemTest {
	private static final int DEVELOPER_PORT = 1234; 
	private static HashMap<String, String> argumentList;
	private HashMap<String, ArrayList<String>> libraries;
	private RunOptions emptyRunOptions;
	private RunOptions runOptions;
	
	private static HashMap<String, String> linkBrokerArgumentList;
	private RunOptions linkBrokerRunOptions;
	private LinkingServer linkingServer;
	private PrintStream stdOut;
	
	private static Thread linkingServerThread;
	private static final String LINKBROKER_DEVELOPER_PORT = "1235";
	
	@Before
	public void setUp(){
		emptyRunOptions = new RunOptions(new HashMap<String, String>(), new HashMap<String, ArrayList<String>>());
		
		LinkingServer linkingServer = setUpLinkBroker();
		setUpDeveloperRunArguments(linkingServer);
	}

	private RunOptions validDeveloperRunOptions(){
		libraries = new HashMap<String, ArrayList<String>>();
		
		argumentList = new HashMap<String, String>();
		argumentList.put("port", String.valueOf(DEVELOPER_PORT));
		argumentList.put("lbport", LINKBROKER_DEVELOPER_PORT);
		
		return new RunOptions(argumentList, libraries);
	}
	
	private LinkingServer setUpLinkBroker() {
		linkBrokerArgumentList = new HashMap<String, String>();
		linkBrokerRunOptions = new RunOptions(linkBrokerArgumentList, null);
		
		linkBrokerArgumentList.put("port", LINKBROKER_DEVELOPER_PORT);
		
		RunLinkServer runLinkingServer = new RunLinkServer(linkBrokerRunOptions);
		linkingServerThread = new Thread(runLinkingServer);
		linkingServerThread.start();
		
		while( (linkingServer = runLinkingServer.getLinkingServer()) == null){
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return linkingServer;
	}

	private void setUpDeveloperRunArguments(LinkingServer linkingServer) {
		runOptions = validDeveloperRunOptions();

		try {
			argumentList.put("lbaddress", String.valueOf(linkingServer.getAddress()) );
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
