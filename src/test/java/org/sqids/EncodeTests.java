package org.sqids;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EncodeTests {
    private final Sqids sqids = Sqids.builder().build();

    @Test
    public void simple() {
        List<Long> numbers = Arrays.asList(1L, 2L, 3L);
        String id = "86Rf07";
        Assertions.assertEquals(sqids.encode(numbers), id);
        Assertions.assertEquals(sqids.decode(id), numbers);
    }

    @Test
    public void differentInputs() {
        List<Long> numbers = Arrays.asList(
                0L,
                0L,
                0L,
                1L,
                2L,
                3L,
                100L,
                1000L,
                100000L,
                1000000L,
                Long.MAX_VALUE);
        Assertions.assertEquals(sqids.decode(sqids.encode(numbers)), numbers);
    }

    @Test
    public void incrementalNumber() {
        Map<String, List<Long>> ids = new HashMap<String, List<Long>>() {
            {
                put("bM", Arrays.asList(0L));
                put("Uk", Arrays.asList(1L));
                put("gb", Arrays.asList(2L));
                put("Ef", Arrays.asList(3L));
                put("Vq", Arrays.asList(4L));
                put("uw", Arrays.asList(5L));
                put("OI", Arrays.asList(6L));
                put("AX", Arrays.asList(7L));
                put("p6", Arrays.asList(8L));
                put("nJ", Arrays.asList(9L));
            }
        };
        for (String id : ids.keySet()) {
            List<Long> numbers = ids.get(id);
            Assertions.assertEquals(sqids.encode(numbers), id);
            Assertions.assertEquals(sqids.decode(id), numbers);
        }
    }

    @Test
    public void incrementalNumbers() {
        Map<String, List<Long>> ids = new HashMap<String, List<Long>>() {
            {
                put("SvIz", Arrays.asList(0L, 0L));
                put("n3qa", Arrays.asList(0L, 1L));
                put("tryF", Arrays.asList(0L, 2L));
                put("eg6q", Arrays.asList(0L, 3L));
                put("rSCF", Arrays.asList(0L, 4L));
                put("sR8x", Arrays.asList(0L, 5L));
                put("uY2M", Arrays.asList(0L, 6L));
                put("74dI", Arrays.asList(0L, 7L));
                put("30WX", Arrays.asList(0L, 8L));
                put("moxr", Arrays.asList(0L, 9L));
            }
        };
        for (String id : ids.keySet()) {
            List<Long> numbers = ids.get(id);
            Assertions.assertEquals(sqids.encode(numbers), id);
            Assertions.assertEquals(sqids.decode(id), numbers);
        }
    }

    @Test
    public void multiInput() {
        List<Long> numbers = Arrays.asList(
                0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L,
                21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L, 31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L,
                39L, 40L, 41L, 42L, 43L, 44L, 45L, 46L, 47L, 48L, 49L, 50L, 51L, 52L, 53L, 54L, 55L, 56L,
                57L, 58L, 59L, 60L, 61L, 62L, 63L, 64L, 65L, 66L, 67L, 68L, 69L, 70L, 71L, 72L, 73L, 74L,
                75L, 76L, 77L, 78L, 79L, 80L, 81L, 82L, 83L, 84L, 85L, 86L, 87L, 88L, 89L, 90L, 91L, 92L,
                93L, 94L, 95L, 96L, 97L, 98L, 99L);
        Assertions.assertEquals(sqids.decode(sqids.encode(numbers)), numbers);
    }

    @Test
    public void encodeNoNumbers() {
        List<Long> numbers = new ArrayList<>();
        Assertions.assertEquals(sqids.encode(numbers), "");
    }

    @Test
    public void decodeEmptyString() {
        List<Long> numbers = new ArrayList<>();
        Assertions.assertEquals(sqids.decode(""), numbers);
    }

    @Test
    public void decodeInvalidCharacter() {
        List<Long> numbers = new ArrayList<>();
        Assertions.assertEquals(sqids.decode("*"), numbers);
    }

    @Test
    public void encodeOutOfRangeNumbers() {
        Assertions.assertThrows(RuntimeException.class, () -> sqids.encode(Arrays.asList(-1L)));
        Assertions.assertThrows(RuntimeException.class, () -> sqids.encode(Arrays.asList(Long.MAX_VALUE + 1)));
    }
}
