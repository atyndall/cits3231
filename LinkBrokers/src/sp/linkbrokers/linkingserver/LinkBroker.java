package sp.linkbrokers.linkingserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import sp.common.Node;
import sp.runoptions.RunOptions;

public class LinkBroker extends Node{

	private LinkingServer linkingServer;

	public LinkBroker(RunOptions runOptions){
		setOptions(runOptions.getOptions());
		runOptions = runOptions.subset(new String[]{"port"});
		
		try {
			this.linkingServer = new LinkingServer(runOptions);
		} catch (RemoteException e) {
			e.printStackTrace();
		}		
	}
	
	public static void main(String[] args){
		RunOptions runOptions = new LinkBrokerArgumentParser().parseArguments(args);
		new LinkBroker(runOptions);
	}
	
	// http://stackoverflow.com/questions/13575795/programatically-add-class-to-existing-jar-file
	// http://stackoverflow.com/questions/3048669/how-can-i-add-entries-to-an-existing-zip-file-in-java
	public static void addFilesToExistingJar(File jarFile,
			Map<String, File> filesToAdd) throws IOException {
	
		File tempFile = LinkBroker.createTempFileFromJar(jarFile);
	
		ZipInputStream jarIn = new ZipInputStream(new FileInputStream(tempFile));
		ZipOutputStream jarOut = new ZipOutputStream(new FileOutputStream(
				jarFile));
	
		ZipEntry fileAlreadyInJar = null;
	
		while ((fileAlreadyInJar = jarIn.getNextEntry()) != null){
			String entryName = fileAlreadyInJar.getName();
	
			if (!LinkBroker.isFileNameInList(entryName, filesToAdd)) {
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

	static boolean isFileNameInList(String entryName,
			Map<String, File> files) {
		for (Entry<String, File> fileEntry : files.entrySet()) {
			if (fileEntry.getKey().equals(entryName))
				return true;
		}
	
		return false;
	}

	static File createTempFileFromJar(File jarFile) throws IOException {
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

	static void replaceFileInJar(String entryName,
			ZipInputStream zipIn, ZipOutputStream zipOut) throws IOException {
	
		final int jarBufferSize = 1024;
		byte[] newJarFileBuffer = new byte[jarBufferSize];
	
		zipOut.putNextEntry(new ZipEntry(entryName));
	
		int numberOfNewZipBytesReadIn;
	
		while ((numberOfNewZipBytesReadIn = zipIn.read(newJarFileBuffer)) > 0) {
			zipOut.write(newJarFileBuffer, 0, numberOfNewZipBytesReadIn);
		}
	}

	@Override
	protected void log(String message) {
		
	}

	@Override
	protected void logError(String error) {
		
	}

	@Override
	protected HashMap<String, String> getDefaultOptions() {
		if(defaultOptions == null){
			defaultOptions = new HashMap<String, String>();
		}
		
		return defaultOptions;
	}

	public LinkingServer getLinkingServer() { return linkingServer; }

}
