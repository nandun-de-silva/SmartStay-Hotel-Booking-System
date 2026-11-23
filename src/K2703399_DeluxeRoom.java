/**
 * Deluxe room package — breakfast included (inherited default).
 *
 * OOP concepts demonstrated:
 *   - Inheritance  : extends the abstract class K2703399_Room and reuses its
 *                    fields (roomId, bedType, availabilityStatus) and methods.
 *   - Polymorphism : overrides the abstract package methods of K2703399_Room
 *                    (method overriding — runtime polymorphism).
 *   - Encapsulation: the package values are private constants of this class.
 *
 * Package (case study): daily rate LKR 9,000, extra guest charge LKR 1,500
 * per guest per night, tax 12%, 2 guests included.
 *
 * @author  K.H. Nandun Lakshan De Silva
 * @id      K2703399
 * @module  CI6115 Programming III
 */
public class K2703399_DeluxeRoom extends K2703399_Room {

    // -----------------------------------------------------------------------
    // Package values (private constants)
    // -----------------------------------------------------------------------
    private static final double DAILY_RATE         = 9000.0;
    private static final double EXTRA_GUEST_CHARGE = 1500.0;
    private static final double TAX_RATE           = 0.12;
    private static final int    BASE_CAPACITY      = 2;

    // -----------------------------------------------------------------------
    // Constructors (Constructor Overloading) — call the parent with super()
    // -----------------------------------------------------------------------

    /** Creates the room with AVAILABLE status. */
    public K2703399_DeluxeRoom(String roomId, K2703399_BedType bedType) {
        super(roomId, bedType);
    }

    /** Creates the room with the given availability status. */
    public K2703399_DeluxeRoom(String roomId, K2703399_BedType bedType,
                              K2703399_AvailabilityStatus status) {
        super(roomId, bedType, status);
    }

    // -----------------------------------------------------------------------
    // Overridden methods (Polymorphism)
    // -----------------------------------------------------------------------
    @Override public K2703399_RoomType getRoomType()          { return K2703399_RoomType.DELUXE; }
    @Override public double  getDailyRate()                  { return DAILY_RATE; }
    @Override public double  getTaxRate()                    { return TAX_RATE; }
    @Override public double  getExtraGuestChargePerGuest()   { return EXTRA_GUEST_CHARGE; }
    @Override public int     getBaseCapacity()               { return BASE_CAPACITY; }
}
