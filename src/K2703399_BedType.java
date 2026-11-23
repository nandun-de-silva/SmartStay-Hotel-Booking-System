/**
 * Enumeration of bed types available in SmartStay Hotel rooms.
 *
 * Provides a type-safe, readable representation of bed configurations
 * rather than error-prone plain String comparisons.
 *
 * @author  K.H. Nandun Lakshan De Silva
 * @id      K2703399
 * @module  CI6115 Programming III
 */
public enum K2703399_BedType {

    SINGLE("Single"),
    DOUBLE("Double"),
    QUEEN ("Queen"),
    KING  ("King");

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------
    private final String displayName;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------
    K2703399_BedType(String displayName) {
        this.displayName = displayName;
    }

    // -----------------------------------------------------------------------
    // Getter
    // -----------------------------------------------------------------------
    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}
