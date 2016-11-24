package Registry;

import java.util.Random;

public class PasswordGenerator {
	
	private static Random rng = new Random();
	
	public static int getPassword() {
		synchronized(rng) {
			return rng.nextInt(Integer.SIZE - 1);
		}
	}
}
