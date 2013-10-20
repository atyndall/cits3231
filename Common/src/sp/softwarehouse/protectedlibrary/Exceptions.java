package sp.softwarehouse.protectedlibrary;

/**
 * Exceptions that could be thrown.
 */
public final class Exceptions {

	@SuppressWarnings("serial")
	public static class UnsuccessfulLinkingException extends Exception {
		public UnsuccessfulLinkingException(String s) {
			super(s);
		}
	}
	
	@SuppressWarnings("serial")
	public static class InvalidLicenseException extends Exception {}
	

}
