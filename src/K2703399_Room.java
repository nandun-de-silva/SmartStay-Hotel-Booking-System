/**
 * Abstract base class for every hotel room in the SmartStay system.
 *
 * OOP concepts demonstrated:
 *   - Abstraction   : the class is abstract. It holds what every room has in
 *                     common (ID, bed, status) and declares the package values
 *                     (rate, tax, extra guest charge, capacity) as abstract
 *                     methods, because only a concrete room package knows them.
 *   - Inheritance   : K2703399_StandardRoom, K2703399_DeluxeRoom,
 *                     K2703399_SuiteRoom and K2703399_FamilySuiteRoom extend
 *                     this class and reuse its fields and methods.
 *   - Polymorphism  : K2703399_Reservation and K2703399_Invoice call
 *                     getDailyRate(), getTaxRate() etc. on a K2703399_Room
 *                     reference; the subclass version runs at runtime.
 *   - Encapsulation : fields are protected (visible to subclasses only, not
 *                     to other classes) and reached through getters/setters.
 *   - Constructor Overloading : two constructors — a short form that creates
 *                     the room as AVAILABLE and a full form with a status.
 *
 * Business rule: the daily rate is room-type specific (case study), so each
 * subclass fixes its own rate and there is no rate setter. A room's type is
 * its class, so changing the type replaces the object (see createRoom()).
 *
 * Relationships:
 *   - Association with K2703399_BedType            (bed configuration)
 *   - Association with K2703399_AvailabilityStatus (current availability state)
 *   - Dependency on K2703399_RoomType              (label returned by getRoomType())
 *
 * Assumption: maximum occupancy is base capacity + 2 for every room type.
 * Extra guest charge applies per additional guest per night.
 *
 * @author  K.H. Nandun Lakshan De Silva
 * @id      K2703399
 * @module  CI6115 Programming III
 */
public abstract class K2703399_Room {

    // -----------------------------------------------------------------------
    // Constant shared by every room type
    // -----------------------------------------------------------------------
    protected static final int MAX_EXTRA_GUESTS = 2; // guests allowed above base capacity

    // -----------------------------------------------------------------------
    // Protected fields (Encapsulation — visible to subclasses only)
    // -----------------------------------------------------------------------
    protected final String                      roomId;
    protected K2703399_BedType                  bedType;
    protected K2703399_AvailabilityStatus       availabilityStatus;

    // -----------------------------------------------------------------------
    // Constructors (Constructor Overloading — called by subclasses via super())
    // -----------------------------------------------------------------------

    /**
     * Short constructor — the room is created with AVAILABLE status.
     *
     * @param roomId  unique room identifier (e.g. "R001")
     * @param bedType bed configuration
     */
    protected K2703399_Room(String roomId, K2703399_BedType bedType) {
        this(roomId, bedType, K2703399_AvailabilityStatus.AVAILABLE);
    }

    /**
     * Full constructor — also sets the initial availability status.
     *
     * @param roomId  unique room identifier
     * @param bedType bed configuration
     * @param status  initial availability status
     */
    protected K2703399_Room(String roomId, K2703399_BedType bedType,
                            K2703399_AvailabilityStatus status) {
        this.roomId             = roomId;
        this.bedType            = bedType;
        this.availabilityStatus = status;
    }

    // -----------------------------------------------------------------------
    // Factory method — builds the subclass that matches a room type
    // -----------------------------------------------------------------------

    /**
     * Creates a room of the given type. K2703399_HotelSystem uses this in
     * addRoom() and updateRoom(), so it never needs to name the subclasses.
     *
     * @param roomId   unique room identifier
     * @param roomType room package chosen by the admin
     * @param bedType  bed configuration
     * @param status   initial availability status
     * @return a new K2703399_StandardRoom, K2703399_DeluxeRoom,
     *         K2703399_SuiteRoom or K2703399_FamilySuiteRoom
     */
    public static K2703399_Room createRoom(String roomId, K2703399_RoomType roomType,
                                           K2703399_BedType bedType,
                                           K2703399_AvailabilityStatus status) {
        switch (roomType) {
            case STANDARD:     return new K2703399_StandardRoom(roomId, bedType, status);
            case DELUXE:       return new K2703399_DeluxeRoom(roomId, bedType, status);
            case SUITE:        return new K2703399_SuiteRoom(roomId, bedType, status);
            case FAMILY_SUITE: return new K2703399_FamilySuiteRoom(roomId, bedType, status);
            default:           throw new IllegalArgumentException("Unknown room type: " + roomType);
        }
    }

    // -----------------------------------------------------------------------
    // Abstract methods — each room package supplies its own values
    // -----------------------------------------------------------------------

    /** @return the room package of this room (used for search and display) */
    public abstract K2703399_RoomType getRoomType();

    /** @return daily rate in LKR */
    public abstract double getDailyRate();

    /** @return tax rate (e.g. 0.10 for 10%) */
    public abstract double getTaxRate();

    /** @return extra charge per additional guest per night (LKR) */
    public abstract double getExtraGuestChargePerGuest();

    /** @return number of guests included without extra charge */
    public abstract int getBaseCapacity();

    // -----------------------------------------------------------------------
    // Concrete methods — inherited by every subclass
    // -----------------------------------------------------------------------

    /**
     * Most packages include breakfast, so the default is true.
     * K2703399_StandardRoom overrides this method to return false.
     *
     * @return true if complimentary breakfast is included
     */
    public boolean hasComplimentaryBreakfast() { return true; }

    /** @return maximum number of guests allowed in this room */
    public int getMaxOccupancy() { return getBaseCapacity() + MAX_EXTRA_GUESTS; }

    /** @return true if this room's status is AVAILABLE */
    public boolean isAvailable() {
        return availabilityStatus == K2703399_AvailabilityStatus.AVAILABLE;
    }

    // -----------------------------------------------------------------------
    // Getters and Setters (Encapsulation)
    // -----------------------------------------------------------------------

    public String           getRoomId()                          { return roomId; }

    public K2703399_BedType getBedType()                         { return bedType; }
    public void             setBedType(K2703399_BedType bedType) { this.bedType = bedType; }

    public K2703399_AvailabilityStatus getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(K2703399_AvailabilityStatus status) {
        this.availabilityStatus = status;
    }

    // -----------------------------------------------------------------------
    // toString — calls the abstract methods, so each subclass prints its own values
    // -----------------------------------------------------------------------
    @Override
    public String toString() {
        return String.format(
            "Room %-5s | %-12s | %-6s Bed | LKR %,9.2f/night | Tax %2.0f%% | Max %d guests | %-18s | Breakfast: %-3s",
            roomId,
            getRoomType().getDisplayName(),
            bedType.getDisplayName(),
            getDailyRate(),
            getTaxRate() * 100,
            getMaxOccupancy(),
            availabilityStatus.getDisplayName(),
            hasComplimentaryBreakfast() ? "Yes" : "No"
        );
    }
}
