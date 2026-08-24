package generator.data;

/**
 * Enum representing types of XOR masks used by the QR codes
 */
public enum MaskType {
    AUTO(-1, (a, b) -> true),
    ZERO(0, (a, b) -> ((a + b) & 1) == 0),
    ONE(1, (a, b) -> (b & 1) == 0),
    TWO(2, (a, b) -> a % 3 == 0),
    THREE(3, (a, b) -> (a + b) % 3 == 0),
    FOUR(4, (a, b) -> (b / 2 + a / 3) % 2 == 0),
    FIVE(5, (a, b) -> a * b % 2 + a * b % 3 == 0),
    SIX(6, (a, b) -> ((a * b) % 2 + a * b % 3) % 2 == 0),
    SEVEN(7, (a, b) -> ((a + b) % 2 + a * b % 3) % 2 == 0);

    final int value;
    final PatternPredicate pattern;

    MaskType(int value, PatternPredicate pattern) {
        this.value = value;
        this.pattern = pattern;
    }

    /**
     * Getter for the numerical value of the mask
     * @return Numerical value
     */
    public int getValue() {
        return value;
    }

    /**
     * Getter for a predicate capable of generating the mask pattern
     * @return Pattern generator predicate
     */
    public PatternPredicate getPattern() {
        return pattern;
    }
}
