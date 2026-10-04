package stratasearch.algorithms;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KMPSearcherTest {

    @Test
    void findsAllOccurrences() {
        KmpSearcher searcher = new KmpSearcher();
        SearchResult res = searcher.Search("ababcabab", new String[]{"abab"});
        assertEquals(2, res.GetMatchCount());
        assertEquals(0, res.GetMatchPositions()[0]);
        assertEquals(5, res.GetMatchPositions()[1]);
    }

    @Test
    void noMatchReturnsEmpty() {
        KmpSearcher searcher = new KmpSearcher();
        SearchResult res = searcher.Search("hello world", new String[]{"xyz"});
        assertEquals(0, res.GetMatchCount());
    }

    @Test
    void emptyPatternHandled() {
        KmpSearcher searcher = new KmpSearcher();
        SearchResult res = searcher.Search("hello world", new String[]{""});
        assertEquals(0, res.GetMatchCount());
    }
}
