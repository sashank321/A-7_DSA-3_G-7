package stratasearch.profiler;

import stratasearch.models.CorpusFingerprint;
import stratasearch.util.ManualStringList;
import stratasearch.util.TextNormalizer;

// Reads documents, computes statistics, returns a CorpusFingerprint.
public class CorpusProfiler {

    private static final double Ln2 = 0.6931471805599453;

    private static double LogBase2(double X) {
        if (X <= 0.0) {
            return 0.0;
        }
        int Exp = 0;
        while (X >= 2.0) {
            X /= 2.0;
            Exp++;
        }
        while (X < 1.0) {
            X *= 2.0;
            Exp--;
        }
        double U = X - 1.0;
        double Ln = 0.0;
        double Term = U;
        for (int K = 1; K <= 30; K++) {
            if (K % 2 == 1) {
                Ln += Term / K;
            } else {
                Ln -= Term / K;
            }
            Term *= U;
        }
        return Exp + Ln / Ln2;
    }

    private static double Round4(double V) {
        return ((long) (V * 10000.0 + 0.5)) / 10000.0;
    }

    private static double Round2(double V) {
        return ((long) (V * 100.0 + 0.5)) / 100.0;
    }

    public CorpusFingerprint Profile(String[] Documents,
                                     String CorpusId,
                                     boolean MultiPatternIntent,
                                     boolean RepeatQueryIntent,
                                     double AverageQueryLength) {
        String Joined = TextNormalizer.JoinDocuments(Documents);
        int CharacterCount = Joined.length();
        int DocumentCount = Documents.length;

        ManualStringList Words = TextNormalizer.SplitWords(Joined);
        int WordCount = Words.GetSize();

        ManualStringList UniqueWords = new ManualStringList();
        int RepeatedWordCount = 0;
        int LongestWordLength = 0;
        long WordLengthSum = 0;
        int[] Freq = new int[WordCount == 0 ? 1 : WordCount];
        for (int I = 0; I < WordCount; I++) {
            String W = Words.Get(I);
            int L = W.length();
            WordLengthSum += L;
            if (L > LongestWordLength) {
                LongestWordLength = L;
            }
            int Idx = UniqueWords.IndexOf(W);
            if (Idx == -1) {
                UniqueWords.Add(W);
                Freq[UniqueWords.GetSize() - 1] = 1;
            } else {
                if (Freq[Idx] == 1) {
                    RepeatedWordCount++;
                }
                Freq[Idx]++;
            }
        }
        int VocabularySize = UniqueWords.GetSize();

        // Distinct characters observed.
        int[] CharPresent = new int[95];
        int UsableChars = 0;
        int UniqueCharacterCount = 0;
        for (int I = 0; I < CharacterCount; I++) {
            char C = Joined.charAt(I);
            if (C >= 32 && C <= 126) {
                if (CharPresent[C - 32] == 0) {
                    UniqueCharacterCount++;
                }
                CharPresent[C - 32]++;
                UsableChars++;
            }
        }
        int EstimatedAlphabetSize = UniqueCharacterCount + 1;
        if (EstimatedAlphabetSize > 95) EstimatedAlphabetSize = 95;

        // Shannon entropy over character distribution.
        double Entropy = 0.0;
        if (UsableChars > 0) {
            for (int I = 0; I < 95; I++) {
                if (CharPresent[I] > 0) {
                    double P = (double) CharPresent[I] / (double) UsableChars;
                    Entropy -= P * LogBase2(P);
                }
            }
        }

        int SentenceEnds = TextNormalizer.CountSentenceEnds(Joined);
        double AverageSentenceLength = WordCount == 0 ? 0.0
                : (SentenceEnds == 0 ? (double) WordCount : (double) WordCount / (double) SentenceEnds);
        double AverageWordLength = WordCount == 0 ? 0.0 : (double) WordLengthSum / (double) WordCount;
        double VocabularyRichness = WordCount == 0 ? 0.0 : (double) VocabularySize / (double) WordCount;
        double PatternDensity = CharacterCount == 0 ? 0.0 : (double) WordCount / (double) CharacterCount;

        // Document length stats over the normalized joined text, split on newlines.
        int LongestDocumentLength = 0;
        long DocLengthSum = 0;
        for (int D = 0; D < DocumentCount; D++) {
            String Clean = TextNormalizer.Normalize(Documents[D]);
            int L = Clean.length();
            DocLengthSum += L;
            if (L > LongestDocumentLength) {
                LongestDocumentLength = L;
            }
        }
        double AverageDocumentLength = DocumentCount == 0 ? 0.0 : (double) DocLengthSum / (double) DocumentCount;

        // Duplicate score from word-hash spread.
        double DuplicateScore = 0.0;
        int SlotCount = VocabularySize * 2 + 7;
        long HashSum = 0;
        for (int I = 0; I < WordCount; I++) {
            long H = 0;
            String W = Words.Get(I);
            for (int J = 0; J < W.length(); J++) {
                H = (H * 31 + W.charAt(J)) % SlotCount;
            }
            HashSum += H;
        }
        double AvgHash = WordCount == 0 ? 0.0 : (double) HashSum / (double) WordCount;
        double Norm = SlotCount == 0 ? 0.0 : AvgHash / (double) SlotCount;
        DuplicateScore = Round4(1.0 - Norm);

        // Repeated phrase ratio: adjacent word-pairs that appear more than once.
        double RepeatedPhraseRatio = 0.0;
        if (WordCount > 1) {
            int BigramSlots = WordCount * 2 + 5;
            String[] BigramKeys = new String[BigramSlots];
            int[] BigramCounts = new int[BigramSlots];
            int TotalBigrams = WordCount - 1;
            int RepeatedBigrams = 0;
            for (int I = 0; I < TotalBigrams; I++) {
                String Key = Words.Get(I) + " " + Words.Get(I + 1);
                long H = 0;
                for (int J = 0; J < Key.length(); J++) {
                    H = (H * 31 + Key.charAt(J)) % BigramSlots;
                }
                int Slot = (int) H;
                int Probes = 0;
                while (BigramKeys[Slot] != null && Probes < BigramSlots) {
                    if (BigramKeys[Slot].equals(Key)) {
                        if (BigramCounts[Slot] == 1) {
                            RepeatedBigrams++;
                        }
                        BigramCounts[Slot]++;
                        break;
                    }
                    Slot = (Slot + 1) % BigramSlots;
                    Probes++;
                }
                if (BigramKeys[Slot] == null) {
                    BigramKeys[Slot] = Key;
                    BigramCounts[Slot] = 1;
                }
            }
            RepeatedPhraseRatio = TotalBigrams == 0 ? 0.0 : Round4((double) RepeatedBigrams / (double) TotalBigrams);
        }

        // Category tags (multi-label) + primary label preserved for compatibility.
        ManualStringList TagList = new ManualStringList();
        if (MultiPatternIntent) {
            TagList.Add("MULTI_PATTERN_HEAVY");
        }
        boolean IsIndexFriendly = RepeatQueryIntent || PatternDensity >= 0.15;
        if (IsIndexFriendly) {
            TagList.Add("INDEX_FRIENDLY");
        }
        boolean IsRepetitive = VocabularyRichness < 0.40 && WordCount >= 20;
        if (IsRepetitive) {
            TagList.Add("REPETITIVE");
        }
        boolean IsSparse = VocabularyRichness > 0.85 && WordCount > 0;
        if (IsSparse) {
            TagList.Add("SPARSE");
        }
        boolean IsDense = PatternDensity >= 0.18 || (VocabularyRichness >= 0.40 && VocabularyRichness <= 0.85 && WordCount >= 30);
        if (IsDense) {
            TagList.Add("DENSE");
        }
        if (TagList.GetSize() == 0) {
            TagList.Add("MIXED");
        }
        String[] CategoryTags = TagList.ToArray();

        String CorpusCategory;
        if (MultiPatternIntent) {
            CorpusCategory = "MULTI_PATTERN_HEAVY";
        } else if (IsIndexFriendly) {
            CorpusCategory = "INDEX_FRIENDLY";
        } else if (IsRepetitive) {
            CorpusCategory = "REPETITIVE";
        } else if (IsSparse) {
            CorpusCategory = "SPARSE";
        } else if (IsDense) {
            CorpusCategory = "DENSE";
        } else {
            CorpusCategory = "MIXED";
        }

        return new CorpusFingerprint(
                CorpusId,
                WordCount,
                CharacterCount,
                VocabularySize,
                RepeatedWordCount,
                LongestWordLength,
                Round2(AverageWordLength * 100) / 100.0,
                Round2(AverageSentenceLength * 100) / 100.0,
                Round4(VocabularyRichness),
                Round4(Entropy),
                DuplicateScore,
                Round4(PatternDensity),
                CorpusCategory,
                CategoryTags,
                DocumentCount,
                LongestDocumentLength,
                Round2(AverageDocumentLength * 100) / 100.0,
                RepeatedPhraseRatio,
                UniqueCharacterCount,
                EstimatedAlphabetSize);
    }

    // Backwards-compatible overload (Phase 1 original signature).
    public CorpusFingerprint Profile(String[] Documents, String CorpusId, boolean MultiPatternIntent, boolean RepeatQueryIntent) {
        return Profile(Documents, CorpusId, MultiPatternIntent, RepeatQueryIntent, 0.0);
    }
}
