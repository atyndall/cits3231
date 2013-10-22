package sp.softwarehouse.protectedlibrary;


import sp.softwarehouse.protectedlibrary.Exceptions.UnsuccessfulLinkingException;

public class RealLibLinker {

	public static <T> T getRealLib(String path) throws UnsuccessfulLinkingException {
		
		try {
			Class<T> cl =  (Class<T>) Class.forName(path);
			return cl.newInstance();
		} catch (ClassNotFoundException e) {
			
			throw new UnsuccessfulLinkingException("You have not successfully linked against the API");
			
		} catch (ClassCastException e) {
			
			throw new UnsuccessfulLinkingException("The API link has been corrupted");
			
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | SecurityException e) {
			
			throw new UnsuccessfulLinkingException("The API could not be initialized");
			
		}
	}
	
}
