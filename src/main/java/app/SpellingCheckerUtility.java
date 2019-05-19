package app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SpellingCheckerUtility {
	
	private static final char CHAR_OFFSET = 'a'; // int = 97
	private static final int TOTAL_ALPHABET_CHARACTERS = 26;

	/**
	 * Uses Buffered Reader with default buffer size to read dictionary file and
	 * update {@code dictionary} bits.
	 * @param sourcePath
	 * @param filter
	 * @throws IOException if the file can not be read.
	 */
	public static void loadDictionary(String sourcePath, BloomFilter dictionary) throws IOException {

		if (sourcePath == null)
			throw new RuntimeException("Source path can not be null");

		long start_ts = System.currentTimeMillis();

		try (BufferedReader br = new BufferedReader(new FileReader(sourcePath))) {

			String line;
			while ((line = br.readLine()) != null) {

				dictionary.add(line);

			}
			// Print file processing latency.
			System.out.println("Processed input file in " + getFormattedLatency(start_ts));
		}
	}
	
	/**
	 * Using Random Int generator, creates random words using English alphabet by using
	 * standard ASCII.
	 * @param numberOfWords
	 * @param minLength
	 * @param maxLength
	 * @return a list of randomly generated words.
	 */
	public static String[] randomWordGenerator(int numberOfWords, int minLength, int maxLength) {
		long start_ts = System.currentTimeMillis();
		String[] randomStrings = new String[numberOfWords];
		Random random = new Random();
		
		for (int i = 0; i < numberOfWords; i++) {
			
			// get value from 0 to exclusive bound and add offset.
			int size = Math.max(minLength, random.nextInt(maxLength));
			char[] generatedWord = new char[size];
			
			for (int j = 0; j < generatedWord.length; j++) {
				generatedWord[j] = (char) (CHAR_OFFSET + random.nextInt(TOTAL_ALPHABET_CHARACTERS));
			}
			
			randomStrings[i] = new String(generatedWord);
		}
		System.out.println("Generated random words in " + getFormattedLatency(start_ts));
		return randomStrings;
	}

	public static String getFormattedLatency(long startTimeMillis) {

		long duration = System.currentTimeMillis() - startTimeMillis;

		long hours = TimeUnit.MILLISECONDS.toHours(duration);
		duration -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
		duration -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
		duration -= TimeUnit.SECONDS.toMillis(seconds);

		StringBuilder sb = new StringBuilder("Total time (hh:mm:ss:SSS): [");
		sb.append(hours).append(":").append(minutes).append(":").append(seconds).append(":").append(duration)
				.append("]");

		return sb.toString();
	}

}
