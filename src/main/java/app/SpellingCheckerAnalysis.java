package app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Test performance and accuracy of the SpellChecker.java class by loading full
 * dictionary into memory and cross-referencing {@code check(String word)}
 * response again a Set<String>
 * 
 * @author bayakubenko
 *
 */
public class SpellingCheckerAnalysis {

	// Very expensive. Used for testing bloom filter accuracy.
	private Set<String> dictionary = null;

	public SpellingCheckerAnalysis() {
		this.dictionary = new HashSet<String>();
	}

	/**
	 * Load the list of words into memory as a Set<String>
	 * 
	 * @param sourcePath Path to a file containing list of words.
	 * @throws IOException
	 */
	public void loadDictionary(String sourcePath) throws IOException {

		if (sourcePath == null)
			throw new RuntimeException("Source path can not be null");

		long start_ts = System.currentTimeMillis();

		try (BufferedReader br = new BufferedReader(new FileReader(sourcePath))) {

			String line;
			while ((line = br.readLine()) != null) {

				dictionary.add(line);

			}

			// Print file processing latency.
			System.out.println("Processed input file in " + SpellingCheckerUtility.getFormattedLatency(start_ts));
		}
	}

	public void analyze(int totalTestWords, String dictionaryPath) {
		SpellingChecker checker = null;

		try {
			checker = new SpellingChecker(dictionaryPath);
			System.out.println("Spelling checker false positive probability:" + checker.getFalsePositiveProbability());
			System.out.println("Loaded " + checker.getDictionarySize() + " words.");

			// Load dictionary to test against.
			loadDictionary(dictionaryPath);

		} catch (IOException e) {
			System.err.println("Error loading dictionary" + e);
			System.exit(1);
		}
		int falsePositivesCount = 0;

		// Generate random words between 3 and 6 character size. The smaller the test
		// words, the
		// higher probability the generated word is an actual word.
		String[] randWords = SpellingCheckerUtility.randomWordGenerator(totalTestWords, 3, 6);

		// for each random generated word, check if it exists in the dictionary.
		for (int i = 0; i < randWords.length; i++) {

			if (checker.check(randWords[i])) {
				if (!this.dictionary.contains(randWords[i])) {
					falsePositivesCount++;
				}
			}
		}

		System.out.println(
				"Spell checker accuracy: " + ((totalTestWords - falsePositivesCount) / (double) totalTestWords) * 100);
	}

	public static void main(String[] args) {
		SpellingCheckerAnalysis analys = new SpellingCheckerAnalysis();
		// Test against 10,000 words.
		analys.analyze(10000, "/usr/share/dict/web2");
	}

}
