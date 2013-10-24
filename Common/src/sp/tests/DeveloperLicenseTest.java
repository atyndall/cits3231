package sp.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import sp.softwarehouse.protectedlibrary.DeveloperLicense;

public class DeveloperLicenseTest extends TestHelper{
	
	@BeforeClass
	public static void setUpClass(){
		TestHelper.setUpClass();
	}
	
	@Test
	public void testCreatesCorrectLicenseFromValidFile() throws IOException {
		DeveloperLicense license = DeveloperLicense.createLicense(validLicenseFile);
		
		assertEquals(validDeveloperName, license.getDeveloperName());
		assertEquals(validIdentifier, license.getIdentifier());
		assertEquals(validEncryptedLicense, license.getLicense());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testRaisesExceptionForEmptyLicenseFile(){
		try{
			File emptyFile = createLicenseFile("","","");
			DeveloperLicense.createLicense(emptyFile);
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testRaisesExceptionForLicenseFileMissingDeveloperName(){
		try{
			File fileMissingDeveloperName = createLicenseFile(validEncryptedLicense,validIdentifier,"");
			DeveloperLicense.createLicense(fileMissingDeveloperName);
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	
	

}