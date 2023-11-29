package org.sqids;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Sqids is designed to generate short YouTube-looking IDs from numbers.
 * <p>
 * This is the Java implementation of https://github.com/sqids/sqids-spec.
 *
 * This implementation is immutable and thread-safe, no lock is necessary.
 */
public class Sqids {
    /**
    * The minimum allowable length of the alphabet used for encoding and
    * decoding Sqids.
    */
    public static final int MIN_ALPHABET_LENGTH = 3;

    /**
     * The maximum allowable minimum length of an encoded Sqid.
     */
    public static final int MIN_LENGTH_LIMIT = 255;

    /**
     * The minimum length of blocked words in the block list. Any words shorter
     * than the minimum are ignored.
     */
    public static final int MIN_BLOCK_LIST_WORD_LENGTH = 3;

    private final String alphabet;
    private final int alphabetLength;
    private final int minLength;
    private final Set<String> blockList;

    private Sqids(final Builder builder) {
        final String alphabet = builder.alphabet;
        final int alphabetLength = alphabet.length();
        final int minLength = builder.minLength;
        final Set<String> blockList = new HashSet<>(builder.blockList);

        if (alphabet.getBytes().length != alphabetLength) {
            throw new IllegalArgumentException("Alphabet cannot contain multibyte characters");
        }

        if (alphabetLength < MIN_ALPHABET_LENGTH) {
            throw new IllegalArgumentException("Alphabet length must be at least " + MIN_ALPHABET_LENGTH);
        }

        if (new HashSet<>(Arrays.asList(alphabet.split(""))).size() != alphabetLength) {
            throw new IllegalArgumentException("Alphabet must contain unique characters");
        }

        if (minLength < 0 || minLength > MIN_LENGTH_LIMIT) {
            throw new IllegalArgumentException("Minimum length has to be between 0 and " + MIN_LENGTH_LIMIT);
        }

        final Set<String> filteredBlockList = new HashSet<>();
        final List<String> alphabetChars = new ArrayList<>(Arrays.asList(alphabet.toLowerCase().split("")));
        for (String word : blockList) {
            if (word.length() >= MIN_BLOCK_LIST_WORD_LENGTH) {
                word = word.toLowerCase();
                List<String> wordChars = Arrays.asList(word.split(""));
                List<String> intersection = new ArrayList<>(wordChars);
                intersection.retainAll(alphabetChars);
                if (intersection.size() == wordChars.size()) {
                    filteredBlockList.add(word);
                }
            }
        }

        this.alphabet = this.shuffle(alphabet);
        this.alphabetLength = this.alphabet.length();
        this.minLength = minLength;
        this.blockList = filteredBlockList;
    }

    /**
     * Generate a Sqids' Builder.
     *
     * @return New Builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Encode a list of numbers to a Sqids ID.
     *
     * @param numbers Numbers to encode.
     * @return Sqids ID.
     */
    public String encode(final List<Long> numbers) {
        if (numbers.isEmpty()) {
            return "";
        }
        for (Long num : numbers) {
            if (num < 0) {
                throw new RuntimeException("Encoding supports numbers between 0 and " + Long.MAX_VALUE);
            }
        }
        return encodeNumbers(numbers);
    }

    /**
     * Decode a Sqids ID back to numbers.
     *
     * @param id ID to decode.
     * @return List of decoded numbers.
     */
    public List<Long> decode(final String id) {
        List<Long> ret = new ArrayList<>();
        if (id.isEmpty()) {
            return ret;
        }

        final char[] alphabetChars = this.alphabet.toCharArray();
        Set<Character> alphabetSet = new HashSet<>();
        for (final char c : alphabetChars) {
            alphabetSet.add(c);
        }
        for (final char c : id.toCharArray()) {
            if (!alphabetSet.contains(c)) {
                return ret;
            }
        }

        final char prefix = id.charAt(0);
        final int offset = this.alphabet.indexOf(prefix);
        String alphabet = new StringBuilder(this.alphabet.substring(offset))
                .append(this.alphabet, 0, offset)
                .reverse()
                .toString();

        int index = 1;
        while (true) {
            final char separator = alphabet.charAt(0);
            int separatorIndex = id.indexOf(separator, index);
            if (separatorIndex == -1) {
                separatorIndex = id.length();
            } else if (index == separatorIndex) {
                break;
            }
            ret.add(toNumber(id, index, separatorIndex, alphabet.substring(1)));
            index = separatorIndex + 1;
            if (index < id.length()) {
                alphabet = shuffle(alphabet);
            } else {
                break;
            }
        }
        return ret;
    }

    private String encodeNumbers(final List<Long> numbers) {
        return this.encodeNumbers(numbers, 0);
    }

    private String encodeNumbers(final List<Long> numbers, final int increment) {
        if (increment > this.alphabetLength) {
            throw new RuntimeException("Reached max attempts to re-generate the ID");
        }

        final int numberSize = numbers.size();
        long offset = numberSize;
        for (int i = 0; i < numberSize; i++) {
            offset = offset + this.alphabet.charAt((int) (numbers.get(i) % this.alphabetLength)) + i;
        }
        offset %= this.alphabetLength;
        offset = (offset + increment) % this.alphabetLength;

        final StringBuilder alphabetB = new StringBuilder(this.alphabet.substring((int) offset))
                .append(this.alphabet, 0, (int) offset);
        final char prefix = alphabetB.charAt(0);
        String alphabet = alphabetB.reverse().toString();
        final StringBuilder id = new StringBuilder().append(prefix);
        for (int i = 0; i < numberSize; i++) {
            final long num = numbers.get(i);
            id.append(toId(num, alphabet.substring(1)));
            if (i < numberSize - 1) {
                id.append(alphabet.charAt(0));
                alphabet = shuffle(alphabet);
            }
        }

        if (this.minLength > id.length()) {
            id.append(alphabet.charAt(0));
            while (this.minLength - id.length() > 0) {
                alphabet = shuffle(alphabet);
                id.append(alphabet, 0, Math.min(this.minLength - id.length(), alphabet.length()));
            }
        }

        if (isBlockedId(id.toString())) {
            id.setLength(0);
            id.append(encodeNumbers(numbers, increment + 1));
        }

        return id.toString();
    }

    private String shuffle(final String alphabet) {
        char[] chars = alphabet.toCharArray();
        int charLength = chars.length;
        for (int i = 0, j = charLength - 1; j > 0; i++, j--) {
            int r = (i * j + chars[i] + chars[j]) % charLength;
            char temp = chars[i];
            chars[i] = chars[r];
            chars[r] = temp;
        }

        return new String(chars);
    }

    private StringBuilder toId(long num, final String alphabet) {
        StringBuilder id = new StringBuilder();
        char[] chars = alphabet.toCharArray();
        int charLength = chars.length;

        do {
            id.append(chars[(int) (num % charLength)]);
            num /= charLength;
        } while (num > 0);

        return id.reverse();
    }

    private long toNumber(final String id, final int fromInclusive, final int toExclusive, final String alphabet) {
        int alphabetLength = alphabet.length();
        long number = 0;
        for (int i = fromInclusive; i < toExclusive; i++) {
            char c = id.charAt(i);
            number = number * alphabetLength + alphabet.indexOf(c);
        }
        return number;
    }

    private boolean isBlockedId(final String id) {
        final String lowercaseId = id.toLowerCase();
        final int lowercaseIdLength = lowercaseId.length();
        for (String word : this.blockList) {
            if (word.length() <= lowercaseIdLength) {
                if (lowercaseIdLength <= 3 || word.length() <= 3) {
                    if (lowercaseId.equals(word)) {
                        return true;
                    }
                } else if (Character.isDigit(word.charAt(0))) {
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

    /**
     * Default Sqids' {@code Builder}.
     */
    public static final class Builder {
        /**
         * Default Alphabet used by {@code Builder}.
         */
        public static final String DEFAULT_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        /**
         * Default Minimum length used by {@code Builder}.
         */
        public static final int DEFAULT_MIN_LENGTH = 0;

        /**
         * Default Block list used by {@code Builder}.
         *
         * Note: This is a Immutable Set.
         */
        public static final Set<String> DEFAULT_BLOCK_LIST = Collections.unmodifiableSet(Stream.of(
                "1d10t",
                "1d1ot",
                "1di0t",
                "1diot",
                "1eccacu10",
                "1eccacu1o",
                "1eccacul0",
                "1eccaculo",
                "1mbec11e",
                "1mbec1le",
                "1mbeci1e",
                "1mbecile",
                "a11upat0",
                "a11upato",
                "a1lupat0",
                "a1lupato",
                "aand",
                "ah01e",
                "ah0le",
                "aho1e",
                "ahole",
                "al1upat0",
                "al1upato",
                "allupat0",
                "allupato",
                "ana1",
                "ana1e",
                "anal",
                "anale",
                "anus",
                "arrapat0",
                "arrapato",
                "arsch",
                "arse",
                "ass",
                "b00b",
                "b00be",
                "b01ata",
                "b0ceta",
                "b0iata",
                "b0ob",
                "b0obe",
                "b0sta",
                "b1tch",
                "b1te",
                "b1tte",
                "ba1atkar",
                "balatkar",
                "bastard0",
                "bastardo",
                "batt0na",
                "battona",
                "bitch",
                "bite",
                "bitte",
                "bo0b",
                "bo0be",
                "bo1ata",
                "boceta",
                "boiata",
                "boob",
                "boobe",
                "bosta",
                "bran1age",
                "bran1er",
                "bran1ette",
                "bran1eur",
                "bran1euse",
                "branlage",
                "branler",
                "branlette",
                "branleur",
                "branleuse",
                "c0ck",
                "c0g110ne",
                "c0g11one",
                "c0g1i0ne",
                "c0g1ione",
                "c0gl10ne",
                "c0gl1one",
                "c0gli0ne",
                "c0glione",
                "c0na",
                "c0nnard",
                "c0nnasse",
                "c0nne",
                "c0u111es",
                "c0u11les",
                "c0u1l1es",
                "c0u1lles",
                "c0ui11es",
                "c0ui1les",
                "c0uil1es",
                "c0uilles",
                "c11t",
                "c11t0",
                "c11to",
                "c1it",
                "c1it0",
                "c1ito",
                "cabr0n",
                "cabra0",
                "cabrao",
                "cabron",
                "caca",
                "cacca",
                "cacete",
                "cagante",
                "cagar",
                "cagare",
                "cagna",
                "cara1h0",
                "cara1ho",
                "caracu10",
                "caracu1o",
                "caracul0",
                "caraculo",
                "caralh0",
                "caralho",
                "cazz0",
                "cazz1mma",
                "cazzata",
                "cazzimma",
                "cazzo",
                "ch00t1a",
                "ch00t1ya",
                "ch00tia",
                "ch00tiya",
                "ch0d",
                "ch0ot1a",
                "ch0ot1ya",
                "ch0otia",
                "ch0otiya",
                "ch1asse",
                "ch1avata",
                "ch1er",
                "ch1ng0",
                "ch1ngadaz0s",
                "ch1ngadazos",
                "ch1ngader1ta",
                "ch1ngaderita",
                "ch1ngar",
                "ch1ngo",
                "ch1ngues",
                "ch1nk",
                "chatte",
                "chiasse",
                "chiavata",
                "chier",
                "ching0",
                "chingadaz0s",
                "chingadazos",
                "chingader1ta",
                "chingaderita",
                "chingar",
                "chingo",
                "chingues",
                "chink",
                "cho0t1a",
                "cho0t1ya",
                "cho0tia",
                "cho0tiya",
                "chod",
                "choot1a",
                "choot1ya",
                "chootia",
                "chootiya",
                "cl1t",
                "cl1t0",
                "cl1to",
                "clit",
                "clit0",
                "clito",
                "cock",
                "cog110ne",
                "cog11one",
                "cog1i0ne",
                "cog1ione",
                "cogl10ne",
                "cogl1one",
                "cogli0ne",
                "coglione",
                "cona",
                "connard",
                "connasse",
                "conne",
                "cou111es",
                "cou11les",
                "cou1l1es",
                "cou1lles",
                "coui11es",
                "coui1les",
                "couil1es",
                "couilles",
                "cracker",
                "crap",
                "cu10",
                "cu1att0ne",
                "cu1attone",
                "cu1er0",
                "cu1ero",
                "cu1o",
                "cul0",
                "culatt0ne",
                "culattone",
                "culer0",
                "culero",
                "culo",
                "cum",
                "cunt",
                "d11d0",
                "d11do",
                "d1ck",
                "d1ld0",
                "d1ldo",
                "damn",
                "de1ch",
                "deich",
                "depp",
                "di1d0",
                "di1do",
                "dick",
                "dild0",
                "dildo",
                "dyke",
                "encu1e",
                "encule",
                "enema",
                "enf01re",
                "enf0ire",
                "enfo1re",
                "enfoire",
                "estup1d0",
                "estup1do",
                "estupid0",
                "estupido",
                "etr0n",
                "etron",
                "f0da",
                "f0der",
                "f0ttere",
                "f0tters1",
                "f0ttersi",
                "f0tze",
                "f0utre",
                "f1ca",
                "f1cker",
                "f1ga",
                "fag",
                "fica",
                "ficker",
                "figa",
                "foda",
                "foder",
                "fottere",
                "fotters1",
                "fottersi",
                "fotze",
                "foutre",
                "fr0c10",
                "fr0c1o",
                "fr0ci0",
                "fr0cio",
                "fr0sc10",
                "fr0sc1o",
                "fr0sci0",
                "fr0scio",
                "froc10",
                "froc1o",
                "froci0",
                "frocio",
                "frosc10",
                "frosc1o",
                "frosci0",
                "froscio",
                "fuck",
                "g00",
                "g0o",
                "g0u1ne",
                "g0uine",
                "gandu",
                "go0",
                "goo",
                "gou1ne",
                "gouine",
                "gr0gnasse",
                "grognasse",
                "haram1",
                "harami",
                "haramzade",
                "hund1n",
                "hundin",
                "id10t",
                "id1ot",
                "idi0t",
                "idiot",
                "imbec11e",
                "imbec1le",
                "imbeci1e",
                "imbecile",
                "j1zz",
                "jerk",
                "jizz",
                "k1ke",
                "kam1ne",
                "kamine",
                "kike",
                "leccacu10",
                "leccacu1o",
                "leccacul0",
                "leccaculo",
                "m1erda",
                "m1gn0tta",
                "m1gnotta",
                "m1nch1a",
                "m1nchia",
                "m1st",
                "mam0n",
                "mamahuev0",
                "mamahuevo",
                "mamon",
                "masturbat10n",
                "masturbat1on",
                "masturbate",
                "masturbati0n",
                "masturbation",
                "merd0s0",
                "merd0so",
                "merda",
                "merde",
                "merdos0",
                "merdoso",
                "mierda",
                "mign0tta",
                "mignotta",
                "minch1a",
                "minchia",
                "mist",
                "musch1",
                "muschi",
                "n1gger",
                "neger",
                "negr0",
                "negre",
                "negro",
                "nerch1a",
                "nerchia",
                "nigger",
                "orgasm",
                "p00p",
                "p011a",
                "p01la",
                "p0l1a",
                "p0lla",
                "p0mp1n0",
                "p0mp1no",
                "p0mpin0",
                "p0mpino",
                "p0op",
                "p0rca",
                "p0rn",
                "p0rra",
                "p0uff1asse",
                "p0uffiasse",
                "p1p1",
                "p1pi",
                "p1r1a",
                "p1rla",
                "p1sc10",
                "p1sc1o",
                "p1sci0",
                "p1scio",
                "p1sser",
                "pa11e",
                "pa1le",
                "pal1e",
                "palle",
                "pane1e1r0",
                "pane1e1ro",
                "pane1eir0",
                "pane1eiro",
                "panele1r0",
                "panele1ro",
                "paneleir0",
                "paneleiro",
                "patakha",
                "pec0r1na",
                "pec0rina",
                "pecor1na",
                "pecorina",
                "pen1s",
                "pendej0",
                "pendejo",
                "penis",
                "pip1",
                "pipi",
                "pir1a",
                "pirla",
                "pisc10",
                "pisc1o",
                "pisci0",
                "piscio",
                "pisser",
                "po0p",
                "po11a",
                "po1la",
                "pol1a",
                "polla",
                "pomp1n0",
                "pomp1no",
                "pompin0",
                "pompino",
                "poop",
                "porca",
                "porn",
                "porra",
                "pouff1asse",
                "pouffiasse",
                "pr1ck",
                "prick",
                "pussy",
                "put1za",
                "puta",
                "puta1n",
                "putain",
                "pute",
                "putiza",
                "puttana",
                "queca",
                "r0mp1ba11e",
                "r0mp1ba1le",
                "r0mp1bal1e",
                "r0mp1balle",
                "r0mpiba11e",
                "r0mpiba1le",
                "r0mpibal1e",
                "r0mpiballe",
                "rand1",
                "randi",
                "rape",
                "recch10ne",
                "recch1one",
                "recchi0ne",
                "recchione",
                "retard",
                "romp1ba11e",
                "romp1ba1le",
                "romp1bal1e",
                "romp1balle",
                "rompiba11e",
                "rompiba1le",
                "rompibal1e",
                "rompiballe",
                "ruff1an0",
                "ruff1ano",
                "ruffian0",
                "ruffiano",
                "s1ut",
                "sa10pe",
                "sa1aud",
                "sa1ope",
                "sacanagem",
                "sal0pe",
                "salaud",
                "salope",
                "saugnapf",
                "sb0rr0ne",
                "sb0rra",
                "sb0rrone",
                "sbattere",
                "sbatters1",
                "sbattersi",
                "sborr0ne",
                "sborra",
                "sborrone",
                "sc0pare",
                "sc0pata",
                "sch1ampe",
                "sche1se",
                "sche1sse",
                "scheise",
                "scheisse",
                "schlampe",
                "schwachs1nn1g",
                "schwachs1nnig",
                "schwachsinn1g",
                "schwachsinnig",
                "schwanz",
                "scopare",
                "scopata",
                "sexy",
                "sh1t",
                "shit",
                "slut",
                "sp0mp1nare",
                "sp0mpinare",
                "spomp1nare",
                "spompinare",
                "str0nz0",
                "str0nza",
                "str0nzo",
                "stronz0",
                "stronza",
                "stronzo",
                "stup1d",
                "stupid",
                "succh1am1",
                "succh1ami",
                "succhiam1",
                "succhiami",
                "sucker",
                "t0pa",
                "tapette",
                "test1c1e",
                "test1cle",
                "testic1e",
                "testicle",
                "tette",
                "topa",
                "tr01a",
                "tr0ia",
                "tr0mbare",
                "tr1ng1er",
                "tr1ngler",
                "tring1er",
                "tringler",
                "tro1a",
                "troia",
                "trombare",
                "turd",
                "twat",
                "vaffancu10",
                "vaffancu1o",
                "vaffancul0",
                "vaffanculo",
                "vag1na",
                "vagina",
                "verdammt",
                "verga",
                "w1chsen",
                "wank",
                "wichsen",
                "x0ch0ta",
                "x0chota",
                "xana",
                "xoch0ta",
                "xochota",
                "z0cc01a",
                "z0cc0la",
                "z0cco1a",
                "z0ccola",
                "z1z1",
                "z1zi",
                "ziz1",
                "zizi",
                "zocc01a",
                "zocc0la",
                "zocco1a",
                "zoccola").collect(Collectors.toSet()));

        private String alphabet = DEFAULT_ALPHABET;
        private int minLength = DEFAULT_MIN_LENGTH;
        private Set<String> blockList = DEFAULT_BLOCK_LIST;

        /**
         * Set {@code Builder}'s alphabet.
         *
         * @param alphabet The new {@code Builder}'s alphabet
         * @return this {@code Builder} object
         */
        public Builder alphabet(final String alphabet) {
            if (alphabet != null) {
                this.alphabet = alphabet;
            }
            return this;
        }

        /**
         * Set {@code Builder}'s minimum length.
         *
         * @param minLength The new {@code Builder}'s minimum length.
         * @return this {@code Builder} object
         */
        public Builder minLength(final int minLength) {
            this.minLength = minLength;
            return this;
        }

        /**
         * Set {@code Builder}'s block list.
         *
         * @param blockList The new {@code Builder}'s block list. A copy will be created.
         * @return this {@code Builder} object
         */
        public Builder blockList(final Set<String> blockList) {
            if (blockList != null) {
                this.blockList = Collections.unmodifiableSet(new HashSet<>(blockList));
            }
            return this;
        }

        /**
         * Returns a newly-created {@code Sqids} based on the contents of this {@code Builder}.
         *
         * @return New Sqids instance.
         */
        public Sqids build() {
            return new Sqids(this);
        }
    }
}
