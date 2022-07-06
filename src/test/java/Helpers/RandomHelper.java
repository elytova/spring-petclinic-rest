package Helpers;

import java.util.Random;

public class RandomHelper {

	public static String randomAlphabetString(int size) {
		String AlphabetString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvxyz";
		StringBuilder sb = new StringBuilder(size);
		for (int i = 0; i < size; i++) {
			int index = (int) (AlphabetString.length() * Math.random());
			sb.append(AlphabetString.charAt(index));
		}
		return sb.toString();
	}

	public static int randomNumeric() {
		Random randI = new Random();
		return randI.ints(100, 1000).findAny().getAsInt();
	}

}
