package Registry;

public class GameStateException extends Exception {
	
	public static final String gameIsFull="game is full";
	public static final String gameIsNotExtistent="game is not existent";
	public static final String lobbyCorrupted="lobby is corrupted";
	public static String maxGameLimitReached ="lobby is full, please wait";

	public GameStateException()	{
	}

	public GameStateException(String message)	{
		super(message);
	}
}

