import type { LabFile } from "../stores/labStore";

export const SAMPLE_DOCUMENTS: LabFile[] = [
  {
    name: "Knuth1977_FastPatternMatching.txt",
    content: `Fast Pattern Matching in Strings by Donald E. Knuth, James H. Morris, and Vaughan R. Pratt.
An algorithm is presented which searches for all occurrences of a word of length m within a main text of length n in time proportional to m + n.
The Knuth-Morris-Pratt (KMP) method constructs an auxiliary table, commonly denoted as the longest prefix suffix (LPS) array, which stores the lengths of the longest proper prefix of the pattern that matches a proper suffix.
This enables deterministic linear searching without back-tracking the text pointer.
Comparisons with Karp-Rabin hashing (Rabin1981_RandomizedPattern) demonstrate that KMP guarantees worst-case linear performance O(n + m), whereas hashing exhibits probabilistic collisions.
Subsequent studies in Boyer-Moore and SuffixArray (Manber1993_SuffixArrays) extend this research to multi-query indexing structures and suffix trees.`,
  },
  {
    name: "Rabin1981_RandomizedPattern.txt",
    content: `Efficient Randomized Pattern-Matching Algorithms by Michael O. Rabin and Richard M. Karp.
We present an algorithm for string matching based on fingerprinting and polynomial rolling hash functions.
Given a pattern P and text T, we compute hash values modulo a large prime. If the hash values match, a character-by-character verification step verifies whether a true match or collision has occurred.
In contrast to Knuth1977_FastPatternMatching, Karp-Rabin requires minimal auxiliary space O(1) beyond pattern storage and handles 2D matrix matching gracefully.
Furthermore, in multi-pattern dictionaries, Aho-Corasick (Aho1975_EfficientStringMatching) provides deterministic linear throughput, while Karp-Rabin serves as an efficient single-pass filter.`,
  },
  {
    name: "Aho1975_EfficientStringMatching.txt",
    content: `Efficient String Matching: An Aid to Bibliographic Search by Alfred V. Aho and Margaret J. Corasick.
This paper describes a simple and efficient algorithm to locate all occurrences of any of a finite set of keywords in an arbitrary text string.
The algorithm constructs a deterministic finite-state automaton from the set of keywords and then processes the text string in a single linear pass.
The preprocessing constructs a Trie with failure links, enabling transitions directly to the longest viable prefix upon mismatch.
Its preprocessing cost is proportional to the sum of keyword lengths, and the search time is linear O(n) independent of dictionary size.
Compared to Knuth1977_FastPatternMatching which checks each pattern independently in O(k * (n + m)), the Aho-Corasick automaton operates in optimal time O(n + sum_len + matches).
For indexing large static corpora, SuffixArray methods (Manber1993_SuffixArrays) provide an orthogonal approach through binary search over lexicographically sorted suffixes.`,
  },
  {
    name: "Manber1993_SuffixArrays.txt",
    content: `Suffix Arrays: A New Method for On-Line String Searches by Udi Manber and Gene W. Myers.
We introduce a new data structure called the Suffix Array, which is an array of integers providing the starting positions of suffixes of a string sorted in lexicographical order.
Suffix arrays provide the power of suffix trees while using three to five times less space.
Using an auxiliary Longest Common Prefix (LCP) array, searching for a pattern P of length m in a corpus of length n requires O(m + log n) time.
Unlike online search algorithms like Knuth1977_FastPatternMatching, Rabin1981_RandomizedPattern, and Aho1975_EfficientStringMatching which rescan the corpus for each query, the Suffix Array index is constructed once in O(n log n) and amortizes query cost across thousands of searches.
Linear-time construction approaches (Karkkainen2003_LinearSuffixArrays) further eliminate the O(n log n) bottleneck.`,
  },
  {
    name: "Karkkainen2003_LinearSuffixArrays.txt",
    content: `Simple Linear Work Suffix Array Construction by Juha Karkkainen and Peter Sanders.
We present a direct algorithm for suffix array construction that achieves O(n) worst-case time complexity without using suffix trees.
The DC3 skew algorithm recursively sorts suffixes at positions i mod 3 != 0, constructs an auxiliary LCP array, and merges with suffixes at positions i mod 3 == 0 in linear time.
Benchmarks comparing against Manber1993_SuffixArrays show substantial speedups on large repetitive corpora.
Together with Knuth1977_FastPatternMatching and Aho1975_EfficientStringMatching, suffix arrays provide the complete foundation for modern index-based full text search.`,
  },
];

export const SAMPLE_PATTERNS = "pattern\nalgorithm\nsuffix\nlinear\nsearch";

