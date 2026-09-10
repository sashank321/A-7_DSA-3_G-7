package com.stratasearch.backend.dto;

import java.util.List;

// Mirrors stratasearch.models.CorpusFingerprint as a REST contract.
public class CorpusFingerprintResponse {
    public String corpusId;
    public int documentCount;
    public int characterCount;
    public int wordCount;
    public int vocabularySize;
    public int repeatedWordCount;
    public int longestWordLength;
    public int longestDocumentLength;
    public double averageWordLength;
    public double averageDocumentLength;
    public double averageSentenceLength;
    public double vocabularyRichness;
    public double entropyEstimate;
    public double duplicateScore;
    public double patternDensity;
    public double repeatedPhraseRatio;
    public int uniqueCharacterCount;
    public int estimatedAlphabetSize;
    public String corpusCategory;
    public List<String> categoryTags;
}
