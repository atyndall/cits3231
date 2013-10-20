package sp.tests;

import static org.junit.Assert.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;

import org.junit.Test;

import sp.common.LinkingRequest;
import sp.common.SoftwareHouseRequest;

public class SoftwareHouseRequestTest extends TestHelper {
	static Certificate developerCertificate;
	static String softwareHouseName = "developer1";
	static String libraryName = "LibraryName";
	static String encryption = "RSA";
	static String symmetricEncryption = "AES";
	

	@Test
	public void shouldCorrectlyEncryptAndDecryptRequest() throws NoSuchAlgorithmException {
		ArrayList<String> libraryList = new ArrayList<String>();
		libraryList.add(libraryName);
		
		LinkingRequest linkRequest = new LinkingRequest();
		linkRequest.addLibraries(libraryList);
		
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(encryption);
		
		KeyPair pair = keyGen.generateKeyPair();
		PrivateKey privateKey = pair.getPrivate();
		PublicKey publicKey = pair.getPublic();
		
		SoftwareHouseRequest request = new SoftwareHouseRequest(linkRequest, publicKey, symmetricEncryption);
		assertEquals(libraryList.get(0), request.getRequest(privateKey, symmetricEncryption).getLibraryList().get(0));
	}

}
