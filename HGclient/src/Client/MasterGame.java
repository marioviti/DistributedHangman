package Client;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MasterGame extends Thread {
	
	public static String gameOver = "GAME OVER";
	public static String youWin = "YOU WIN";
	
	private JSONParser parser;
	private GuessMessage gmessage;
	private MulticastService udpReceiver;
	private SecretString secret;
	private boolean running;
	
	private JSONObject guessRecord;

	/**
	 * Master Game session handler
	 * 
	 * @param iface
	 * @param gamingPassWord
	 * @param gamingMulticastAddress
	 * @param masterGameSecretWord
	 * @throws SocketException
	 * @throws IOException
	 */
	public MasterGame(String iface, String gamingPassWord, String gamingMulticastAddress, String masterGameSecretWord) throws SocketException, IOException {
		this.udpReceiver = new MulticastService(iface,gamingMulticastAddress,4231);
		this.parser = new JSONParser();
		this.secret = new SecretString(masterGameSecretWord);
		gmessage = new GuessMessage(gamingPassWord);
		guessRecord = new JSONObject();
	}
	
	@Override
	public void run() {
		running = true;
		String received;
		String update;
		try {
			udpReceiver.join();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		log("running...");
			
		while(running) {
			try {
			iLog("waiting for guesses");
			if((received = udpReceiver.receive())!=null) {
				log("decryption phase...");
				gmessage.checkGuess(received);
				if(isNewGuess(gmessage)) {
					secret.checkGuess((String)gmessage.get(GuessMessage.fieldguess));
					iLog("received...");
					iLog(gmessage.toString());
					log("encryption phase...");
					update=gmessage.fillUpdate(secret.getStateWord(),secret.getCounter()+"");
					iLog(secret.getStateWord());
					log("sending...");
					log(update);
					udpReceiver.send(update);
					if (secret.getStateWord().equals(MasterGame.youWin)) {
						iLog("Guessers Win");
						running = false;
					}
					if (secret.getStateWord().equals(MasterGame.gameOver)) {
						iLog("Master Win");
						running = false;
					}
				}
			}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (EncryptionOperationNotPossibleException e) {
				e.printStackTrace();
			}
		}		
	}
	
	private boolean isNewGuess(GuessMessage gmessage2) throws ParseException {
		String k = (String) gmessage2.get(GuessMessage.fieldguesserID);
		String guessID = (String) gmessage2.get(GuessMessage.fieldguessID);
		JSONObject prevGuess = (JSONObject)guessRecord.get(k);
		if ( prevGuess == null || !prevGuess.get(GuessMessage.fieldguessID).equals(guessID)) {
			guessRecord.put(k,gmessage2.getMessage());
			return true;
		}
		return false;
	}

	private void check(String received) throws ParseException, EncryptionOperationNotPossibleException {
		secret.checkGuess((String)gmessage.checkGuess(received));
	}

	public void log(String s) {
		System.err.println(this.getClass().getName()+ ": " + s);
	}

	public void iLog(String s) {
		System.out.println(this.getClass().getName()+ ": " + s);
	}
	
	private class SecretString {
		
		private String secretWord;
		private String stateWord;
		private int numOfLetters;
		private char[] state;
		private char[] secret;
		private int counter;
		private int limit;
		
		public SecretString(String secretWord) {
			this.counter = 0;
			this.secretWord = secretWord;
			this.numOfLetters = secretWord.length();
			this.limit = 4 * numOfLetters;
			state = new char[numOfLetters];
			for(int i=0; i<numOfLetters; i++)
				state[i] = '*';
			secret = secretWord.toCharArray();
			stateToString();
		}

		public String checkGuess(String guess) {
			if(this.stateWord.equals(youWin) || this.stateWord.equals(gameOver) )
				return getStateWord();
			char guessc = guess.charAt(0);
			boolean guessed = false;
			for(int i = 0; i< numOfLetters; i++)
				if(secret[i] == guessc) {
					state[i] = guessc;
					guessed = true;
				}
			if(!guessed) 
				counter++;
			stateToString();
			if(counter == limit) 
				this.stateWord = MasterGame.gameOver;
			if(this.stateWord.equals(secretWord))
				this.stateWord = MasterGame.youWin;
			return getStateWord();
		}
		
		private void stateToString() {
			stateWord = String.valueOf(state);
		}

		public String getStateWord() {
			return stateWord;
		}
		
		public int getCounter() {
			return counter;
		}
	}
}