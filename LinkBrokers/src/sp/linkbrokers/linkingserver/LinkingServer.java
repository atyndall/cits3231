package sp.linkbrokers.linkingserver;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.rmi.ssl.*;
import java.security.cert.Certificate;

import sp.common.ChecksumGenerator;
import sp.common.SoftwareHouseRequest;
import sp.softwarehouse.libprovidingserver.ILibProvidingServer;


public class LinkingServer extends UnicastRemoteObject implements ILinkingServer {
	
	private static final long serialVersionUID = -1076731227201413447L;
	public static final int linkingPort = 54164;
	public static final int libProviderPort = 54165;
	
	private ILibProvidingServer libProvidingServer;
	
	public LinkingServer() throws RemoteException {
		super(linkingPort, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory(null, null, true));
		
    	Registry libProviderReg = LocateRegistry.getRegistry("localhost", libProviderPort, new SslRMIClientSocketFactory());
    	try {
			this.libProvidingServer = (ILibProvidingServer) libProviderReg.lookup("LibProvidingServer");
		} catch (NotBoundException e) {
			e.printStackTrace();
			return;
		}
	}

	public static void main(String args[]) {
		URL trustDir = LinkingServer.class.getResource("/truststore-linkbrokers.jks");
		
		System.out.println("Truststore: " + trustDir.getPath());
		
		System.setProperty("javax.net.ssl.keyStore", trustDir.getPath());
		System.setProperty("javax.net.ssl.keyStorePassword", "123456");
		System.setProperty("javax.net.ssl.trustStore", trustDir.getPath());
		System.setProperty("javax.net.ssl.trustStorePassword", "123456");
		
	    try {
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
	
	// http://stackoverflow.com/questions/13575795/programatically-add-class-to-existing-jar-file
	// http://stackoverflow.com/questions/3048669/how-can-i-add-entries-to-an-existing-zip-file-in-java
	public static void addFilesToExistingZip(File jarFile,
	         Map<String,File> files) throws IOException {
		
        File tempFile = initialiseJarTmpFile(jarFile);
	    byte[] buf = new byte[1024];

	    ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
	    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(jarFile));

	    ZipEntry entry = zin.getNextEntry();
	    
	    while (entry != null) {
	    	String entryName = entry.getName();
	    	
	        if (!isFileInDatabase(entryName,files)) {
	            // Add ZIP entry to output stream.
	            out.putNextEntry(new ZipEntry(entryName));
	            // Transfer bytes from the ZIP file to the output file
	            
	            int len;
	            
	            while ((len = zin.read(buf)) > 0) {
	                out.write(buf, 0, len);
	            }
	        }
	        
	        entry = zin.getNextEntry();
	    }
	    
	    // Close the streams        
	    zin.close();
	    // Compress the files
	    
	    for (Entry<String, File> e : files.entrySet()) {
	        InputStream in = new FileInputStream(e.getValue());
	        // Add ZIP entry to output stream.
	        out.putNextEntry(new ZipEntry(e.getKey()));
	        // Transfer bytes from the file to the ZIP file
	        int len;
	        while ((len = in.read(buf)) > 0) {
	            out.write(buf, 0, len);
	        }
	        // Complete the entry
	        out.closeEntry();
	        in.close();
	    }
	    // Complete the ZIP file
	    out.close();
	    tempFile.delete();
	}

	private static File initialiseJarTmpFile(File jarFile) throws IOException {
		/**
         * Create a temp file, delete it so existing jar file can be renamed to
         * the current temp file
         */
	    File tempFile = File.createTempFile("link", ".jar");
	    tempFile.delete();

	    if (!jarFile.renameTo(tempFile))
	        throw new RuntimeException("Could not rename the file "+
	        		jarFile.getAbsolutePath()+" to "+tempFile.getAbsolutePath());
		return tempFile;
	}

	private static boolean isFileInDatabase(String entryName, Map<String,File> files){
        for (Entry<String, File> e : files.entrySet()) {
            if (e.getKey().equals(entryName))
                return true;
        }
        
        return false;
	}
	
	public byte[] performLink(SoftwareHouseRequest req, byte[] inJarByte) {
		// we read the jar file to a location on the disk
		try {
			File outJarF = File.createTempFile("link", ".jar");
			FileOutputStream outJar = new FileOutputStream(outJarF);
			outJar.write(inJarByte);
			outJar.close();
			
			// TODO: We need to pass the developer's certificate and public key in the request
			// then we just verify that their cert is signed by SoftwareHouse
			// right now this will fail because we can't access ths information
			Certificate developerCert = req.getCertificate();
			String softwareHouseName = "softwarehouse"; // TODO: this will need to be customised based on actual name of the software house
			PublicKey softWareHouseKey = null; // TODO we must get the softwarehouse public key somehow
			
			try {
				developerCert.verify(softWareHouseKey);
			} catch (SignatureException e) {
				// panic
			}
			
			PublicKey developerKey = developerCert.getPublicKey();
			
			// verify checksums
			Map<String, byte[]> providedChecksums = req.getChecksums(developerKey);
			Map<String, byte[]> generatedChecksums = ChecksumGenerator.getChecksums(new JarInputStream(new FileInputStream(outJarF.getAbsolutePath())));
			
			if (!ChecksumGenerator.verifyByteMapsEqual(providedChecksums, generatedChecksums)) {
				// TODO: panic
			}
			
			
			Map<String, byte[]> clss = libProvidingServer.getClassesToLink(req);
			Map<String, File> oclss = new HashMap<String, File>();
			
			// convert inputstreams to tmp files
			for(Entry<String, byte[]> e : clss.entrySet()) {
				File tmp = File.createTempFile("link", ".class");
				FileOutputStream fos = new FileOutputStream(tmp);
				fos.write(e.getValue());				
				oclss.put(e.getKey(), tmp);
			}
			
			addFilesToExistingZip(outJarF, oclss);
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			FileInputStream outJarIn = new FileInputStream(outJarF);
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = outJarIn.read(bytes)) != -1) {
				bos.write(bytes, 0, read);
			}
			outJarIn.close();
			
			return bos.toByteArray();			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}

	
}
