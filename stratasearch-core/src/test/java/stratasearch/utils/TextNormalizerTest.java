package stratasearch.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TextNormalizerTest {

    @Test
    void controlCharsNormalizeToSpace() {
        String input = "hello\u0001world";
        String result = TextNormalizer.normalize(input);
        assertEquals("hello world", result);
    }

    @Test
    void printableAsciiPreserved() {
        String input = "The quick brown fox.";
        assertEquals(input, TextNormalizer.normalize(input));
    }
}
