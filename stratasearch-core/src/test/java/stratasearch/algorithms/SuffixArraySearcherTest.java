package stratasearch.algorithms;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SuffixArraySearcherTest {

    @Test
    void findsPatternInText() {
        SuffixArraySearcher searcher = new SuffixArraySearcher();
        SearchResult res = searcher.Search("banana", new String[]{"ana"});
        assertTrue(res.GetMatchCount() > 0);
    }

    @Test
    void noMatchReturnsEmpty() {
        SuffixArraySearcher searcher = new SuffixArraySearcher();
        SearchResult res = searcher.Search("hello world", new String[]{"xyz"});
        assertEquals(0, res.GetMatchCount());
    }
}
