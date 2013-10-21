package sp.linkbrokers.cmdlinker;

import java.net.URL;
import java.rmi.ConnectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.rmi.ssl.SslRMIClientSocketFactory;

import sp.linkbrokers.linkingserver.ILinkingServer;


public class CommandLineLink {

	public static final int linkingPort = 54164;
	
	final static String storeLocation = "/truststore-developer1.jks";
	final static String rmiRegistryAddress = "localhost";
	final static String linkingServerClassName = "LinkingServer";
	
	public static void main(String args[]) throws Exception {
		URL trustDir = CommandLineLink.class.getResource(storeLocation);
		
		//Developer.log("Truststore: " + trustDir.getPath());
		
		initiliseStoreValues(trustDir);
		
		try {
			
			Registry reg = LocateRegistry.getRegistry(rmiRegistryAddress, linkingPort, new SslRMIClientSocketFactory());
			ILinkingServer linkingSvr = (ILinkingServer) reg.lookup(linkingServerClassName);
			
			// TODO: we should actually call the linker here
			// linkingSvr.performLink(req, inJar)
			
		} catch (ConnectException e) {
			
        	//Developer.logError(e.getMessage());
            e.printStackTrace();
		}
		
	}

	private static void initiliseStoreValues(URL trustDir) {
		System.setProperty("javax.net.ssl.keyStore", trustDir.getPath());
		System.setProperty("javax.net.ssl.keyStorePassword", "123456");
		System.setProperty("javax.net.ssl.trustStore", trustDir.getPath());
		System.setProperty("javax.net.ssl.trustStorePassword", "123456");
	}
	
}
