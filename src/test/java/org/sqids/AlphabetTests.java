package org.sqids;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class AlphabetTests {
    @Test
    public void simpleAlphabet() {
        SqidsOptions options = new SqidsOptions();
        options.Alphabet = "0123456789abcdef";
        Sqids sqids = new Sqids(options);
        List<Long> numbers = Arrays.asList(1L, 2L, 3L);
        String id = "489158";
        Assertions.assertEquals(sqids.encode(numbers), id);
        Assertions.assertEquals(sqids.decode(id), numbers);
    }

    @Test
    public void shortAlphabet() {
        SqidsOptions options = new SqidsOptions();
        options.Alphabet = "abc";
        Sqids sqids = new Sqids(options);
        List<Long> numbers = Arrays.asList(1L, 2L, 3L);
        Assertions.assertEquals(sqids.decode(sqids.encode(numbers)), numbers);
    }

    @Test
    public void multibyteCharacters() {
        SqidsOptions options = new SqidsOptions();
        options.Alphabet = "ë1092";
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Sqids(options));
    }

    @Test
    public void repeatingAlphabetCharacters() {
        SqidsOptions options = new SqidsOptions();
        options.Alphabet = "aabcdefg";
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Sqids(options));
    }

    @Test
    public void tooShortOfAnAlphabet() {
        SqidsOptions options = new SqidsOptions();
        options.Alphabet = "ab";
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Sqids(options));
    }

}
