package stratasearch.algorithms.util;

// Manual prefix-doubling construction of a suffix array. No Arrays.sort / no Comparator.
public class SuffixSortUtil {

    // Rank of a partial key at offset; -1 past end so shorter suffixes sort first.
    private static int PartialKey(String S, int Index) {
        if (Index >= S.length()) {
            return -1;
        }
        return S.charAt(Index) + 1;
    }

    // Counting sort of suffix indices using Key[suffixIndex]. MaxKey = maximum key value.
    private static void CountingSortByKey(int[] Order, int[] Key, int MaxKey) {
        int N = Order.length;
        int[] Bucket = new int[MaxKey + 2];
        for (int I = 0; I < N; I++) {
            Bucket[Key[Order[I]] + 1]++;
        }
        int Running = 0;
        for (int B = 0; B < Bucket.length; B++) {
            int C = Bucket[B];
            Bucket[B] = Running;
            Running += C;
        }
        int[] Out = new int[N];
        for (int I = 0; I < N; I++) {
            int K = Key[Order[I]];
            Out[Bucket[K + 1]] = Order[I];
            Bucket[K + 1]++;
        }
        for (int I = 0; I < N; I++) {
            Order[I] = Out[I];
        }
    }

    public static int[] BuildSuffixArray(String S) {
        int N = S.length();
        if (N == 0) {
            return new int[0];
        }
        int[] Order = new int[N];
        int[] Rank = new int[N];
        for (int I = 0; I < N; I++) {
            Order[I] = I;
        }
        // Pass 0: counting sort by first character.
        int[] FirstKey = new int[N];
        int MaxFirst = 0;
        for (int I = 0; I < N; I++) {
            FirstKey[I] = S.charAt(I);
            if (FirstKey[I] > MaxFirst) {
                MaxFirst = FirstKey[I];
            }
        }
        int[] FirstKeyBySuffix = new int[N];
        for (int I = 0; I < N; I++) {
            FirstKeyBySuffix[I] = FirstKey[I];
        }
        CountingSortByKey(Order, FirstKeyBySuffix, MaxFirst);
        int Classes = 0;
        Rank[Order[0]] = 0;
        for (int I = 1; I < N; I++) {
            if (S.charAt(Order[I]) != S.charAt(Order[I - 1])) {
                Classes++;
            }
            Rank[Order[I]] = Classes;
        }
        // Doubling passes.
        for (int Len = 1; Len < N; Len = Len * 2) {
            // Key[suffix] = rank of suffix at position (suffix + Len), or -1 if past end.
            int[] SecondHalfKey = new int[N];
            int MaxSecond = 0;
            for (int I = 0; I < N; I++) {
                int Pos = I + Len;
                int Key = (Pos < N) ? Rank[Pos] : -1;
                SecondHalfKey[I] = Key;
                if (Key > MaxSecond) {
                    MaxSecond = Key;
                }
            }
            CountingSortByKey(Order, SecondHalfKey, MaxSecond);
            // Rank is already key-by-suffix for the first half.
            CountingSortByKey(Order, Rank, Classes);
            // Re-rank.
            int[] NewRank = new int[N];
            int NewClasses = 0;
            NewRank[Order[0]] = 0;
            for (int I = 1; I < N; I++) {
                int A = Order[I - 1];
                int B = Order[I];
                int ASecond = (A + Len < N) ? Rank[A + Len] : -1;
                int BSecond = (B + Len < N) ? Rank[B + Len] : -1;
                if (Rank[A] != Rank[B] || ASecond != BSecond) {
                    NewClasses++;
                }
                NewRank[B] = NewClasses;
            }
            for (int I = 0; I < N; I++) {
                Rank[I] = NewRank[I];
            }
            Classes = NewClasses;
            if (Classes == N - 1) {
                break;
            }
        }
        return Order;
    }

    // Kasai's LCP construction.
    public static int[] BuildLcpArray(String S, int[] SuffixArray) {
        int N = S.length();
        int[] Lcp = new int[N];
        if (N == 0) {
            return Lcp;
        }
        int[] RankOf = new int[N];
        for (int I = 0; I < N; I++) {
            RankOf[SuffixArray[I]] = I;
        }
        int H = 0;
        for (int I = 0; I < N; I++) {
            int R = RankOf[I];
            if (R == 0) {
                Lcp[R] = 0;
                continue;
            }
            int J = SuffixArray[R - 1];
            while (I + H < N && J + H < N && S.charAt(I + H) == S.charAt(J + H)) {
                H++;
            }
            Lcp[R] = H;
            if (H > 0) {
                H--;
            }
        }
        return Lcp;
    }
}
