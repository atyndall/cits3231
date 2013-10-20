package sp.tests;

import static org.junit.Assert.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import sp.common.LinkingRequest;
import sp.common.SoftwareHouseRequest;

public class SoftwareHouseRequestTest extends TestHelper {
	
	static String softwareHouseName = "developer1";
	static String[] libraryNames = new String[]{"Library1", "Library2", "Library3"};
	
	static String encryption = "RSA";
	static String symmetricEncryption = "AES";
	
	static PrivateKey privateKey;
	static PublicKey publicKey;

	static PrivateKey differentPrivateKey;
	static PublicKey differentPublicKey;
	
	static LinkingRequest linkRequestForSingleLibrary;
	static LinkingRequest linkRequestForMultipleLibraries;
	static LinkingRequest emptyLinkingRequest;
	
	@BeforeClass
	static public void setUp() throws NoSuchAlgorithmException{
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(encryption);
		KeyPair pair = keyGen.generateKeyPair();
		
		privateKey = pair.getPrivate();
		publicKey = pair.getPublic();
		
		pair = keyGen.generateKeyPair();
		
		differentPrivateKey = pair.getPrivate();
		differentPublicKey = pair.getPublic();
		
		ArrayList<String> libraryList = new ArrayList<String>();
		libraryList.add(libraryNames[0]);
		
		linkRequestForSingleLibrary = new LinkingRequest();
		linkRequestForSingleLibrary.addLibraries(libraryList);
		
		linkRequestForMultipleLibraries = new LinkingRequest();
		emptyLinkingRequest = new LinkingRequest();
		
		for(int i=1; i< libraryNames.length; i++){
			libraryList.add(libraryNames[i]);
		}
		
		linkRequestForMultipleLibraries.addLibraries(libraryList);
	}

	@Test
	public void shouldCorrectlyEncryptAndDecryptRequestForSingleLibrary(){
		SoftwareHouseRequest request = new SoftwareHouseRequest(linkRequestForSingleLibrary, publicKey, symmetricEncryption);
		
		assertIsSameSoftwareHouseRequest(linkRequestForSingleLibrary,
				request.getRequest(privateKey, symmetricEncryption));
	}
	
	@Test
	public void shouldCorrectlyEncryptAndDecryptRequestForMultipleLibraries(){
		SoftwareHouseRequest request = new SoftwareHouseRequest(linkRequestForMultipleLibraries, publicKey, symmetricEncryption);
		
		assertIsSameSoftwareHouseRequest(linkRequestForMultipleLibraries,
				request.getRequest(privateKey, symmetricEncryption));
	}
	
	@Test
	public void shouldCorrectlyEncryptAndDecryptEmptyRequest(){
		SoftwareHouseRequest request = new SoftwareHouseRequest(emptyLinkingRequest, publicKey, symmetricEncryption);
		
		assertEquals(true, request.getRequest(privateKey, symmetricEncryption).getLibraryList().isEmpty());
	}
	
	@Test
	public void shouldNotAllowLinkingRequestToBeAltered(){
		SoftwareHouseRequest request = new SoftwareHouseRequest(linkRequestForSingleLibrary, publicKey, symmetricEncryption);
		
		LinkingRequest returnedRequest = request.getRequest(privateKey, symmetricEncryption);
		
		
		modifyRequest(returnedRequest);
		
		assertIsNotSameSoftwareHouseRequest(linkRequestForSingleLibrary, returnedRequest);
		
		SoftwareHouseRequest secondRequest = new SoftwareHouseRequest(linkRequestForSingleLibrary, publicKey, symmetricEncryption);
		
		assertIsSameSoftwareHouseRequest(linkRequestForSingleLibrary,
				secondRequest.getRequest(privateKey, symmetricEncryption));
	}
	
	@Test
	public void shouldCorrectlyReportARequestThatHasntBeenSignedAsUnsigned(){
		SoftwareHouseRequest request = new SoftwareHouseRequest(linkRequestForSingleLibrary, 
				publicKey, symmetricEncryption);
		assertEquals(false, request.isSigned());
	}
	
	@Test
	public void shouldCorrectlyReportARequestThatHasBeenSignedAsSigned(){
		SoftwareHouseRequest request = new SoftwareHouseRequest(linkRequestForSingleLibrary, 
				publicKey, symmetricEncryption);
		request.sign(privateKey);
		assertEquals(true, request.isSigned());
	}
	
	@Test
	public void shouldReturnAFalseSignatureCheckForAnUnsignedRequest(){
		SoftwareHouseRequest request = new SoftwareHouseRequest(linkRequestForSingleLibrary, 
				publicKey, symmetricEncryption);
		assertEquals(false, request.isSignatureCorrect(publicKey));
	}
	
	@Test
	public void shouldReturnATrueSignatureCheckForARequestSignedWithCorrespondingPrivateKey(){
		SoftwareHouseRequest request = new SoftwareHouseRequest(linkRequestForSingleLibrary, 
				publicKey, symmetricEncryption);
		request.sign(privateKey);
		
		assertEquals(true, request.isSignatureCorrect(publicKey));
	}
	
	@Test
	public void shouldReturnAFalseSignatureCheckForARequestSignedWithADifferentPrivateKey(){
		SoftwareHouseRequest request = new SoftwareHouseRequest(linkRequestForSingleLibrary, 
				publicKey, symmetricEncryption);
		request.sign(privateKey);
		
		assertEquals(false, request.isSignatureCorrect(differentPublicKey));
	}
	
	
	
	
	private void assertIsNotSameSoftwareHouseRequest(LinkingRequest originalRequest, LinkingRequest request) {
		ArrayList<String> requestLibraryList = request.getLibraryList();
		ArrayList<String> originalLibraryList = request.getLibraryList();
		
		assertEquals(requestLibraryList.size(), originalLibraryList.size());  
				
		for(int i=0; i<requestLibraryList.size(); i++){
			assertFalse(requestLibraryList.get(i).equals(originalLibraryList.get(i)));
		}
		
	}

	private void assertIsSameSoftwareHouseRequest(LinkingRequest originalRequest, LinkingRequest request) {
		ArrayList<String> requestLibraryList = request.getLibraryList();
		ArrayList<String> originalLibraryList = request.getLibraryList();
		
		assertEquals(requestLibraryList.size(), originalLibraryList.size());  
				
		for(int i=0; i<requestLibraryList.size(); i++){
			assertEquals(requestLibraryList.get(i),originalLibraryList.get(i));
		}
	}

	private void modifyRequest(LinkingRequest originalRequest) {
		originalRequest.getLibraryList().clear();
	}
	
//	@Test(expected=BadPaddingException.class)
//	public void shouldRaiseExceptionIfIncorrectPrivateKeyIsUsed() throws NoSuchAlgorithmException{
//		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(encryption);
//		KeyPair pair = keyGen.generateKeyPair();
//		
//		PrivateKey wrongPrivateKey = pair.getPrivate();
//		
//		SoftwareHouseRequest request = new SoftwareHouseRequest(linkRequestForSingleLibrary, publicKey, symmetricEncryption);
//		assertEquals(linkRequestForSingleLibrary.getLibraryList().get(0), request.getRequest(wrongPrivateKey, symmetricEncryption).getLibraryList().get(0));
//	}

}
