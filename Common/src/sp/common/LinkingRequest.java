package sp.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sp.softwarehouse.protectedlibrary.DeveloperLicense;

public class LinkingRequest implements Serializable {
	private static final long serialVersionUID = 2316319439688449010L;
	
	/**
	 * TODO: Replace test values with correctly generated values
	 */
	final String TEST_LICENSE_NAME = "licenses/developer1-0.lic";
	
	private List<DeveloperLicense> licenseList;
	private List<String> libraryList;
	
	public LinkingRequest(){
//		this.license =  DeveloperLicense.fromStream(LinkingRequest.class.getResourceAsStream(TEST_LICENSE_NAME));
		this.libraryList = new ArrayList<String>();
		this.licenseList = new ArrayList<DeveloperLicense>();
	}
	
	public List<String> getLibraryList(){
		return libraryList;
	}
	
	public void addLibraries(List<String> library, List<DeveloperLicense> licenses) throws IllegalArgumentException {
		if (library.size() != licenses.size())
			throw new IllegalArgumentException("There must be the same number of licenses and libraries.");
		
		libraryList.addAll(library);
		licenseList.addAll(licenses);
	}
	
	public List<DeveloperLicense> getLicenses() {
		return licenseList;
	}
	
	public void add(String library, DeveloperLicense license){
		libraryList.add(library);
		licenseList.add(license);
	}
}
