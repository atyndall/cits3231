package sp.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import sp.linkbrokers.linkingserver.LinkBroker;
import sp.linkbrokers.linkingserver.LinkingServer;
import sp.linkbrokers.linkingserver.RemoteLinkingServer;
import sp.runoptions.RunOptions;

public class LinkingBrokerTest {
	private static String emptyJarFilePath;
	private static Map<String, File> listContainingFileA;
	private static byte[] fileADigest;
	private static File fileA;
	private static final String fileAHandle = "file1";
	
	private static final String fileBContents = "Not fileA";
	private static final String fileAContents = "Test File Contents";	
	
	private static Registry rmiRegistry;
	private static String LINKING_SERVER_NAME = "LinkingServer";
	private static String LINKING_SERVER_ADDRESS = "localhost";
	private static int LINKING_SERVER_PORT = 12345;
	private static RunOptions validRunOptions;
	
	@BeforeClass
	static public void setUpClass(){
		try {
			emptyJarFilePath = createEmptyJar();
			
			listContainingFileA = new HashMap<String, File>();
			fileA = createTestFileWithContents(fileAContents);
			listContainingFileA.put(fileAHandle, fileA);
			
			FileInputStream fileAIn = new FileInputStream(fileA);
			fileADigest = calculateFileDigest(fileAIn);
			fileAIn.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		HashMap<String,String> options = new HashMap<String,String>();
		options.put("port", String.valueOf(LINKING_SERVER_PORT));
		
		validRunOptions = new RunOptions(options, null);

	}

	@Test
	public void shouldAddNewLinkingServerReferenceToRMIRegistry(){
		ensureLinkingServerNotInRMIRegistry();
		
		new LinkBroker(validRunOptions);
		
		assertLinkingServerInRMIRegistry();
	}
	
	@Test
	public void shouldAddNewFilesToJar() {
		try {
			LinkingServer.addFilesToExistingJar(new File(emptyJarFilePath), listContainingFileA);
		
			assertArrayEquals(fileADigest,digestOfFileInJar(fileAHandle, emptyJarFilePath));
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void shouldAddNewFilesToJarWhenItAlreadyContainsAFileWithTheSameName() {
		try {
			
			File fileWithSameNameAsFileA = createTestFileWithContents(fileBContents);
			String jarFilePath = createJarWithFile(fileWithSameNameAsFileA);
			
			LinkingServer.addFilesToExistingJar(new File(jarFilePath), listContainingFileA);
		
			assertArrayEquals(fileADigest,digestOfFileInJar(fileAHandle, jarFilePath));
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void tearDownClas(){
		removeLinkingServerFromRMIRegistry();
	}
	
	private static String createEmptyJar() {
		return createJarWithFile(null);
	}

	private static String createJarWithFile(File fileToAdd) {
		File jarFile = null;
		
		try {
			jarFile = File.createTempFile("testJar", ".jar");
			
			if(fileToAdd != null){
				FileInputStream fileToAddIn = new FileInputStream(fileToAdd);
				FileOutputStream jarFileOut = new FileOutputStream(jarFile);
				JarOutputStream emptyJarOut = new JarOutputStream(jarFileOut,new Manifest());
				
				byte[] inBuffer = new byte[128];
				
				emptyJarOut.putNextEntry(new JarEntry(fileToAdd.getName()));
				
				while((fileToAddIn.read(inBuffer) > 0))
					emptyJarOut.write(inBuffer);
				
				fileToAddIn.close();
				emptyJarOut.close();
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return jarFile.getAbsolutePath();
	}

	private byte[] digestOfFileInJar(String fileHandle, String jarFilePath){
		byte[] actualHash = null;
		
		try {
			JarFile emptyJar = new JarFile(jarFilePath);
			InputStream newFileIn = emptyJar.getInputStream(emptyJar.getEntry(fileHandle));
			actualHash = calculateFileDigest(newFileIn);
			emptyJar.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return actualHash;
	}

	private static File createTestFileWithContents(String fileContents) {
		File testFile = null;
		
		try {
			testFile = File.createTempFile("newTestFile", null);
			FileOutputStream testFileOut = new FileOutputStream(testFile);
			
			testFileOut.write(fileContents.getBytes());
			testFileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return testFile;
	}
	
	private static byte[] calculateFileDigest(InputStream inputStream){
		MessageDigest digest = null;
		
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		
		byte[] inputBuffer = new byte[128];
		
		try {
		  DigestInputStream digestInput = new DigestInputStream(inputStream, digest);
		  digestInput.setMessageDigest(digest);
		  
		  while((digestInput.read(inputBuffer)) > 1)
			  digest.update(inputBuffer);
		  
		  
		  digestInput.close();
		  
		} catch ( IOException e) {
			e.printStackTrace();
		}

		return digest.digest();
	}

	private static void assertLinkingServerInRMIRegistry(){
		RemoteLinkingServer linkingServer;
		
		try {
			Registry registry = getRegistry();
			linkingServer = (RemoteLinkingServer) registry.lookup(LINKING_SERVER_NAME);
			
			assert(linkingServer instanceof LinkingServer);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		} 
	}

	private void ensureLinkingServerNotInRMIRegistry() {
		try{
			assertLinkingServerInRMIRegistry();
		} catch(AssertionError e){
			removeLinkingServerFromRMIRegistry();
		}
	}

	private static Registry getRegistry() throws RemoteException {
		if(rmiRegistry == null)
			rmiRegistry = LocateRegistry.getRegistry(LINKING_SERVER_ADDRESS , LINKING_SERVER_PORT );
		
		return rmiRegistry;
	}
	
	private static void removeLinkingServerFromRMIRegistry(){
		try {
			getRegistry().unbind(LINKING_SERVER_NAME);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
		}
	}
	
}
