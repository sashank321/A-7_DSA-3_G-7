package stratasearch.util;

// Manual dynamic string array. Replaces ArrayList<String> (forbidden).
public class ManualStringList {
    private String[] Items;
    private int Size;
    private static final int InitialCapacity = 8;

    public ManualStringList() {
        Items = new String[InitialCapacity];
        Size = 0;
    }

    private void EnsureCapacity() {
        if (Size == Items.length) {
            String[] Grown = new String[Items.length * 2];
            for (int I = 0; I < Size; I++) {
                Grown[I] = Items[I];
            }
            Items = Grown;
        }
    }

    public void Add(String Value) {
        EnsureCapacity();
        Items[Size] = Value;
        Size++;
    }

    public String Get(int Index) {
        return Items[Index];
    }

    public int GetSize() {
        return Size;
    }

    public int IndexOf(String Value) {
        for (int I = 0; I < Size; I++) {
            if (Items[I].equals(Value)) {
                return I;
            }
        }
        return -1;
    }

    public boolean Contains(String Value) {
        return IndexOf(Value) != -1;
    }

    public String[] ToArray() {
        String[] Exact = new String[Size];
        for (int I = 0; I < Size; I++) {
            Exact[I] = Items[I];
        }
        return Exact;
    }
}
