package generator.data;

/**
 * Enum representing the error correction level
 */
public enum ErrorCorrectionLevel {
    LOW(1),
    MEDIUM(0),
    QUARTILE(3),
    HIGH(2);

    private final int ID;
    ErrorCorrectionLevel(int id) {
        this.ID = id;
    }

    /**
     * Getter for the numerical value assigned to the EC level
     * @return Numerical value of EC level
     */
    public int getId() {
        return ID;
    }
}
