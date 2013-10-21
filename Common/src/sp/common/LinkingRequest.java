package sp.common;

import java.io.Serializable;
import java.util.ArrayList;

import sp.softwarehouse.protectedlibrary.DeveloperLicense;

public class LinkingRequest implements Serializable{
	private static final long serialVersionUID = 2316319439688449010L;
	
	/**
	 * TODO: Replace test values with correctly generated values
	 */
	final String TEST_LICENSE_NAME = "licenses/developer1-0.lic";
	
	private DeveloperLicense license;
	private ArrayList<String> libraryList;
	
	public LinkingRequest(){
//		this.license =  DeveloperLicense.fromStream(LinkingRequest.class.getResourceAsStream(TEST_LICENSE_NAME));
		this.libraryList = new ArrayList<String>();
	}
	
	public ArrayList<String> getLibraryList(){
		return libraryList;
	}
	
	public void addLibraries(ArrayList<String> library){
		libraryList.addAll(library);
	}
	
	public String getLicenseHolder(){
		return license.getDeveloperName();
	}
	
	public String getLicenseId(){
		return license.getIdentifier();
	}
	
	public void add(String library){
		libraryList.add(library);
	}
}
