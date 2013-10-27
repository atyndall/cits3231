package sp.softwarehouse.protectedlibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarInputStream;

import sp.common.ChecksumGenerator;
import sp.softwarehouse.protectedlibrary.Exceptions.InvalidLicenseException;

/*
 * Interface that all protected libraries extend
 */
public abstract class ProtectedLibrary {
	
	protected static void verifyChecksums(String[] paths, String[] checksums) throws InvalidLicenseException {
		if (paths.length != checksums.length) throw new InvalidLicenseException(); 
		
		try {
			File jarLoc = new File(ProtectedLibrary.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			JarInputStream is = new JarInputStream(new FileInputStream(jarLoc));
			
			Map<String, String> includedChecksums = getIncludedChecksums(paths, checksums);
			Map<String, String> generatedChecksums = ChecksumGenerator.checksumsToEnc(ChecksumGenerator.getChecksums(is));
			
			ClassLoader cl = ClassLoader.getSystemClassLoader();
			
			// remove checksums for classes that are subclasses of ProtectedLibrary
			for (Entry<String, String> e : generatedChecksums.entrySet()) {
				String cname = e.getKey().replace('/', '.');
				Class<?> cls = cl.loadClass(cname);
				if (cls.isAssignableFrom(ProtectedLibrary.class)) {
					generatedChecksums.remove(e.getKey());
				}
			}
			
			if (!ChecksumGenerator.verifyMapsEqual(includedChecksums, generatedChecksums)) {
				throw new InvalidLicenseException();
			}
		} catch (URISyntaxException | IOException | ClassNotFoundException e) {
			throw new InvalidLicenseException();
		}
		
	}
	
	private static Map<String, String> getIncludedChecksums(String[] paths, String[] checksums) {
		Map<String, String> out = new HashMap<String, String>();
		
		for (int i = 0; i < paths.length; i++) {
			out.put(paths[i], checksums[i]);
		}
		
		return out;
	}
	
}
