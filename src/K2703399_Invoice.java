import java.time.LocalDate;

/**
 * Represents a detailed billing invoice for a SmartStay reservation.
 *
 * OOP concept demonstrated:
 *   - Dependency «create» : an Invoice is created by
 *     K2703399_Reservation.generateInvoice() and returned to the caller; the
 *     reservation does not store it, so each call produces a fresh invoice.
 *   - Encapsulation : all charge fields are private; read via getters.
 *
 * Billing formula (per the case-study brief):
 *   1. Base room charge  = daily rate x number of nights
 *   2. Extra guest charge = (guests beyond base capacity) x rate x nights
 *   3. Discount (12%)    = applied to base room charge only, if stay >= 5 nights
 *   4. Tax               = (discounted base + extra guest charge) x tax rate
 *   5. Final payable     = subtotal + tax - booking advance (LKR 10,000)
 *
 * Assumption: extra guest charge is applied per extra guest per night,
 * consistent with the per-night nature of the daily room rate.
 *
 * @author  K.H. Nandun Lakshan De Silva
 * @id      K2703399
 * @module  CI6115 Programming III
 */
public class K2703399_Invoice {

    // -----------------------------------------------------------------------
    // Private fields (Encapsulation)
    // -----------------------------------------------------------------------
    private final String         invoiceId;
    private final String         bookingId;
    private final K2703399_Guest  guest;          // reference held for display only
    private final K2703399_Room   room;           // reference held for display only
    private final LocalDate      bookingDate;
    private final LocalDate      checkInDate;
    private final LocalDate      checkOutDate;
    private final long           numberOfNights;
    private final int            numberOfGuests;
    private final double         baseRoomCharge;
    private final double         extraGuestCharge;
    private final double         discountAmount;
    private final double         taxAmount;
    private final double         bookingAdvanceDeducted;
    private final double         finalPayable;
    private final boolean        discountApplied;

    // -----------------------------------------------------------------------
    // Constructor — called by K2703399_Reservation.generateInvoice()
    // -----------------------------------------------------------------------

    /**
     * Constructs a fully-calculated invoice.
     * All monetary values are computed by K2703399_Reservation.generateInvoice()
     * before being passed here; the invoice only stores and prints them.
     */
    public K2703399_Invoice(String invoiceId, String bookingId,
                           K2703399_Guest guest, K2703399_Room room,
                           LocalDate bookingDate,
                           LocalDate checkInDate, LocalDate checkOutDate,
                           long numberOfNights, int numberOfGuests,
                           double baseRoomCharge, double extraGuestCharge,
                           double discountAmount, double taxAmount,
                           double bookingAdvanceDeducted, double finalPayable,
                           boolean discountApplied) {
        this.invoiceId              = invoiceId;
        this.bookingId              = bookingId;
        this.guest                  = guest;
        this.room                   = room;
        this.bookingDate            = bookingDate;
        this.checkInDate            = checkInDate;
        this.checkOutDate           = checkOutDate;
        this.numberOfNights         = numberOfNights;
        this.numberOfGuests         = numberOfGuests;
        this.baseRoomCharge         = baseRoomCharge;
        this.extraGuestCharge       = extraGuestCharge;
        this.discountAmount         = discountAmount;
        this.taxAmount              = taxAmount;
        this.bookingAdvanceDeducted = bookingAdvanceDeducted;
        this.finalPayable           = finalPayable;
        this.discountApplied        = discountApplied;
    }

    // -----------------------------------------------------------------------
    // Public method: formatted invoice printout
    // -----------------------------------------------------------------------

    /**
     * Prints a complete, formatted invoice to standard output.
     * Shows all billing components: room details, guest details,
     * charge breakdown, and the final payable amount.
     */
    public void printInvoice() {
        String thick  = "=".repeat(64);
        String thin   = "-".repeat(64);

        System.out.println();
        System.out.println(thick);
        System.out.println("          SMARTSTAY HOTEL - BOOKING INVOICE");
        System.out.println(thick);
        System.out.printf("  Invoice No  : %s%n", invoiceId);
        System.out.printf("  Booking ID  : %s%n", bookingId);
        System.out.println(thin);

        // Guest details
        System.out.println("  GUEST DETAILS");
        System.out.printf("  Name         : %s%n",  guest.getFullName());
        System.out.printf("  NIC/Passport : %s%n",  guest.getNicPassport());
        System.out.printf("  Contact      : %s%n",  guest.getContactNumber());
        System.out.printf("  Email        : %s%n",  guest.getEmail());
        System.out.println(thin);

        // Room details
        System.out.println("  ROOM DETAILS");
        System.out.printf("  Room ID      : %s%n",  room.getRoomId());
        System.out.printf("  Room Type    : %s%n",  room.getRoomType().getDisplayName());
        System.out.printf("  Bed Type     : %s%n",  room.getBedType().getDisplayName());
        System.out.printf("  Daily Rate   : LKR %,.2f%n", room.getDailyRate());
        System.out.printf("  Breakfast    : %s%n",
            room.hasComplimentaryBreakfast() ? "Complimentary" : "Not Included");
        System.out.println(thin);

        // Booking details
        System.out.println("  BOOKING DETAILS");
        System.out.printf("  Booked On    : %s%n",  bookingDate);
        System.out.printf("  Check-In     : %s%n",  checkInDate);
        System.out.printf("  Check-Out    : %s%n",  checkOutDate);
        System.out.printf("  Nights       : %d%n",  numberOfNights);
        System.out.printf("  Guests       : %d%n",  numberOfGuests);
        System.out.println(thin);

        // Charge breakdown
        System.out.println("  CHARGE BREAKDOWN");
        System.out.printf("  Base Room Charge                           : LKR %,12.2f%n",
            baseRoomCharge);

        // Extra guest and discount lines are always printed (LKR 0.00 when not
        // applicable) so every invoice shows every item required by the brief.
        // Labels are padded to a fixed width so the amount column stays aligned.
        int extraGuests = Math.max(0, numberOfGuests - room.getBaseCapacity());
        String extraLabel = extraGuests > 0
            ? String.format("Extra Guest Charge (%d guest(s) x %d nights)", extraGuests, numberOfNights)
            : "Extra Guest Charge (no extra guests)";
        System.out.printf("  %-43s: LKR %,12.2f%n", extraLabel, extraGuestCharge);

        String discountLabel = discountApplied
            ? "Discount (12% for 5+ nights)"
            : "Discount (not applicable, under 5 nights)";
        System.out.printf("  %-43s: - LKR %,11.2f%n", discountLabel, discountAmount);

        System.out.printf("  Tax (%.0f%%)                                  : + LKR %,11.2f%n",
            room.getTaxRate() * 100, taxAmount);
        System.out.printf("  Refundable Advance Deducted                : - LKR %,11.2f%n",
            bookingAdvanceDeducted);
        System.out.println(thin);
        System.out.printf("  TOTAL AMOUNT PAYABLE                       :   LKR %,11.2f%n",
            finalPayable);
        System.out.println(thick);
        System.out.println();
    }

    // -----------------------------------------------------------------------
    // Getters (Encapsulation)
    // -----------------------------------------------------------------------

    public String  getInvoiceId()              { return invoiceId; }
    public String  getBookingId()              { return bookingId; }
    public double  getFinalPayable()           { return finalPayable; }
    public double  getBaseRoomCharge()         { return baseRoomCharge; }
    public double  getExtraGuestCharge()       { return extraGuestCharge; }
    public double  getDiscountAmount()         { return discountAmount; }
    public double  getTaxAmount()              { return taxAmount; }
    public double  getBookingAdvanceDeducted() { return bookingAdvanceDeducted; }
    public boolean isDiscountApplied()         { return discountApplied; }
    public long    getNumberOfNights()         { return numberOfNights; }
}
