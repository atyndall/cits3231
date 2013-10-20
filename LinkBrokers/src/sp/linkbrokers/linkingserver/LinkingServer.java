package sp.linkbrokers.linkingserver;


import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.rmi.ssl.*;

import sp.softwarehouse.libprovidingserver.ILibProvidingServer;


public class LinkingServer extends UnicastRemoteObject implements ILinkingServer {
	
	private static final long serialVersionUID = -1076731227201413447L;
	public static final int linkingPort = 54164;
	public static final int libProviderPort = 54165;
	
	protected LinkingServer() throws RemoteException {
		super(linkingPort, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory(null, null, true));
	}

	public static void main(String args[]) {
		URL trustDir = LinkingServer.class.getResource("/truststore-linkbrokers.jks");
		
		System.out.println("Truststore: " + trustDir.getPath());
		
		System.setProperty("javax.net.ssl.keyStore", trustDir.getPath());
		System.setProperty("javax.net.ssl.keyStorePassword", "123456");
		System.setProperty("javax.net.ssl.trustStore", trustDir.getPath());
		System.setProperty("javax.net.ssl.trustStorePassword", "123456");
		
	    try {
	    	Registry libProviderReg = LocateRegistry.getRegistry("localhost", libProviderPort, new SslRMIClientSocketFactory());
			ILibProvidingServer libProvider = (ILibProvidingServer) libProviderReg.lookup("LibProvidingServer");
			System.out.println(libProvider.giveMeLib());
	    	
	    	
			Registry linkingReg = LocateRegistry.createRegistry(linkingPort,
							new SslRMIClientSocketFactory(),
							new SslRMIServerSocketFactory(null, null, true));
			
			LinkingServer linkingSvr = new LinkingServer();
			
			String addr = "LinkingServer";
			linkingReg.bind(addr, linkingSvr);
			
			System.out.println("The RMI registry is currently running on port " + linkingPort);
			System.out.println("Clients can bind to the linking server with '"+addr+"'");
		
        } catch (Exception e) {
        	System.out.println("LinkingServer err: " + e.getMessage());
            e.printStackTrace();
        }
	}
	
	public int giveMeCake() throws RemoteException {
		return 4; // no, you get the number 4
	}
	
}
