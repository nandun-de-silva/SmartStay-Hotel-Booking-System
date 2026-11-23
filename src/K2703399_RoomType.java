/**
 * Enumeration of the room packages offered by SmartStay Hotel.
 *
 * The enum is only a label: it is used to build the room-type menus, to
 * filter room searches and to tell K2703399_Room.createRoom() which subclass
 * to build. The package values (rate, tax, extra guest charge, capacity and
 * breakfast) belong to the room subclasses, so they are defined in one place.
 *
 * OOP concept demonstrated: Encapsulation (display name kept private and
 * read through a getter).
 *
 * @author  K.H. Nandun Lakshan De Silva
 * @id      K2703399
 * @module  CI6115 Programming III
 */
public enum K2703399_RoomType {

    // -----------------------------------------------------------------------
    // Constants  (displayName)
    // -----------------------------------------------------------------------
    STANDARD    ("Standard"),
    DELUXE      ("Deluxe"),
    SUITE       ("Suite"),
    FAMILY_SUITE("Family Suite");

    // -----------------------------------------------------------------------
    // Field
    // -----------------------------------------------------------------------
    private final String displayName;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------
    K2703399_RoomType(String displayName) {
        this.displayName = displayName;
    }

    // -----------------------------------------------------------------------
    // Getter
    // -----------------------------------------------------------------------
    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}
