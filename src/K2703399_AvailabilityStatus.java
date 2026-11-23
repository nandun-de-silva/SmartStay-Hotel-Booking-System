/**
 * Enumeration of possible availability states for a SmartStay hotel room.
 *
 * Using an enum instead of a String ensures only valid statuses can be
 * assigned to a room, preventing data-integrity errors.
 *
 * @author  K.H. Nandun Lakshan De Silva
 * @id      K2703399
 * @module  CI6115 Programming III
 */
public enum K2703399_AvailabilityStatus {

    AVAILABLE        ("Available"),
    RESERVED         ("Reserved"),
    UNDER_MAINTENANCE("Under Maintenance");

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------
    private final String displayName;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------
    K2703399_AvailabilityStatus(String displayName) {
        this.displayName = displayName;
    }

    // -----------------------------------------------------------------------
    // Getter
    // -----------------------------------------------------------------------
    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}
