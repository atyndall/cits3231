package sp.softwarehouse.libprovidingserver;

import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class LibProvidingServer extends UnicastRemoteObject implements ILibProvidingServer {
	
	private static final long serialVersionUID = 9214577972245950200L;
	public static final int libProvidingPort = 54165;
	
	protected LibProvidingServer() throws RemoteException {
		super(libProvidingPort, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory(null, null, true));
	}

	public static void main(String args[]) {
		URL trustDir = LibProvidingServer.class.getResource("/truststore-softwarehouse.jks");
		
		System.out.println("Truststore: " + trustDir.getPath());
		
		System.setProperty("javax.net.ssl.keyStore", trustDir.getPath());
		System.setProperty("javax.net.ssl.keyStorePassword", "123456");
		System.setProperty("javax.net.ssl.trustStore", trustDir.getPath());
		System.setProperty("javax.net.ssl.trustStorePassword", "123456");
		
	    try {
			Registry reg = LocateRegistry.createRegistry(libProvidingPort,
							new SslRMIClientSocketFactory(),
							new SslRMIServerSocketFactory(null, null, true));
			
			LibProvidingServer obj = new LibProvidingServer();
			
			String addr = "LibProvidingServer";
			reg.bind(addr, obj);
			
			System.out.println("The RMI registry is currently running on port " + libProvidingPort);
			System.out.println("Clients can bind to the linking server with '"+addr+"'");
		
        } catch (Exception e) {
        	System.out.println("LibProvidingServer err: " + e.getMessage());
            e.printStackTrace();
        }
	}
	
	public int giveMeLib() throws RemoteException {
		return 9; // no, you get the number 4
	}
	
}
