package sp.linkbrokers.linkingserver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;


import sp.common.RunOptions;
import sp.common.SoftwareHouseRequest;

public class LinkingServer extends UnicastRemoteObject implements
		ILinkingServer {

	private static final long serialVersionUID = -1076731227201413447L;
	public static final int port = 54164;
	public static final int libProviderPort = 54165;
	private LinkBroker linkBroker;
	private HashMap<String, String> options;

//	private ILibProvidingServer libProvidingServer;

	public LinkingServer(RunOptions options, LinkBroker linkBroker) throws RemoteException {
		this.linkBroker = linkBroker;
		this.options = options.getOptions();
		
//		super(linkingPort, new SslRMIClientSocketFactory(),
//				new SslRMIServerSocketFactory(null, null, true));
//
//		Registry libProviderReg = LocateRegistry.getRegistry("localhost",
//				libProviderPort, new SslRMIClientSocketFactory());
//
//		try {
//			this.libProvidingServer = (ILibProvidingServer) libProviderReg
//					.lookup("LibProvidingServer");
//		} catch (NotBoundException e) {
//			e.printStackTrace();
//			return;
//		}
	}

//	public static void main(String args[]) {
//		
//		URL trustDir = LinkingServer.class
//				.getResource("/truststore-linkbrokers.jks");
//
//		System.out.println("Truststore: " + trustDir.getPath());
//
//		System.setProperty("javax.net.ssl.keyStore", trustDir.getPath());
//		System.setProperty("javax.net.ssl.keyStorePassword", "123456");
//		System.setProperty("javax.net.ssl.trustStore", trustDir.getPath());
//		System.setProperty("javax.net.ssl.trustStorePassword", "123456");

//		try {
//			Registry linkingReg = LocateRegistry.createRegistry(linkingPort,
//					new SslRMIClientSocketFactory(),
//					new SslRMIServerSocketFactory(null, null, true));

//			LinkingServer linkingSvr = new LinkingServer();

//			String addr = "LinkingServer";
//			linkingReg.bind(addr, linkingSvr);
//
//			System.out.println("The RMI registry is currently running on port "
//					+ linkingPort);
//			System.out.println("Clients can bind to the linking server with '"
//					+ addr + "'");
//		} catch (Exception e) {
//			System.out.println("LinkingServer err: " + e.getMessage());
//			e.printStackTrace();
//		}
//	}

	@Override
	public byte[] performLink(SoftwareHouseRequest req, byte[] inJar)
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getPort() {
		return port;
	}

	public String getAddress() throws UnknownHostException {
		return InetAddress.getLocalHost().toString();
	}

//	public byte[] performLink(SoftwareHouseRequest req, byte[] inJarByte) {
//		// we read the jar file to a location on the disk
////		try {
////			File outJarF = File.createTempFile("link", ".jar");
////			FileOutputStream outJar = new FileOutputStream(outJarF);
////			outJar.write(inJarByte);
////			outJar.close();
////
////			Map<String, byte[]> clss = libProvidingServer.getClassesToLink(req);
////			Map<String, File> oclss = new HashMap<String, File>();
////
////			// convert inputstreams to tmp files
////			for (Entry<String, byte[]> e : clss.entrySet()) {
////				File tmp = File.createTempFile("link", ".class");
////				FileOutputStream fos = new FileOutputStream(tmp);
////				fos.write(e.getValue());
////				oclss.put(e.getKey(), tmp);
////			}
////
////			addFilesToExistingJar(outJarF, oclss);
////
////			ByteArrayOutputStream bos = new ByteArrayOutputStream();
////			FileInputStream outJarIn = new FileInputStream(outJarF);
////			int read = 0;
////			byte[] bytes = new byte[1024];
////			while ((read = outJarIn.read(bytes)) != -1) {
////				bos.write(bytes, 0, read);
////			}
////			outJarIn.close();
////
////			return bos.toByteArray();
////		} catch (Exception e) {
////			e.printStackTrace();
////			return null;
////		}
//
//	}

}
