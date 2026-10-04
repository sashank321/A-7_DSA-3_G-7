package stratasearch.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TextNormalizerTest {

    @Test
    void controlCharsNormalizeToSpace() {
        String input = "hello\u0001world";
        String result = TextNormalizer.Normalize(input);
        assertEquals("hello world", result);
    }

    @Test
    void printableAsciiPreservedAndLowercased() {
        String input = "The quick brown fox.";
        assertEquals("the quick brown fox.", TextNormalizer.Normalize(input));
    }
}
