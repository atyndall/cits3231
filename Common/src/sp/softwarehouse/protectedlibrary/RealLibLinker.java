package sp.softwarehouse.protectedlibrary;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import sp.softwarehouse.protectedlibrary.Exceptions.InvalidLicenseException;
import sp.softwarehouse.protectedlibrary.Exceptions.UnsuccessfulLinkingException;

public class RealLibLinker {

	@SuppressWarnings("unchecked")
	public static <t> t getRealLib(Class<t> interfaceName, String path, DeveloperLicense lic) throws UnsuccessfulLinkingException, InvalidLicenseException {
		
		try {
			Class<t> cl = (Class<t>) Class.forName(path);
			Class<?>[] params = {DeveloperLicense.class};
			Constructor<t> co = cl.getConstructor(params);
			return co.newInstance(lic);
			
		} catch (ClassNotFoundException e) {
			
			throw new UnsuccessfulLinkingException("You have not successfully linked against the API");
			
		} catch (ClassCastException e) {
			
			throw new UnsuccessfulLinkingException("The API link has been corrupted");
			
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | NoSuchMethodException | SecurityException e) {
			
			throw new UnsuccessfulLinkingException("The API could not be initialized");
			
		} catch (InvocationTargetException e) {
			
			if (e.getCause() instanceof InvalidLicenseException) {
		        throw (InvalidLicenseException) e.getCause();
		    } else {
		    	throw new UnsuccessfulLinkingException("Something odd happened");
		    }
		}
	}
	
}
