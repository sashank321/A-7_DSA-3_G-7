package stratasearch.models;

// Immutable corpus fingerprint produced by CorpusProfiler.
public class CorpusFingerprint {
    private final String CorpusId;
    private final int WordCount;
    private final int CharacterCount;
    private final int VocabularySize;
    private final int RepeatedWordCount;
    private final int LongestWordLength;
    private final double AverageWordLength;
    private final double AverageSentenceLength;
    private final double VocabularyRichness;
    private final double EntropyEstimate;
    private final double DuplicateScore;
    private final double PatternDensity;
    private final String CorpusCategory;
    private final String[] CategoryTags;
    private final int DocumentCount;
    private final int LongestDocumentLength;
    private final double AverageDocumentLength;
    private final double RepeatedPhraseRatio;
    private final int UniqueCharacterCount;
    private final int EstimatedAlphabetSize;

    public CorpusFingerprint(String CorpusId,
                             int WordCount,
                             int CharacterCount,
                             int VocabularySize,
                             int RepeatedWordCount,
                             int LongestWordLength,
                             double AverageWordLength,
                             double AverageSentenceLength,
                             double VocabularyRichness,
                             double EntropyEstimate,
                             double DuplicateScore,
                             double PatternDensity,
                             String CorpusCategory,
                             String[] CategoryTags,
                             int DocumentCount,
                             int LongestDocumentLength,
                             double AverageDocumentLength,
                             double RepeatedPhraseRatio,
                             int UniqueCharacterCount,
                             int EstimatedAlphabetSize) {
        this.CorpusId = CorpusId;
        this.WordCount = WordCount;
        this.CharacterCount = CharacterCount;
        this.VocabularySize = VocabularySize;
        this.RepeatedWordCount = RepeatedWordCount;
        this.LongestWordLength = LongestWordLength;
        this.AverageWordLength = AverageWordLength;
        this.AverageSentenceLength = AverageSentenceLength;
        this.VocabularyRichness = VocabularyRichness;
        this.EntropyEstimate = EntropyEstimate;
        this.DuplicateScore = DuplicateScore;
        this.PatternDensity = PatternDensity;
        this.CorpusCategory = CorpusCategory;
        this.CategoryTags = CategoryTags;
        this.DocumentCount = DocumentCount;
        this.LongestDocumentLength = LongestDocumentLength;
        this.AverageDocumentLength = AverageDocumentLength;
        this.RepeatedPhraseRatio = RepeatedPhraseRatio;
        this.UniqueCharacterCount = UniqueCharacterCount;
        this.EstimatedAlphabetSize = EstimatedAlphabetSize;
    }

    public String GetCorpusId() { return CorpusId; }
    public int GetWordCount() { return WordCount; }
    public int GetCharacterCount() { return CharacterCount; }
    public int GetVocabularySize() { return VocabularySize; }
    public int GetRepeatedWordCount() { return RepeatedWordCount; }
    public int GetLongestWordLength() { return LongestWordLength; }
    public double GetAverageWordLength() { return AverageWordLength; }
    public double GetAverageSentenceLength() { return AverageSentenceLength; }
    public double GetVocabularyRichness() { return VocabularyRichness; }
    public double GetEntropyEstimate() { return EntropyEstimate; }
    public double GetDuplicateScore() { return DuplicateScore; }
    public double GetPatternDensity() { return PatternDensity; }
    public String GetCorpusCategory() { return CorpusCategory; }

    public String[] GetCategoryTags() { return CategoryTags; }
    public int GetCategoryTagCount() { return CategoryTags.length; }
    public String GetCategoryTag(int I) { return CategoryTags[I]; }
    public boolean HasCategoryTag(String Tag) {
        for (int I = 0; I < CategoryTags.length; I++) {
            if (CategoryTags[I].equals(Tag)) {
                return true;
            }
        }
        return false;
    }

    public int GetDocumentCount() { return DocumentCount; }
    public int GetLongestDocumentLength() { return LongestDocumentLength; }
    public double GetAverageDocumentLength() { return AverageDocumentLength; }
    public double GetRepeatedPhraseRatio() { return RepeatedPhraseRatio; }
    public int GetUniqueCharacterCount() { return UniqueCharacterCount; }
    public int GetEstimatedAlphabetSize() { return EstimatedAlphabetSize; }

    public String ToString() {
        StringBuilder Sb = new StringBuilder();
        Sb.append("=== Corpus Fingerprint ===\n");
        Sb.append("CorpusId              : ").append(CorpusId).append('\n');
        Sb.append("DocumentCount         : ").append(DocumentCount).append('\n');
        Sb.append("CharacterCount        : ").append(CharacterCount).append('\n');
        Sb.append("WordCount             : ").append(WordCount).append('\n');
        Sb.append("VocabularySize        : ").append(VocabularySize).append('\n');
        Sb.append("RepeatedWordCount     : ").append(RepeatedWordCount).append('\n');
        Sb.append("LongestWordLength     : ").append(LongestWordLength).append('\n');
        Sb.append("LongestDocumentLength : ").append(LongestDocumentLength).append('\n');
        Sb.append("AverageWordLength     : ").append(F2(AverageWordLength)).append('\n');
        Sb.append("AverageDocumentLength : ").append(F0(AverageDocumentLength)).append(" chars\n");
        Sb.append("AverageSentenceLength : ").append(F2(AverageSentenceLength)).append(" words\n");
        Sb.append("VocabularyRichness    : ").append(F4(VocabularyRichness)).append('\n');
        Sb.append("EntropyEstimate       : ").append(F4(EntropyEstimate)).append(" bits/char\n");
        Sb.append("DuplicateScore        : ").append(F4(DuplicateScore)).append('\n');
        Sb.append("PatternDensity        : ").append(F4(PatternDensity)).append('\n');
        Sb.append("RepeatedPhraseRatio   : ").append(F4(RepeatedPhraseRatio)).append('\n');
        Sb.append("UniqueCharacterCount  : ").append(UniqueCharacterCount).append('\n');
        Sb.append("EstimatedAlphabetSize : ").append(EstimatedAlphabetSize).append('\n');
        Sb.append("CorpusCategory        : ").append(CorpusCategory).append('\n');
        Sb.append("CategoryTags          : ");
        for (int I = 0; I < CategoryTags.length; I++) {
            if (I > 0) Sb.append(", ");
            Sb.append(CategoryTags[I]);
        }
        return Sb.toString();
    }

    private String F0(double V) {
        long Scaled = (long) (V + 0.5);
        return "" + Scaled;
    }

    private String F2(double V) {
        long Scaled = (long) (V * 100.0 + 0.5);
        return (Scaled / 100) + "." + Pad((int) (Scaled % 100), 2);
    }

    private String F4(double V) {
        long Scaled = (long) (V * 10000.0 + 0.5);
        return (Scaled / 10000) + "." + Pad((int) (Scaled % 10000), 4);
    }

    private String Pad(int V, int Width) {
        String S = "" + V;
        StringBuilder Sb = new StringBuilder();
        while (Sb.length() + S.length() < Width) Sb.append('0');
        Sb.append(S);
        return Sb.toString();
    }
}
