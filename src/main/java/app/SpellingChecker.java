package app;

import java.io.IOException;

/**
 * Provides ability to check spelling against a memory efficient dictionary. 
 * Memory efficiency trade off impacts the accuracy of false positive response 
 * from {@code check(String word)} interface. The dictionary words are loaded 
 * from a configurable local path.
 * 
 * @author bayakubenko
 *
 */
public class SpellingChecker {
	private BloomFilter dictionary = null;
	
	private static final int DEFAULT_NUMBER_OF_ELEMENTS = 260000;
	private static final double DEFAULT_FALSE_POSITIVE_PROBABILITY = .01;
	
	/**
	 * Populates a provided bloom filter with words extracted from local file 
	 * {@code dictionarySourcePath}.
	 * @param dictionarySourcePath
	 * @param cache
	 * @throws IOException
	 */
	public SpellingChecker(String dictionarySourcePath, BloomFilter cache) throws IOException {
		this.dictionary = cache;
		SpellingCheckerUtility.loadDictionary(dictionarySourcePath, this.dictionary);
	}

	/**
	 * Populates a bloom filter with words extracted from local file {@code dictionarySourcePath}.
	 * The bloom filter is configured with default values {@code DEFAULT_NUMBER_OF_ELEMENTS} and 
	 * {@code DEFAULT_NUMBER_OF_BITS}.
	 * @param dictionarySourcePath
	 * @throws IOException Exception is thrown if the local input file can not be processed.
	 */
	public SpellingChecker(String dictionarySourcePath) throws IOException {
		//this(dictionarySourcePath, new BloomFilter(DEFAULT_NUMBER_OF_ELEMENTS, DEFAULT_NUMBER_OF_BITS));
		this(dictionarySourcePath, new BloomFilter(DEFAULT_NUMBER_OF_ELEMENTS, DEFAULT_FALSE_POSITIVE_PROBABILITY));
	}
	
	/**
	 * Check the spelling of a word.
	 * @param word
	 * @return true if the word is spelled correctly with probability 
	 * of false positive.
	 */
	public boolean check(String word) {
		return this.dictionary.mightContain(word);
	}
	
	/**
	 * Get the total size of words added to the dictionary.
	 * @return
	 */
	public int getDictionarySize() {
		return this.dictionary.getNumberOfElements();
	}
	
	/**
	 * Get the probability of a word contained in the dictionary
	 * but never added.
	 * @return
	 */
	public double getFalsePositiveProbability() {
		return this.dictionary.getFalsePositiveProbability();
	}

}
