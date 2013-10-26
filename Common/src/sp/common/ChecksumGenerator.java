package sp.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignedObject;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ChecksumGenerator {

	public static Map<String, byte[]> getChecksums(JarInputStream jar) throws IOException {
		Map<String, byte[]> checksums = new HashMap<String, byte[]>();
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		
		JarEntry je = jar.getNextJarEntry();
		
		byte[] buffer = new byte[1024];
		while (je != null) {
			if (!je.isDirectory()) {
				String fileName = je.getName();
				
				ByteArrayOutputStream os = new ByteArrayOutputStream();             
				 
	            int len;
	            while ((len = jar.read(buffer)) > 0) {
	            	os.write(buffer, 0, len);
	            }
	 
	            os.close();
	            
	            byte[] digest = md.digest(os.toByteArray());
	            
	            checksums.put(fileName, digest);
	            
	            je = jar.getNextJarEntry();
			}
		}
		
		return checksums;
	}
	
	public static Map<String, String> checksumsToEnc(Map<String, byte[]> in) {
		Map<String, String> out = new HashMap<String, String>();
		
		for (Entry<String, byte[]> e : in.entrySet()) {
			out.put(e.getKey(), checksumToEnc(e.getValue()));
		}
		
		return out;
	}
	
	public static <A, B> boolean verifyMapsEqual(Map<A, B> a, Map<A, B> b) {
		if (a.size() != b.size()) {
			return false;
		}
		
		Set<A> aPaths = a.keySet();
		Set<A> bPaths = b.keySet();
		
		if (!aPaths.equals(bPaths)) {
			return false;
		}
		
		for (A p : aPaths) {
			if (!(a.get(p).equals(b.get(p)))) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean verifyByteMapsEqual(Map<String, byte[]> a, Map<String, byte[]> b) {
		return verifyMapsEqual(checksumsToEnc(a), checksumsToEnc(b));
	}
	
	private static String checksumToEnc(byte[] value) {
		StringBuilder sb = new StringBuilder();
	    for (byte b : value) {
	        sb.append(String.format("%02X", b));
	    }
		return sb.toString();
	}

	private static SignedObject signChecksums(PrivateKey key, Map<String, byte[]> checksums) throws InvalidKeyException, SignatureException, IOException {
		Signature signingEngine;
		try {
			signingEngine = Signature.getInstance("SHA1withRSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		
		return new SignedObject((Serializable)checksums, key, signingEngine);
	}
	
	public static SignedObject getSignedChecksums(PrivateKey key, JarInputStream jar) throws InvalidKeyException, SignatureException, IOException {
		return signChecksums(key, getChecksums(jar));
	}
	

}
