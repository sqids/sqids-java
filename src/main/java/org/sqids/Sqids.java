package org.sqids;

import java.util.*;
import java.util.stream.Collectors;

public class Sqids {
    private String Alphabet;
    private int MinLength;
    private Set<String> BlockList;
    private SqidsOptions defaultOptions = new SqidsOptions();

    public Sqids() {
        this(null);
    }

    public Sqids(SqidsOptions options) {
        String alphabet = (options != null && options.Alphabet != null) ? options.Alphabet : defaultOptions.Alphabet;
        int minLength = options != null ? options.MinLength : defaultOptions.MinLength;
        Set<String> blocklist = (options != null && options.BlockList != null) ? new HashSet<>(options.BlockList) : new HashSet<>(defaultOptions.BlockList);

        if (alphabet.getBytes().length != alphabet.length()) {
            throw new IllegalArgumentException("Alphabet cannot contain multibyte characters");
        }

        int minAlphabetLength = 3;
        if (alphabet.length() < minAlphabetLength) {
            throw new IllegalArgumentException("Alphabet length must be at least " + minAlphabetLength);
        }

        if (new HashSet<>(Arrays.asList(alphabet.split(""))).size() != alphabet.length()) {
            throw new IllegalArgumentException("Alphabet must contain unique characters");
        }

        int minLengthLimit = 255;
        if (minLength < 0 || minLength > minLengthLimit) {
            throw new IllegalArgumentException("Minimum length has to be between 0 and " + minLengthLimit);
        }

        Set<String> filteredBlocklist = new HashSet<>();
        List<String> alphabetChars = new ArrayList<>(Arrays.asList(alphabet.toLowerCase().split("")));
        for (String word : blocklist) {
            if (word.length() >= 3) {
                String wordLowercased = word.toLowerCase();
                List<String> wordChars = new ArrayList<>(Arrays.asList(wordLowercased.split("")));
                List<String> intersection = new ArrayList<>(wordChars);
                intersection.retainAll(alphabetChars);
                if (intersection.size() == wordChars.size()) {
                    filteredBlocklist.add(wordLowercased);
                }
            }
        }

        this.Alphabet = this.shuffle(alphabet);
        this.MinLength = minLength;
        this.BlockList = filteredBlocklist;
    }

    public String encode(List<Long> numbers) {
        if (numbers.isEmpty()) {
            return "";
        }
        long maxValue = maxValue();
        List<Long> inRangeNumbers = numbers.stream()
                .filter(n -> n >= 0 && n <= maxValue)
                .collect(Collectors.toList());
        if (inRangeNumbers.size() != numbers.size()) {
            throw new RuntimeException("Encoding supports numbers between 0 and " + maxValue);
        }
        return encodeNumbers(inRangeNumbers);
    }

    public List<Long> decode(String id) {
        List<Long> ret = new ArrayList<>();

        if (id.isEmpty()) {
            return ret;
        }

        char[] alphabetChars = this.Alphabet.toCharArray();
        for (char c : id.toCharArray()) {
            if (!new String(alphabetChars).contains(String.valueOf(c))) {
                return ret;
            }
        }

        char prefix = id.charAt(0);
        int offset = this.Alphabet.indexOf(prefix);
        String modifiedAlphabet = this.Alphabet.substring(offset) + this.Alphabet.substring(0, offset);
        modifiedAlphabet = new StringBuilder(modifiedAlphabet).reverse().toString();
        String slicedId = id.substring(1);

        while (slicedId.length() > 0) {
            char separator = modifiedAlphabet.charAt(0);

            String[] chunks = slicedId.split(String.valueOf(separator), 2);
            if (chunks.length > 0) {
                if (chunks[0].isEmpty()) {
                    return ret;
                }

                ret.add(toNumber(chunks[0], modifiedAlphabet.substring(1)));
                if (chunks.length > 1) {
                    modifiedAlphabet = shuffle(modifiedAlphabet);
                }
            }

            slicedId = chunks.length > 1 ? chunks[1] : "";
        }

        return ret;
    }

    private String encodeNumbers(List<Long> numbers) {
        return this.encodeNumbers(numbers, 0);
    }

    private String encodeNumbers(List<Long> numbers, int increment) {
        if (increment > this.Alphabet.length()) {
            throw new RuntimeException("Reached max attempts to re-generate the ID");
        }

        long offset = numbers.size();
        for (int i = 0; i < numbers.size(); i++) {
            offset = offset + this.Alphabet.charAt((int) (numbers.get(i) % this.Alphabet.length())) + i;
        }
        offset %= this.Alphabet.length();
        offset = (offset + increment) % this.Alphabet.length();

        String modifiedAlphabet = this.Alphabet.substring((int) offset) + this.Alphabet.substring(0, (int) offset);
        char prefix = modifiedAlphabet.charAt(0);
        modifiedAlphabet = new StringBuilder(modifiedAlphabet).reverse().toString();
        StringBuilder idBuilder = new StringBuilder().append(prefix);

        for (int i = 0; i < numbers.size(); i++) {
            long num = numbers.get(i);
            idBuilder.append(toId(num, modifiedAlphabet.substring(1)));
            if (i < numbers.size() - 1) {
                idBuilder.append(modifiedAlphabet.charAt(0));
                modifiedAlphabet = shuffle(modifiedAlphabet);
            }
        }

        String id = idBuilder.toString();

        if (this.MinLength > id.length()) {
            id += modifiedAlphabet.charAt(0);

            while (this.MinLength - id.length() > 0) {
                modifiedAlphabet = shuffle(modifiedAlphabet);
                id += modifiedAlphabet.substring(0, Math.min(this.MinLength - id.length(), modifiedAlphabet.length()));
            }
        }

        if (isBlockedId(id)) {
            id = encodeNumbers(numbers, increment + 1);
        }

        return id;
    }

    private String shuffle(String alphabet) {
        char[] chars = alphabet.toCharArray();

        for (int i = 0, j = chars.length - 1; j > 0; i++, j--) {
            int r = (i * j + chars[i] + chars[j]) % chars.length;
            char temp = chars[i];
            chars[i] = chars[r];
            chars[r] = temp;
        }

        return new String(chars);
    }

//    private String toId(long num, String alphabet) {
//        List<Character> id = new ArrayList<>();
//        char[] chars = alphabet.toCharArray();
//
//        long result = num;
//        do {
//            id.add(0, chars[(int) result % chars.length]);
//            result = result / chars.length;
//        } while (result > 0);
//
//        StringBuilder idBuilder = new StringBuilder();
//        for (char c : id) {
//            idBuilder.append(c);
//        }
//        return idBuilder.toString();
//    }

    private String toId(long num, String alphabet) {
        StringBuilder id = new StringBuilder();
        char[] chars = alphabet.toCharArray();

        do {
            id.append(chars[(int) (num % chars.length)]);
            num /= chars.length;
        } while (num > 0);

        return id.reverse().toString();
    }

    private long toNumber(String id, String alphabet) {
        char[] chars = alphabet.toCharArray();
        long number = 0;

        for (char c : id.toCharArray()) {
            number = number * chars.length + new String(chars).indexOf(c);
        }

        return number;
    }

    private boolean isBlockedId(String id) {
        String lowercaseId = id.toLowerCase();

        for (String word : this.BlockList) {
            if (word.length() <= lowercaseId.length()) {
                if (lowercaseId.length() <= 3 || word.length() <= 3) {
                    if (lowercaseId.equals(word)) {
                        return true;
                    }
                } else if (word.matches("\\d+")) {
                    if (lowercaseId.startsWith(word) || lowercaseId.endsWith(word)) {
                        return true;
                    }
                } else if (lowercaseId.contains(word)) {
                    return true;
                }
            }
        }
        return false;
    }

    private long maxValue() {
        return Long.MAX_VALUE;
    }
}
