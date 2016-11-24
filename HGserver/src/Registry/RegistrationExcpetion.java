package Registry;

public class RegistrationExcpetion extends Exception {
	
	public static final String usernameNotAvailable = "username not available";
	public static final String wrongCredentials = "wrong credentials";
	public static final String alreadyLogged = "already logged";
	public static final String updaterNotValid = "updater not valid";

	public RegistrationExcpetion()	{
	}

	public RegistrationExcpetion(String message)	{
		super(message);
	}
}
