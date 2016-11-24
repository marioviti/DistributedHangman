package Client;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GuessMessage {
	
	/**
	 * message
	 * {
	 * 	0:update
	 * 	1:counter
	 * 	2:guesserID
	 * 	3:guessID
	 * 	4:guess
	 * }
	 */
	
	public static final String fieldupdate = "0", fieldCounter = "1", fieldguesserID = "2", fieldguessID = "3", fieldguess = "4";
	private JSONParser parser;
	private JSONObject message;
	private String gamingPassWord;
	private String guesserID;
	public String update,counter;
	private int guessID;
	private boolean acked;
	
	private StandardPBEStringEncryptor encryptor;
	
	public GuessMessage() {
		init();
	}
	
	public GuessMessage(String gamingPassWord) {
		this.gamingPassWord = gamingPassWord;
		encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword(this.gamingPassWord);
		init();
	}

	public GuessMessage(String gamingPassWord, String guesserID) {
		guessID = 0;
		this.gamingPassWord = gamingPassWord;
		this.guesserID=guesserID;
		encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword(this.gamingPassWord);
		init();
	}

	private void init() {
		parser = new JSONParser();
		message = new JSONObject();
		message.put(fieldupdate, null);
		message.put(fieldCounter, null);
		message.put(fieldguesserID, guesserID);
		message.put(fieldguessID, null);
		message.put(fieldguess, null);
	}

	public String fillGuess(String input) {
		message.put(fieldupdate, null);
		message.put(fieldCounter, null);
		message.put(fieldguessID, (++guessID)+"");
		message.put(fieldguess, input);
		acked = false;
		return encryptor.encrypt(message.toString());
	}
	
	public String fillUpdate(String update, String counter) {
		message.put(fieldupdate, update);
		message.put(fieldCounter, counter);
		return encryptor.encrypt(message.toString());
	}

	private JSONObject decryptAndParse(String message) throws ParseException {
		String decrypted = encryptor.decrypt(message);
		JSONObject copy = (JSONObject) parser.parse(decrypted);
		return copy;
	}
	
	/**
	 * method used by guessers to decrypt update messages
	 * @param encrypted
	 * @return
	 * @throws ParseException
	 * @throws EncryptionOperationNotPossibleException
	 */
	public String checkUpdate(String encrypted) throws ParseException, EncryptionOperationNotPossibleException {
		JSONObject copy = decryptAndParse(encrypted);
		if ((update = (String) copy.get(fieldupdate))!=null && (counter = (String)copy.get(fieldCounter)) != null) {
			if( copy.get(fieldguesserID).equals(guesserID) ) {
				acked = true;
				message = copy;
			}
			return update;
		}
		return null;
	}
	
	/**
	 * method used by masters to decrypt a guess
	 * @param encrypted
	 * @return
	 * @throws ParseException
	 * @throws EncryptionOperationNotPossibleException
	 */
	public String checkGuess(String encrypted) throws ParseException, EncryptionOperationNotPossibleException {
		JSONObject copy = decryptAndParse(encrypted);
		if ((update = (String) copy.get(fieldupdate)) != null && (counter = (String)copy.get(fieldCounter)) != null)
			throw new ParseException(0);
		message = copy;
		return (String)message.get(fieldguess);
	}
	
	public JSONObject getMessage() {
		return message;
	}
	
	public Object get(Object key) {
		return message.get(key);
	}

	public boolean acked() {
		return acked;
	}
	
	@Override
	public String toString() {
		return message.toString();
	}
}
