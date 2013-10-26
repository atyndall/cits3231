package sp.linkbrokers.linkingserver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.rmi.ssl.*;

import sp.common.SoftwareHouseRequest;
import sp.softwarehouse.libprovidingserver.ILibProvidingServer;

public class LinkingServer extends UnicastRemoteObject implements
		ILinkingServer {

	private static final long serialVersionUID = -1076731227201413447L;
	public static final int linkingPort = 54164;
	public static final int libProviderPort = 54165;

	private ILibProvidingServer libProvidingServer;

	public LinkingServer() throws RemoteException {
		super(linkingPort, new SslRMIClientSocketFactory(),
				new SslRMIServerSocketFactory(null, null, true));

		Registry libProviderReg = LocateRegistry.getRegistry("localhost",
				libProviderPort, new SslRMIClientSocketFactory());

		try {
			this.libProvidingServer = (ILibProvidingServer) libProviderReg
					.lookup("LibProvidingServer");
		} catch (NotBoundException e) {
			e.printStackTrace();
			return;
		}
	}

	public static void main(String args[]) {
		URL trustDir = LinkingServer.class
				.getResource("/truststore-linkbrokers.jks");

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

			System.out.println("The RMI registry is currently running on port "
					+ linkingPort);
			System.out.println("Clients can bind to the linking server with '"
					+ addr + "'");
		} catch (Exception e) {
			System.out.println("LinkingServer err: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// http://stackoverflow.com/questions/13575795/programatically-add-class-to-existing-jar-file
	// http://stackoverflow.com/questions/3048669/how-can-i-add-entries-to-an-existing-zip-file-in-java
	public static void addFilesToExistingJar(File jarFile,
			Map<String, File> filesToAdd) throws IOException {

		File tempFile = createTempFileFromJar(jarFile);

		ZipInputStream jarIn = new ZipInputStream(new FileInputStream(tempFile));
		ZipOutputStream jarOut = new ZipOutputStream(new FileOutputStream(
				jarFile));

		ZipEntry fileAlreadyInJar = null;

		while ((fileAlreadyInJar = jarIn.getNextEntry()) != null){
			String entryName = fileAlreadyInJar.getName();

			if (!isFileNameInList(entryName, filesToAdd)) {
				replaceFileInJar(entryName, jarIn, jarOut);
			}

		}

		jarIn.close();

		final int jarBufferSize = 1024;
		byte[] newJarFileBuffer = new byte[jarBufferSize];

		for (Entry<String, File> fileToAddToJar : filesToAdd.entrySet()) {
			InputStream in = new FileInputStream(fileToAddToJar.getValue());

			// Add ZIP entry to output stream.
			jarOut.putNextEntry(new ZipEntry(fileToAddToJar.getKey()));
			// Transfer bytes from the file to the ZIP file

			int len;
			while ((len = in.read(newJarFileBuffer)) > 0) {
				jarOut.write(newJarFileBuffer, 0, len);
			}

			// Complete the entry
			jarOut.closeEntry();
			in.close();
		}

		jarOut.close();
		tempFile.delete();
	}

	private static void replaceFileInJar(String entryName,
			ZipInputStream zipIn, ZipOutputStream zipOut) throws IOException {

		final int jarBufferSize = 1024;
		byte[] newJarFileBuffer = new byte[jarBufferSize];

		zipOut.putNextEntry(new ZipEntry(entryName));

		int numberOfNewZipBytesReadIn;

		while ((numberOfNewZipBytesReadIn = zipIn.read(newJarFileBuffer)) > 0) {
			zipOut.write(newJarFileBuffer, 0, numberOfNewZipBytesReadIn);
		}
	}

	private static File createTempFileFromJar(File jarFile) throws IOException {
		/**
		 * Create a temporary file, delete it so existing jar file can be
		 * renamed to the current temporary file
		 */
		File tempFile = File.createTempFile("link", ".jar");
		tempFile.delete();
		
		if ( !(jarFile).renameTo(tempFile) ) 
			throw new RuntimeException("Could not rename the file "
					+ jarFile.getAbsolutePath() + " to "
					+ tempFile.getAbsolutePath());
		
		return tempFile;
	}

	private static boolean isFileNameInList(String entryName,
			Map<String, File> files) {
		for (Entry<String, File> fileEntry : files.entrySet()) {
			if (fileEntry.getKey().equals(entryName))
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

			Map<String, byte[]> clss = libProvidingServer.getClassesToLink(req);
			Map<String, File> oclss = new HashMap<String, File>();

			// convert inputstreams to tmp files
			for (Entry<String, byte[]> e : clss.entrySet()) {
				File tmp = File.createTempFile("link", ".class");
				FileOutputStream fos = new FileOutputStream(tmp);
				fos.write(e.getValue());
				oclss.put(e.getKey(), tmp);
			}

			addFilesToExistingJar(outJarF, oclss);

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
