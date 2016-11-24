package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.json.simple.JSONObject;

public class GuesserGame extends Thread {
		
	private boolean running;
	private String input;
	private BufferedReader in;
	
	private MulticastService udpSender;
	private GuesserHelper helper;
	private GuessMessage gmessage;
	
	private int deltaSleep = 200;
	private int timeOut = 10 * deltaSleep;
	private int resendTrialMax = 100;
	private int resentTrial = 0;
	private long currentTime;
	private int guesserNumOfLetters;

	/**
	 * Guessing game session handler
	 * 
	 * @param ifaceIP
	 * @param gamingPassWord
	 * @param gamingMulticastAddress
	 * @param guesserNumOfLetters
	 * @param user
	 * @throws SocketException
	 * @throws IOException
	 */
	public GuesserGame(String ifaceIP, String gamingPassWord, String gamingMulticastAddress, int guesserNumOfLetters, String user) throws SocketException, IOException {
		this.udpSender = new MulticastService(ifaceIP, gamingMulticastAddress,4231);
		in = new BufferedReader(new InputStreamReader(System.in));
		this.gmessage = new GuessMessage(gamingPassWord,user);
		this.helper = new GuesserHelper(udpSender,gmessage);
		this.guesserNumOfLetters = guesserNumOfLetters;
	}
	
	@Override
	public void run() {
		log("starting...");
		running = true;
		String startStatus = "";
		for(int i =0; i<guesserNumOfLetters; i++) {
			startStatus+="*";
		}
		try {
			this.udpSender.join();
			this.helper.start();
			iLog(startStatus);
			iLog("make a guess");
			log("running...");
			while(running)
				execute();
		
		} catch (IOException e) {
			e.printStackTrace();
			this.helper.interrupt();
		} catch (InterruptedException e) {
			e.printStackTrace();
			this.helper.interrupt();
		}
		iLog("game has ended...");
		log("stopping...");
	}

	private void execute() throws IOException, InterruptedException {
		if((input = in.readLine())!=null) {
			if( input.length()==1 ) {
				log("encryption phase...");
				String guess = gmessage.fillGuess(input);
				log("sending...");
				log(guess);
				udpSender.send(guess);
				currentTime = System.currentTimeMillis();
				iLog("sending...");
				ackLoop(guess);
				if(gmessage.update.equals(MasterGame.youWin) || gmessage.update.equals(MasterGame.gameOver)) {
					iLog(gmessage.update);
					running = false;
				}
			}else
			iLog("guess must be one char");
		}
	}
	
	private void ackLoop(String guess) throws InterruptedException, UnknownHostException, IOException {
		resentTrial = 0;
		while(true) {
			sleep(deltaSleep);
			if(gmessage.acked())
				break;
			if(timeIsOut()) {
				log("resending...");
				udpSender.send(guess);
				currentTime = System.currentTimeMillis();
				resentTrial++;
			}
			
			// long timout
			if(tooManyTrials()) {
				iLog("too many trials...");
				log("too many trials...");
				running = false;
				break;
			}
		}	
	}

	private boolean timeIsOut() {
		return System.currentTimeMillis() - currentTime >= timeOut;
	}

	private boolean tooManyTrials() {
		return resentTrial >= resendTrialMax;
	}
	
	public void log(String s) {
		System.err.println(this.getClass().getName()+ ": " + s);
	}
	
	public void iLog(String s) {
		System.out.println(this.getClass().getName()+ ": " + s);
	}
}