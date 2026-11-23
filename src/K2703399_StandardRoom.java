/**
 * Standard room package — the only package without complimentary breakfast.
 *
 * OOP concepts demonstrated:
 *   - Inheritance  : extends the abstract class K2703399_Room and reuses its
 *                    fields (roomId, bedType, availabilityStatus) and methods.
 *   - Polymorphism : overrides the abstract package methods of K2703399_Room
 *                    (method overriding — runtime polymorphism).
 *   - Encapsulation: the package values are private constants of this class.
 *
 * Package (case study): daily rate LKR 6,000, extra guest charge LKR 1,000
 * per guest per night, tax 10%, 2 guests included.
 *
 * @author  K.H. Nandun Lakshan De Silva
 * @id      K2703399
 * @module  CI6115 Programming III
 */
public class K2703399_StandardRoom extends K2703399_Room {

    // -----------------------------------------------------------------------
    // Package values (private constants)
    // -----------------------------------------------------------------------
    private static final double DAILY_RATE         = 6000.0;
    private static final double EXTRA_GUEST_CHARGE = 1000.0;
    private static final double TAX_RATE           = 0.10;
    private static final int    BASE_CAPACITY      = 2;

    // -----------------------------------------------------------------------
    // Constructors (Constructor Overloading) — call the parent with super()
    // -----------------------------------------------------------------------

    /** Creates the room with AVAILABLE status. */
    public K2703399_StandardRoom(String roomId, K2703399_BedType bedType) {
        super(roomId, bedType);
    }

    /** Creates the room with the given availability status. */
    public K2703399_StandardRoom(String roomId, K2703399_BedType bedType,
                                K2703399_AvailabilityStatus status) {
        super(roomId, bedType, status);
    }

    // -----------------------------------------------------------------------
    // Overridden methods (Polymorphism)
    // -----------------------------------------------------------------------
    @Override public K2703399_RoomType getRoomType()          { return K2703399_RoomType.STANDARD; }
    @Override public double  getDailyRate()                  { return DAILY_RATE; }
    @Override public double  getTaxRate()                    { return TAX_RATE; }
    @Override public double  getExtraGuestChargePerGuest()   { return EXTRA_GUEST_CHARGE; }
    @Override public int     getBaseCapacity()               { return BASE_CAPACITY; }

    /** Standard rooms do not include breakfast, so the inherited default is overridden. */
    @Override public boolean hasComplimentaryBreakfast()     { return false; }
}
