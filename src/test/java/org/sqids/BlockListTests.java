package org.sqids;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BlockListTests {
    @Test
    public void blockList() {
        Sqids sqids = Sqids.builder()
                .build();
        List<Long> numbers = Arrays.asList(4572721L);
        Assertions.assertEquals(sqids.decode("aho1e"), numbers);
        Assertions.assertEquals(sqids.encode(numbers), "JExTR");
    }

    @Test
    public void emptyBlockList() {
        Sqids sqids = Sqids.builder()
                .blockList(new HashSet<>())
                .build();
        List<Long> numbers = Arrays.asList(4572721L);
        Assertions.assertEquals(sqids.decode("aho1e"), numbers);
        Assertions.assertEquals(sqids.encode(numbers), "aho1e");
    }

    @Test
    public void nonEmptyBlockList() {
        Sqids sqids = Sqids.builder()
                .blockList(new HashSet<>(Arrays.asList("ArUO")))
                .build();
        List<Long> numbers = Arrays.asList(4572721L);
        Assertions.assertEquals(sqids.decode("aho1e"), numbers);
        Assertions.assertEquals(sqids.encode(numbers), "aho1e");

        numbers = Arrays.asList(100000L);
        Assertions.assertEquals(sqids.decode("ArUO"), numbers);
        Assertions.assertEquals(sqids.encode(numbers), "QyG4");
        Assertions.assertEquals(sqids.decode("QyG4"), numbers);
    }

    @Test
    public void encodeBlockList() {
        Sqids sqids = Sqids.builder()
                .blockList(new HashSet<>(Arrays.asList(
                        "JSwXFaosAN", // normal result of 1st encoding, let's block that word on purpose
                        "OCjV9JK64o", // result of 2nd encoding
                        "rBHf", // result of 3rd encoding is `4rBHfOiqd3`, let's block a substring
                        "79SM", // result of 4th encoding is `dyhgw479SM`, let's block the postfix
                        "7tE6" // result of 4th encoding is `7tE6jdAHLe`, let's block the prefix
                )))
                .build();
        List<Long> numbers = Arrays.asList(1000000L, 2000000L);
        Assertions.assertEquals(sqids.encode(numbers), "1aYeB7bRUt");
        Assertions.assertEquals(sqids.decode("1aYeB7bRUt"), numbers);
    }

    @Test
    public void decodeBlockList() {
        Sqids sqids = Sqids.builder()
                .blockList(new HashSet<>(Arrays.asList(
                        "86Rf07",
                        "se8ojk",
                        "ARsz1p",
                        "Q8AI49",
                        "5sQRZO"
                )))
                .build();
        List<Long> numbers = Arrays.asList(1L, 2L, 3L);
        Assertions.assertEquals(sqids.decode("86Rf07"), numbers);
        Assertions.assertEquals(sqids.decode("se8ojk"), numbers);
        Assertions.assertEquals(sqids.decode("ARsz1p"), numbers);
        Assertions.assertEquals(sqids.decode("Q8AI49"), numbers);
        Assertions.assertEquals(sqids.decode("5sQRZO"), numbers);
    }

    @Test
    public void shortBlockList() {
        Sqids sqids = Sqids.builder()
                .blockList(new HashSet<>(Arrays.asList("pnd")))
                .build();
        List<Long> numbers = Arrays.asList(1000L);
        Assertions.assertEquals(sqids.decode(sqids.encode(numbers)), numbers);
    }

    @Test
    public void lowercaseBlockList() {
        Sqids sqids = Sqids.builder()
                .alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
                .blockList(new HashSet<>(Arrays.asList("sxnzkl")))
                .build();
        List<Long> numbers = Arrays.asList(1L, 2L, 3L);
        Assertions.assertEquals(sqids.encode(numbers), "IBSHOZ");
        Assertions.assertEquals(sqids.decode("IBSHOZ"), numbers);
    }

    @Test
    public void maxBlockList() {
        Sqids sqids = Sqids.builder()
                .alphabet("abc")
                .minLength(3)
                .blockList(new HashSet<>(Arrays.asList(
                        "cab",
                        "abc",
                        "bca")))
                .build();
        Assertions.assertThrows(RuntimeException.class, () -> sqids.encode(Arrays.asList(0L)));
    }
}
