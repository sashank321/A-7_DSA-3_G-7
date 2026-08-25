package stratasearch.algorithms;

// Manual ascending sort (insertion) used to normalize match-position output.
public class IntSortUtil {
    public static void SortAscending(int[] Arr, int Count) {
        for (int I = 1; I < Count; I++) {
            int Key = Arr[I];
            int J = I - 1;
            while (J >= 0 && Arr[J] > Key) {
                Arr[J + 1] = Arr[J];
                J--;
            }
            Arr[J + 1] = Key;
        }
    }
}
