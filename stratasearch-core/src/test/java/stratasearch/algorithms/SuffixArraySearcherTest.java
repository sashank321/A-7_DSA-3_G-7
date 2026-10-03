package stratasearch.algorithms;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class SuffixArraySearcherTest {

    @Test
    void findsPatternInText() {
        SuffixArraySearcher searcher = new SuffixArraySearcher();
        List<Integer> hits = searcher.search("banana", "ana");
        assertFalse(hits.isEmpty());
    }

    @Test
    void noMatchReturnsEmpty() {
        SuffixArraySearcher searcher = new SuffixArraySearcher();
        assertTrue(searcher.search("hello world", "xyz").isEmpty());
    }
}
