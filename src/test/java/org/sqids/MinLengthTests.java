package org.sqids;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MinLengthTests {
    private final int minLength = Sqids.Builder.DEFAULT_ALPHABET.length();

    @Test
    public void simple() {
        Sqids sqids = Sqids.builder()
                .minLength(minLength)
                .build();
        List<Long> numbers = Arrays.asList(1L, 2L, 3L);
        String id = "86Rf07xd4zBmiJXQG6otHEbew02c3PWsUOLZxADhCpKj7aVFv9I8RquYrNlSTM";
        Assertions.assertEquals(sqids.encode(numbers), id);
        Assertions.assertEquals(sqids.decode(id), numbers);
    }

    @Test
    public void incremental() {
        List<Long> numbers = Arrays.asList(1L, 2L, 3L);
        Map<Integer, String> ids = new HashMap<Integer, String>() {
            {
                put(6, "86Rf07");
                put(7, "86Rf07x");
                put(8, "86Rf07xd");
                put(9, "86Rf07xd4");
                put(10, "86Rf07xd4z");
                put(11, "86Rf07xd4zB");
                put(12, "86Rf07xd4zBm");
                put(13, "86Rf07xd4zBmi");
                put(minLength + 0, "86Rf07xd4zBmiJXQG6otHEbew02c3PWsUOLZxADhCpKj7aVFv9I8RquYrNlSTM");
                put(minLength + 1, "86Rf07xd4zBmiJXQG6otHEbew02c3PWsUOLZxADhCpKj7aVFv9I8RquYrNlSTMy");
                put(minLength + 2, "86Rf07xd4zBmiJXQG6otHEbew02c3PWsUOLZxADhCpKj7aVFv9I8RquYrNlSTMyf");
                put(minLength + 3, "86Rf07xd4zBmiJXQG6otHEbew02c3PWsUOLZxADhCpKj7aVFv9I8RquYrNlSTMyf1");
            }
        };
        for (Integer minLength : ids.keySet()) {
            Sqids sqids = Sqids.builder()
                    .minLength(minLength)
                    .build();
            String id = ids.get(minLength);
            Assertions.assertEquals(sqids.encode(numbers), id);
            Assertions.assertEquals(sqids.decode(id), numbers);
        }
    }

    @Test
    public void incrementalNumbers() {
        Sqids sqids = Sqids.builder()
                .minLength(minLength)
                .build();
        Map<String, List<Long>> ids = new HashMap<String, List<Long>>() {
            {
                put("SvIzsqYMyQwI3GWgJAe17URxX8V924Co0DaTZLtFjHriEn5bPhcSkfmvOslpBu", Arrays.asList(0L, 0L));
                put("n3qafPOLKdfHpuNw3M61r95svbeJGk7aAEgYn4WlSjXURmF8IDqZBy0CT2VxQc", Arrays.asList(0L, 1L));
                put("tryFJbWcFMiYPg8sASm51uIV93GXTnvRzyfLleh06CpodJD42B7OraKtkQNxUZ", Arrays.asList(0L, 2L));
                put("eg6ql0A3XmvPoCzMlB6DraNGcWSIy5VR8iYup2Qk4tjZFKe1hbwfgHdUTsnLqE", Arrays.asList(0L, 3L));
                put("rSCFlp0rB2inEljaRdxKt7FkIbODSf8wYgTsZM1HL9JzN35cyoqueUvVWCm4hX", Arrays.asList(0L, 4L));
                put("sR8xjC8WQkOwo74PnglH1YFdTI0eaf56RGVSitzbjuZ3shNUXBrqLxEJyAmKv2", Arrays.asList(0L, 5L));
                put("uY2MYFqCLpgx5XQcjdtZK286AwWV7IBGEfuS9yTmbJvkzoUPeYRHr4iDs3naN0", Arrays.asList(0L, 6L));
                put("74dID7X28VLQhBlnGmjZrec5wTA1fqpWtK4YkaoEIM9SRNiC3gUJH0OFvsPDdy", Arrays.asList(0L, 7L));
                put("30WXpesPhgKiEI5RHTY7xbB1GnytJvXOl2p0AcUjdF6waZDo9Qk8VLzMuWrqCS", Arrays.asList(0L, 8L));
                put("moxr3HqLAK0GsTND6jowfZz3SUx7cQ8aC54Pl1RbIvFXmEJuBMYVeW9yrdOtin", Arrays.asList(0L, 9L));
            }
        };
        for (String id : ids.keySet()) {
            List<Long> numbers = ids.get(id);
            Assertions.assertEquals(sqids.encode(numbers), id);
            Assertions.assertEquals(sqids.decode(id), numbers);
        }
    }

    @Test
    public void minLengths() {
        List<Integer> minLengths = Arrays.asList(0, 1, 5, 10, minLength);
        List<List<Long>> numbers = new ArrayList<List<Long>>() {{
            add(Arrays.asList(0L));
            add(Arrays.asList(0L, 0L, 0L, 0L, 0L));
            add(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));
            add(Arrays.asList(100L, 200L, 300L));
            add(Arrays.asList(1000L, 2000L, 30000L));
            add(Arrays.asList((long) Integer.MAX_VALUE));
        }};
        for (Integer minLength : minLengths) {
            Sqids sqids = Sqids.builder()
                    .minLength(minLength)
                    .build();
            for (List<Long> number : numbers) {
                String id = sqids.encode(number);
                Assertions.assertTrue(id.length() >= minLength);
                Assertions.assertEquals(sqids.decode(id), number);
            }
        }
    }

    @Test
    public void encodeOutOfRangeNumbers() {
        int minLengthLimit = 255;
        Assertions.assertThrows(RuntimeException.class, () -> Sqids.builder()
                .minLength(-1)
                .build());
        Assertions.assertThrows(RuntimeException.class, () -> Sqids.builder()
                .minLength(minLengthLimit + 1)
                .build());
    }
}
