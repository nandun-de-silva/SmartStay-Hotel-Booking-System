import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a room reservation made by a guest at SmartStay Hotel.
 *
 * OOP concepts demonstrated:
 *   - Encapsulation  : all fields private; the booking ID, guest and room are
 *                      final, and dates/guests change only via updateBooking().
 *   - Association    : refers to one K2703399_Guest and one K2703399_Room.
 *                      They are not parts of the booking and exist
 *                      independently of it (directed association).
 *   - Dependency     : generateInvoice() creates and returns a K2703399_Invoice
 *                      without storing it («create» dependency).
 *
 * Business rules enforced:
 *   - Modification and cancellation are only allowed more than 24 hours
 *     before check-in.
 *   - A 12% discount is applied on the base room charge for stays of 5+ nights.
 *   - A refundable booking advance of LKR 10,000 is deducted from the final bill.
 *
 * @author  K.H. Nandun Lakshan De Silva
 * @id      K2703399
 * @module  CI6115 Programming III
 */
public class K2703399_Reservation {

    // -----------------------------------------------------------------------
    // Business-rule constants
    // -----------------------------------------------------------------------
    // Public so the user interface can show the same amount it is charged
    public  static final double BOOKING_ADVANCE         = 10_000.0; // LKR
    private static final double LONG_STAY_DISCOUNT_RATE = 0.12;     // 12 %
    private static final int    LONG_STAY_THRESHOLD     = 5;        // nights
    private static final int    CHANGE_CUTOFF_DAYS      = 2;        // "more than 24 h"

    // -----------------------------------------------------------------------
    // Private fields (Encapsulation)
    // -----------------------------------------------------------------------
    private final String         bookingId;
    private final K2703399_Guest  guest;         // Association — guest exists independently
    private final K2703399_Room   room;          // Association — room exists independently
    private       LocalDate      checkInDate;   // can change through updateBooking()
    private       LocalDate      checkOutDate;  // can change through updateBooking()
    private       int            numberOfGuests;// can change through updateBooking()
    private final LocalDate      bookingDate;   // date on which the booking was created
    private       boolean        isCancelled;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates a new reservation.
     * All validation is performed by K2703399_HotelSystem before this is called.
     *
     * @param bookingId      system-generated booking reference (e.g. BK00001)
     * @param guest          the registered guest making the reservation
     * @param room           the room being reserved
     * @param checkInDate    intended check-in date
     * @param checkOutDate   intended check-out date
     * @param numberOfGuests number of guests who will occupy the room
     */
    public K2703399_Reservation(String bookingId, K2703399_Guest guest, K2703399_Room room,
                               LocalDate checkInDate, LocalDate checkOutDate,
                               int numberOfGuests) {
        this.bookingId      = bookingId;
        this.guest          = guest;
        this.room           = room;
        this.checkInDate    = checkInDate;
        this.checkOutDate   = checkOutDate;
        this.numberOfGuests = numberOfGuests;
        this.bookingDate    = LocalDate.now();
        this.isCancelled    = false;
    }

    // -----------------------------------------------------------------------
    // Business logic methods
    // -----------------------------------------------------------------------

    /**
     * Calculates the total number of nights for this reservation.
     *
     * @return number of nights between check-in and check-out
     */
    public long calculateNights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    /**
     * Determines whether this reservation can still be modified or cancelled.
     *
     * Rule: a reservation cannot be modified or cancelled within 24 hours of
     * the check-in date. Since dates (not times) are used, "within 24 hours"
     * is modelled as "check-in is today or tomorrow" — i.e., fewer than
     * 2 days away.
     *
     * @return true if the reservation can be modified or cancelled
     */
    public boolean isEligibleForChange() {
        if (isCancelled) return false; // already cancelled
        long daysUntilCheckIn = ChronoUnit.DAYS.between(LocalDate.now(), checkInDate);
        return daysUntilCheckIn >= CHANGE_CUTOFF_DAYS; // must be more than 24 h away
    }

    /**
     * Changes the dates and number of guests of this reservation.
     * All validation is performed by K2703399_HotelSystem before this is called.
     *
     * @param newCheckIn     new check-in date
     * @param newCheckOut    new check-out date
     * @param newGuestCount  new number of guests
     */
    public void updateBooking(LocalDate newCheckIn, LocalDate newCheckOut, int newGuestCount) {
        this.checkInDate    = newCheckIn;
        this.checkOutDate   = newCheckOut;
        this.numberOfGuests = newGuestCount;
    }

    /**
     * Generates a fully-calculated invoice for this reservation.
     *
     * Calculation steps (per case-study brief):
     *   1. Base room charge    = daily rate x nights
     *   2. Extra guest charge  = (guests - base capacity) x charge/guest x nights
     *   3. Discount (12%)      = applied to base room charge only, if nights >= 5
     *   4. Tax                 = (discounted base + extra guest charge) x tax rate
     *   5. Final payable       = taxable subtotal + tax - booking advance (LKR 10,000)
     *
     * OOP concept: «create» dependency — this method creates and returns a new
     * K2703399_Invoice but does not store it as a field of the reservation.
     *
     * @param invoiceId system-generated invoice identifier
     * @return a fully populated K2703399_Invoice object
     */
    public K2703399_Invoice generateInvoice(String invoiceId) {

        long nights = calculateNights();

        // Step 1: Base room charge
        double baseRoomCharge = room.getDailyRate() * nights;

        // Step 2: Extra guest charge (per additional guest per night)
        double extraGuestCharge = 0.0;
        int extraGuests = numberOfGuests - room.getBaseCapacity();
        if (extraGuests > 0) {
            extraGuestCharge = extraGuests * room.getExtraGuestChargePerGuest() * nights;
        }

        // Step 3: Long-stay discount — applied to base room charge only
        boolean discountApplied = nights >= LONG_STAY_THRESHOLD;
        double  discountAmount  = discountApplied
                                  ? baseRoomCharge * LONG_STAY_DISCOUNT_RATE
                                  : 0.0;
        double discountedBase   = baseRoomCharge - discountAmount;

        // Step 4: Tax on (discounted base + extra guest charges)
        double taxableSubtotal = discountedBase + extraGuestCharge;
        double taxAmount       = taxableSubtotal * room.getTaxRate();

        // Step 5: Deduct refundable booking advance
        double totalBeforeAdvance = taxableSubtotal + taxAmount;
        double finalPayable       = Math.max(0.0, totalBeforeAdvance - BOOKING_ADVANCE);

        // Create and return the Invoice («create» dependency)
        return new K2703399_Invoice(
            invoiceId, bookingId,
            guest, room,
            bookingDate,
            checkInDate, checkOutDate,
            nights, numberOfGuests,
            baseRoomCharge, extraGuestCharge,
            discountAmount, taxAmount,
            BOOKING_ADVANCE, finalPayable,
            discountApplied
        );
    }

    // -----------------------------------------------------------------------
    // Getters and Setters (Encapsulation)
    // -----------------------------------------------------------------------

    public String        getBookingId()      { return bookingId; }
    public K2703399_Guest getGuest()          { return guest; }
    public K2703399_Room  getRoom()           { return room; }
    public LocalDate     getCheckInDate()    { return checkInDate; }
    public LocalDate     getCheckOutDate()   { return checkOutDate; }
    public int           getNumberOfGuests() { return numberOfGuests; }
    public LocalDate     getBookingDate()    { return bookingDate; }
    public boolean       isCancelled()       { return isCancelled; }

    public void setCancelled(boolean cancelled) { this.isCancelled = cancelled; }

    // -----------------------------------------------------------------------
    // toString
    // -----------------------------------------------------------------------
    @Override
    public String toString() {
        return String.format(
            "Booking %-8s | Guest: %-22s | Room: %-5s | Check-In: %s | Check-Out: %s | Guests: %d | Status: %s",
            bookingId, guest.getFullName(), room.getRoomId(),
            checkInDate, checkOutDate, numberOfGuests,
            isCancelled ? "CANCELLED" : "ACTIVE"
        );
    }
}
