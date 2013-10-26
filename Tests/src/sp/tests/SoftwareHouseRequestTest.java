package sp.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import sp.common.LinkingRequest;
import sp.common.SoftwareHouseRequest;
import sp.exceptions.InvalidDeveloperLicenseFileException;
import sp.softwarehouse.protectedlibrary.DeveloperLicense;

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
		
		DeveloperLicense validLicense = null;
		List<DeveloperLicense> validLicenses = null;
		
		privateKey = pair.getPrivate();
		publicKey = pair.getPublic();
		
		pair = keyGen.generateKeyPair();
		
		differentPrivateKey = pair.getPrivate();
		differentPublicKey = pair.getPublic();
		
		List<String> libraryList = new ArrayList<String>();
		libraryList.add(libraryNames[0]);
		
		
		try {
			validLicense = DeveloperLicense.createLicense(validLicenseFile);
			validLicenses = new ArrayList<DeveloperLicense>();
			validLicenses.add(validLicense);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidDeveloperLicenseFileException e) {
			e.printStackTrace();
		}
		
		linkRequestForSingleLibrary = new LinkingRequest();
		linkRequestForSingleLibrary.addLibraries(libraryList, validLicenses); 
		
		linkRequestForMultipleLibraries = new LinkingRequest();
		emptyLinkingRequest = new LinkingRequest();
		
		for(int i=1; i< libraryNames.length; i++){
			libraryList.add(libraryNames[i]);
			validLicenses.add(validLicense);
		}
		
		linkRequestForMultipleLibraries.addLibraries(libraryList, validLicenses);
	}

	@Test
	public void shouldCorrectlyEncryptAndDecryptRequestForSingleLibrary() throws NoSuchAlgorithmException{
		SoftwareHouseRequest request = new SoftwareHouseRequest(linkRequestForSingleLibrary, publicKey, symmetricEncryption);
		
		assertIsSameSoftwareHouseRequest(linkRequestForSingleLibrary,
				request.getRequest(privateKey, symmetricEncryption));
	}
	
	@Test
	public void shouldCorrectlyEncryptAndDecryptRequestForMultipleLibraries() throws NoSuchAlgorithmException{
		SoftwareHouseRequest request = new SoftwareHouseRequest(linkRequestForMultipleLibraries, publicKey, symmetricEncryption);
		
		assertIsSameSoftwareHouseRequest(linkRequestForMultipleLibraries,
				request.getRequest(privateKey, symmetricEncryption));
	}
	
	@Test
	public void shouldCorrectlyEncryptAndDecryptEmptyRequest() throws NoSuchAlgorithmException{
		SoftwareHouseRequest request = new SoftwareHouseRequest(emptyLinkingRequest, publicKey, symmetricEncryption);
		
		assertEquals(true, request.getRequest(privateKey, symmetricEncryption).getLibraryList().isEmpty());
	}
	
	@Test
	public void shouldNotAllowLinkingRequestToBeAltered() throws NoSuchAlgorithmException{
		SoftwareHouseRequest request = new SoftwareHouseRequest(linkRequestForSingleLibrary, publicKey, symmetricEncryption);
		
		LinkingRequest returnedRequest = request.getRequest(privateKey, symmetricEncryption);
		
		
		modifyRequest(returnedRequest);
		
		assertIsNotSameSoftwareHouseRequest(linkRequestForSingleLibrary, returnedRequest);
		
		SoftwareHouseRequest secondRequest = new SoftwareHouseRequest(linkRequestForSingleLibrary, publicKey, symmetricEncryption);
		
		assertIsSameSoftwareHouseRequest(linkRequestForSingleLibrary,
				secondRequest.getRequest(privateKey, symmetricEncryption));
	}
	
	@Test
	public void shouldCorrectlyReportARequestThatHasntBeenSignedAsUnsigned() throws NoSuchAlgorithmException{
		SoftwareHouseRequest request = new SoftwareHouseRequest(linkRequestForSingleLibrary, 
				publicKey, symmetricEncryption);
		assertEquals(false, request.isSigned());
	}
	
	@Test
	public void shouldCorrectlyReportARequestThatHasBeenSignedAsSigned() throws NoSuchAlgorithmException{
		SoftwareHouseRequest request = new SoftwareHouseRequest(linkRequestForSingleLibrary, 
				publicKey, symmetricEncryption);
		request.sign(privateKey);
		assertEquals(true, request.isSigned());
	}
	
	@Test
	public void shouldReturnAFalseSignatureCheckForAnUnsignedRequest() throws NoSuchAlgorithmException{
		SoftwareHouseRequest request = new SoftwareHouseRequest(linkRequestForSingleLibrary, 
				publicKey, symmetricEncryption);
		assertEquals(false, request.isSignatureCorrect(publicKey));
	}
	
	@Test
	public void shouldReturnATrueSignatureCheckForARequestSignedWithCorrespondingPrivateKey() throws NoSuchAlgorithmException{
		SoftwareHouseRequest request = new SoftwareHouseRequest(linkRequestForSingleLibrary, 
				publicKey, symmetricEncryption);
		request.sign(privateKey);
		
		assertEquals(true, request.isSignatureCorrect(publicKey));
	}
	
	@Test
	public void shouldReturnAFalseSignatureCheckForARequestSignedWithADifferentPrivateKey() throws NoSuchAlgorithmException{
		SoftwareHouseRequest request = new SoftwareHouseRequest(linkRequestForSingleLibrary, 
				publicKey, symmetricEncryption);
		request.sign(privateKey);
		
		assertEquals(false, request.isSignatureCorrect(differentPublicKey));
	}
	
	
	
	
	private void assertIsNotSameSoftwareHouseRequest(LinkingRequest originalRequest, LinkingRequest request) {
		List<String> requestLibraryList = request.getLibraryList();  // TODO: update to work with new license structure
		List<String> originalLibraryList = request.getLibraryList();  // TODO: update to work with new license structure
		
		assertEquals(requestLibraryList.size(), originalLibraryList.size());  
				
		for(int i=0; i<requestLibraryList.size(); i++){
			assertFalse(requestLibraryList.get(i).equals(originalLibraryList.get(i)));
		}
		
	}

	private void assertIsSameSoftwareHouseRequest(LinkingRequest originalRequest, LinkingRequest request) {
		List<String> requestLibraryList = request.getLibraryList();  // TODO: update to work with new license structure
		List<String> originalLibraryList = request.getLibraryList();  // TODO: update to work with new license structure
		
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
