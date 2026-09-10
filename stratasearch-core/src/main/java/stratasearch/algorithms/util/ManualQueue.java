package stratasearch.algorithms.util;

// Manually implemented flat-array queue with F and R pointers initialized to -1.
// Used exclusively for Aho-Corasick BFS failure-link construction.
public class ManualQueue {
    private Object[] Items;
    private int F;
    private int R;
    private int Count;

    public ManualQueue(int Capacity) {
        if (Capacity < 1) {
            Capacity = 1;
        }
        Items = new Object[Capacity];
        F = -1;
        R = -1;
        Count = 0;
    }

    public boolean IsEmpty() {
        return Count == 0;
    }

    public int GetSize() {
        return Count;
    }

    public void Enqueue(Object Value) {
        if (Count == Items.length) {
            Object[] Grown = new Object[Items.length * 2];
            for (int I = 0; I < Count; I++) {
                Grown[I] = Items[(F + I) % Items.length];
            }
            Items = Grown;
            F = 0;
            R = Count - 1;
        }
        if (IsEmpty()) {
            F = 0;
            R = 0;
        } else {
            R = (R + 1) % Items.length;
        }
        Items[R] = Value;
        Count++;
    }

    public Object Dequeue() {
        if (IsEmpty()) {
            return null;
        }
        Object Value = Items[F];
        Items[F] = null;
        Count--;
        if (Count == 0) {
            F = -1;
            R = -1;
        } else {
            F = (F + 1) % Items.length;
        }
        return Value;
    }
}
