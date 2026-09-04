package generator.data;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.List;
import java.util.function.Predicate;

/**
 * Enum representing the encoding type used by the QR code
 */
public enum EncodingType {
    NUMERIC(3, EncodingType::isSimpleDigit),
    ALPHANUMERIC(2, EncodingType::isSimpleLetter),
    BYTE(1, (c) -> true),
    KANJI(0, EncodingType::isKanji);

    private final Predicate<Character> canEncodeChar;
    private final int indicatorBit;

    private static final List<Character> alphaNumericCharacters = List.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
            'X', 'Y', 'Z', ' ', '$', '%', '*', '+', '-', '.', '/', ':');

    EncodingType(int indicatorBit, Predicate<Character> canEncodeChar) {
        this.canEncodeChar = canEncodeChar;
        this.indicatorBit = indicatorBit;
    }

    /**
     * Checks if the encoding type can encode the character provided.
     * @param character The character to test
     * @return Boolean representing if the character can be encoded by this encoding type
     */
    public boolean canEncodeChar(char character) {
        return canEncodeChar.test(character);
    }

    /**
     * Provides the index of the set bit in the 4-bit number representing the encoding type, starting at the most significant bit
     * @return Index of the set bit
     */
    public int getIndicatorBit() {
        return indicatorBit;
    }

    private static boolean isSimpleDigit(char c) {
        List<Character> digits = List.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');

        return digits.contains(c);
    }

    private static boolean isSimpleLetter(char c) {
        return alphaNumericCharacters.contains(c) || isSimpleDigit(c);
    }

    private static final CharsetEncoder ENCODER = Charset.forName("Shift_JIS").newEncoder();
    private static boolean isKanji(char ch) {
        if (!ENCODER.canEncode(ch)) {
            return false;
        }

        Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
        if (block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
            return true;
        } else if (block == Character.UnicodeBlock.HIRAGANA) {
            return true;
        } else return block == Character.UnicodeBlock.KATAKANA;
    }
}
