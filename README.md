# spell-checker
Java implementation of memory efficient spelling checker. The implementation is a solution to the coding exercise described in http://codekata.com/kata/kata05-bloom-filters/ 

## Implementation
The solution leverages Java 8 JRE libraries to implement a Bloom Filter designed to store string occurrences from a dictionary provided by a local input file. The Bloom Filter implementation leverages the MD5 hash function from java.security.MessageDigest library and provides an interfaces to `add` and `check` membership of an input word.

### Testing
The solution is tested in `SpellingCheckerAnalysis.java` class. This class loads a dictionary of words into a `Set` and cross references the results of the `SpellingChecker.java` class tested against a set of randomly generated words.

A constructed Bloom Filter with false positive probability of `.01` was tested against `10,000` randomly generated words of length between 0-6 characters. Minimum length of 3 was set on a few test runs to remove uninteresting words. However, this technique did not impact the results drastically. The result of the tests yielded `~99%` accuracy which is in line with `.01` probability of false positives.

### References
http://pages.cs.wisc.edu/~cao/papers/summary-cache/node8.html
https://github.com/google/guava/blob/master/guava/src/com/google/common/hash/BloomFilter.java

