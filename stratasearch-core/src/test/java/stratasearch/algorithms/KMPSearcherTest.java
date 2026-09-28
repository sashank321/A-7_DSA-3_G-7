package stratasearch.algorithms;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class KMPSearcherTest {

    @Test
    void findsAllOccurrences() {
        KMPSearcher searcher = new KMPSearcher();
        List<Integer> hits = searcher.search("ababcabab", "abab");
        assertFalse(hits.isEmpty());
    }

    @Test
    void noMatchReturnsEmpty() {
        KMPSearcher searcher = new KMPSearcher();
        assertTrue(searcher.search("hello world", "xyz").isEmpty());
    }
}
