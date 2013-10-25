package sp.exceptions;

public class InvalidDeveloperLicenseFileException extends Exception{
	private static final long serialVersionUID = 8167172835456988315L;

	public InvalidDeveloperLicenseFileException(String message){
		super(message);
	}
}
