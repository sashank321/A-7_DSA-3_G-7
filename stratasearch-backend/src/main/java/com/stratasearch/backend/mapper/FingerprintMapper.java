package com.stratasearch.backend.mapper;

import com.stratasearch.backend.dto.CorpusFingerprintResponse;
import stratasearch.models.CorpusFingerprint;

import java.util.ArrayList;
import java.util.List;

// CorpusFingerprint (core) -> CorpusFingerprintResponse (DTO).
public final class FingerprintMapper {
    private FingerprintMapper() {}

    public static CorpusFingerprintResponse ToDto(CorpusFingerprint F) {
        CorpusFingerprintResponse D = new CorpusFingerprintResponse();
        D.corpusId = F.GetCorpusId();
        D.documentCount = F.GetDocumentCount();
        D.characterCount = F.GetCharacterCount();
        D.wordCount = F.GetWordCount();
        D.vocabularySize = F.GetVocabularySize();
        D.repeatedWordCount = F.GetRepeatedWordCount();
        D.longestWordLength = F.GetLongestWordLength();
        D.longestDocumentLength = F.GetLongestDocumentLength();
        D.averageWordLength = F.GetAverageWordLength();
        D.averageDocumentLength = F.GetAverageDocumentLength();
        D.averageSentenceLength = F.GetAverageSentenceLength();
        D.vocabularyRichness = F.GetVocabularyRichness();
        D.entropyEstimate = F.GetEntropyEstimate();
        D.duplicateScore = F.GetDuplicateScore();
        D.patternDensity = F.GetPatternDensity();
        D.repeatedPhraseRatio = F.GetRepeatedPhraseRatio();
        D.uniqueCharacterCount = F.GetUniqueCharacterCount();
        D.estimatedAlphabetSize = F.GetEstimatedAlphabetSize();
        D.corpusCategory = F.GetCorpusCategory();
        List<String> Tags = new ArrayList<>();
        for (int I = 0; I < F.GetCategoryTagCount(); I++) {
            Tags.add(F.GetCategoryTag(I));
        }
        D.categoryTags = Tags;
        return D;
    }
}
