package app;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;

/**
 * BloomFilter implementation designed for a spelling checker application using
 * Java 8. This implementation leverages MD5 hash function from java.security.MessageDigest 
 * library and defaults String encoding to UTF-8 Charset. BloomFilter insertion and 
 * membership time complexity of O({@code numberOfHashes}) 
 * 
 * References: 
 * http://pages.cs.wisc.edu/~cao/papers/summary-cache/node8.html
 * https://github.com/google/guava/blob/master/guava/src/com/google/common/hash/BloomFilter.java
 * 
 * @author bayakubenko
 *
 */
public class BloomFilter {

	private BitSet bitset;

	private int totalBits;
	private int numberOfHashes;
	private int numberOfElements;
	private int expectedElementsTotal;
	private double falsePositiveProbability;

	private static final int SHIFT_BYTE_OFFSET = 8;
	private static final String DEFAULT_HASH_FUNCTION = "MD5";
	private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	// m: total bits
	// n: expected insertions
	// b: m/n, bits per insertion
	// p: expected false positive probability
	// 1) Optimal k = b * ln2
	// 2) p = (1 - e ^ (-kn/m))^k
	// 3) For optimal k: p = 2^(-k) ~= 0.6185^b
	// 4) For optimal k: m = -nlnp / ((ln2) ^ 2)

	// This is not thread safe
	private final MessageDigest md5;

	public BloomFilter(int expectedElementsTotal) {
		this.expectedElementsTotal = expectedElementsTotal;
		this.md5 = initializeHashFunction();
		this.numberOfElements = 0;
	}

	public BloomFilter(int expectedElementsTotal, int totalBits) {
		this(expectedElementsTotal);
		
		this.totalBits = totalBits;
		
		this.bitset = new BitSet(this.totalBits);
		this.numberOfHashes = computeOptimalNumberOfHashes(expectedElementsTotal, totalBits);
		this.falsePositiveProbability = computeFalsePositiveProbability(expectedElementsTotal, totalBits, this.numberOfHashes);
	}

	public BloomFilter(int expectedElementsTotal, double falsePositiveProbability) {
		this(expectedElementsTotal);

		this.totalBits = computeOptimalBitSetSize(expectedElementsTotal, falsePositiveProbability);
				
		this.numberOfHashes = computeOptimalNumberOfHashes(expectedElementsTotal, this.totalBits);
		
		this.bitset = new BitSet(this.totalBits);
		this.falsePositiveProbability = falsePositiveProbability;
		
	}
	
	private MessageDigest initializeHashFunction() {
		// Keta: Generates a fairly long hash (such as MD5) and then take your smaller
		// hash values by extracting sequences of bits from the result
		try {
			return MessageDigest.getInstance(DEFAULT_HASH_FUNCTION);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Can not initialize " + DEFAULT_HASH_FUNCTION);
		}
	}

	/**
	 * Computes optimal number of hashes to be used in the word hashing logic.
	 * Optimal k = b * ln2 where b = m/n
	 * 
	 * m = total bits n = expected insertions
	 *
	 * @param totalElements
	 * @param totalBits
	 * @return Returns optimal number of hash functions for {@code totalElements}
	 *         and {@code totalBits}
	 */
	private int computeOptimalNumberOfHashes(int expectedElements, int totalBits) {
		double b = totalBits / (double) expectedElements;
		return (int) Math.round(b * Math.log(2));
	}

	/**
	 * False positive probability p = (1 - e ^ (-kn/m))^k
	 * 
	 * @return returns the false positive probability for given
	 *         {@code totalElements, totalBits, numberOfHashes}
	 */
	private double computeFalsePositiveProbability(int expectedElements, int totalBits, int numberOfHashes) {
		return Math.pow((1 - Math.exp(-1 * numberOfHashes * (double) (expectedElements / (double) totalBits))),
				numberOfHashes);
	}

	/**
	 * Optimal m = -nlnp / ((ln2) ^ 2)
	 * 
	 * Return the optimal size of the Bitset used in the Bloom Filter.
	 * 
	 * @param numberOfInsertions
	 * @param optimalProbability
	 * @return
	 */
	private int computeOptimalBitSetSize(int expectedElements, double optimalProbability) {
		long size = Math.round((-1 * expectedElements * Math.log(optimalProbability)) / (Math.log(2) * Math.log(2)));

		return size <= Integer.MAX_VALUE ? (int)size : Integer.MAX_VALUE;
	}

	/**
	 * Generate {@code numberOfHashes} hashes by leveraging 
	 * java.security.MessageDigest MD5 hashing algorithm.
	 * @param word
	 * @return An array of 4 byte hashes in Integer type.
	 */
	private int[] hashes(String word) {

		int[] hashes = new int[this.numberOfHashes];

		for (int i = 0; i < this.numberOfHashes; i++) {
			// Perform 'Salting' technique by appending
			// an integer to the word to be hashed by MD5
			String tmp = word + Integer.toString(i);

			// Returns a 16 byte hash
			byte[] bytes = md5.digest(tmp.getBytes(DEFAULT_CHARSET));

			// Use remainder hashing method to always return
			// integer < this.totalBits
			hashes[i] = getInteger(bytes) % this.totalBits;			
		}

		return hashes;
	}

	 
	/**
	 * Extract first 4 bytes (index 0 to 3) of the
	 * byte array and stores as a positive integer type. 
	 * If length of the array is less than 4, then stores
	 * all bytes in an integer. The method returns 0
	 * if bytes array is empty,
	 *
	 * @param bytes
	 * @return return integer equivalent to the first 4 bytes
	 * of the input array
	 */
	private int getInteger(byte[] bytes) {
		int l = bytes.length < 4 ? bytes.length : 4;
		
		int val = 0;

		// Get the first 4 bytes (integer) of the hash
		for (int i = 0; i < l; i++) {
			val = val << SHIFT_BYTE_OFFSET; // shift by a byte
			// & with 0xFF (byte) to get unsigned result since
			// primitives are mostly signed
			val = val | (bytes[i] & 0xFF);
		}

		return Math.abs(val);
	}
	
	/**
	 * Hashes word {@code numberOfHashes} of times and adds each hash to the filter.
	 * 
	 * @param string
	 */
	public void add(String word) {

		int[] hashes = hashes(word);

		// Store this.numberOfHashes of hashes in bitset
		for (int i = 0; i < hashes.length; i++) {
			this.bitset.set(hashes[i]);
		}

		this.numberOfElements++;
	}

	/**
	 * Hashes word {@code numberOfHashes} of times and checks if each index equivalent
	 * to the hash output is true in the bitset.
	 * 
	 * @param string
	 * @return Returns false if any of the hashes are not found, other wise returns
	 *         true.
	 */
	public boolean mightContain(String word) {

		int[] hashes = hashes(word);

		for (int i = 0; i < hashes.length; i++) {

			// if any values are false, the logic
			// guarantees that we have not seen this
			// 'word' yet.
			if (!this.bitset.get(hashes[i]))
				return false;
		}

		return true;
	}

	public int getTotalBits() {
		return totalBits;
	}

	public int getNumberOfHashes() {
		return numberOfHashes;
	}

	public int getNumberOfElements() {
		return numberOfElements;
	}

	public double getFalsePositiveProbability() {
		return falsePositiveProbability;
	}

	public int getExpectedElementsTotal() {
		return expectedElementsTotal;
	}

	@Override
	public String toString() {
		return "BloomFilter [totalBits=" + totalBits + ", numberOfHashes=" + numberOfHashes + ", expectedElements="
				+ expectedElementsTotal + ", falsePositiveProbability=" + falsePositiveProbability + "]";
	}

}
