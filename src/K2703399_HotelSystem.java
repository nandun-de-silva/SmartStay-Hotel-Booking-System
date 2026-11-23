import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central service class for the SmartStay Hotel Booking System.
 *
 * Manages all rooms, guests, and reservations. Acts as the single point of
 * access for both admin and guest operations, enforcing all business rules.
 *
 * OOP concepts demonstrated:
 *   - Encapsulation       : all collections are private; exposed only through
 *                           controlled public methods.
 *   - Composition         : Room and Reservation objects are created inside
 *                           HotelSystem (addRoom(), makeReservation()) and
 *                           exist only in its lists (strong "has-a"). The
 *                           room list is typed as the abstract K2703399_Room,
 *                           so it holds any room subclass (Polymorphism).
 *   - Aggregation         : HotelSystem "has" a list of Guest objects. A guest
 *                           is created outside (by the registration screen)
 *                           and passed in, so it exists independently
 *                           (weak "has-a").
 *   - Method Overloading  : searchAvailableRooms() exists in two forms —
 *                           one with a room-type filter, one without — and
 *                           isRoomFreeForDates() has a form that ignores one
 *                           reservation (used when modifying a booking).
 *
 * Data structures used:
 *   - ArrayList<K2703399_Room>        : ordered list of rooms (fast iteration).
 *   - ArrayList<K2703399_Guest>       : ordered list of registered guests.
 *   - ArrayList<K2703399_Reservation> : ordered list of all reservations.
 *   - HashMap<String, K2703399_Guest> : maps NIC/passport -> Guest for O(1)
 *                                      lookup instead of O(n) linear scan.
 *
 * Algorithms used:
 *   - Linear search    : findRoomById(), searchReservationsByGuestName(),
 *                        searchReservationById(), isRoomFreeForDates().
 *   - Collections.sort : getReservationsByCheckInDate() sorts results by
 *                        booking ID; getAllReservationsSortedByCheckIn()
 *                        sorts by check-in date using a Comparator.
 *   - Rule validation  : the validate / check methods return null when every
 *                        rule passes, or a message naming the rule that failed.
 *
 * @author  K.H. Nandun Lakshan De Silva
 * @id      K2703399
 * @module  CI6115 Programming III
 */
public class K2703399_HotelSystem {

    // -----------------------------------------------------------------------
    // Business-rule constants
    // -----------------------------------------------------------------------
    private static final int MIN_ADVANCE_BOOKING_DAYS = 2; // must book >= 2 days ahead

    // -----------------------------------------------------------------------
    // Data structures (Encapsulation — all private)
    // -----------------------------------------------------------------------
    private final List<K2703399_Room>        rooms;        // ArrayList: ordered room list
    private final List<K2703399_Guest>       guests;       // ArrayList: registered guests
    private final List<K2703399_Reservation> reservations; // ArrayList: all reservations

    // HashMap for fast NIC/passport -> Guest lookup (avoids O(n) scan each time)
    private final Map<String, K2703399_Guest> guestByNic;

    // Auto-increment counters for generating unique IDs
    private int guestCounter   = 1;
    private int bookingCounter = 1;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /** Initialises all collections. */
    public K2703399_HotelSystem() {
        rooms        = new ArrayList<>();
        guests       = new ArrayList<>();
        reservations = new ArrayList<>();
        guestByNic   = new HashMap<>();
    }

    // =======================================================================
    // ADMIN OPERATIONS
    // =======================================================================

    /**
     * Creates a new room and adds it to the system.
     * The room object is built here by the factory method and lives only in
     * this list (Composition). A duplicate room ID is rejected.
     *
     * @param roomId   unique room identifier (e.g. "R009")
     * @param roomType room package, which decides the subclass and the rate
     * @param bedType  bed configuration
     * @return the new room, or null if the room ID is a duplicate
     */
    public K2703399_Room addRoom(String roomId, K2703399_RoomType roomType,
                                 K2703399_BedType bedType) {
        if (roomExists(roomId)) return null; // duplicate found
        K2703399_Room room = K2703399_Room.createRoom(roomId.toUpperCase(), roomType, bedType,
                                                      K2703399_AvailabilityStatus.AVAILABLE);
        rooms.add(room);
        return room;
    }

    /**
     * Updates the room type and/or bed type of an existing room.
     * Pass null for newType / newBed to leave them unchanged.
     *
     * A room's type is its subclass (e.g. K2703399_SuiteRoom), so a new type
     * means a new object: K2703399_Room.createRoom() builds it with the same
     * ID, bed and status, and it takes the old room's place in the list.
     * The daily rate therefore always follows the room type.
     *
     * A room with active reservations cannot be updated, because its type
     * decides the price of those bookings and the details on their invoices.
     *
     * @param roomId  ID of the room to update
     * @param newType new room type (or null to keep current)
     * @param newBed  new bed type (or null to keep current)
     * @return true if updated, false if not found or has active reservations
     */
    public boolean updateRoom(String roomId,
                              K2703399_RoomType newType,
                              K2703399_BedType  newBed) {
        K2703399_Room room = findRoomById(roomId);
        if (room == null || hasActiveReservations(roomId)) return false;

        if (newBed != null) room.setBedType(newBed);
        if (newType != null && newType != room.getRoomType()) {
            K2703399_Room replacement = K2703399_Room.createRoom(
                room.getRoomId(), newType, room.getBedType(), room.getAvailabilityStatus());
            rooms.set(rooms.indexOf(room), replacement); // same position in the list
        }
        return true;
    }

    /**
     * Removes a room from the system.
     * A room cannot be removed if it has at least one active reservation.
     *
     * @param roomId ID of the room to remove
     * @return true if removed, false if not found or has active reservations
     */
    public boolean removeRoom(String roomId) {
        K2703399_Room room = findRoomById(roomId);
        if (room == null || hasActiveReservations(roomId)) return false;
        rooms.remove(room);
        return true;
    }

    /**
     * Updates the availability status of a room (admin-controlled).
     *
     * @param roomId    ID of the target room
     * @param newStatus new availability status
     * @return true if updated, false if room not found
     */
    public boolean updateRoomAvailability(String roomId, K2703399_AvailabilityStatus newStatus) {
        K2703399_Room room = findRoomById(roomId);
        if (room == null) return false;
        room.setAvailabilityStatus(newStatus);
        return true;
    }

    /**
     * Returns a defensive copy of the complete room list.
     *
     * @return new list containing all rooms
     */
    public List<K2703399_Room> getAllRooms() {
        return new ArrayList<>(rooms); // defensive copy — caller cannot modify internal list
    }

    /**
     * Checks whether a room with the given ID exists.
     *
     * @param roomId room identifier (case-insensitive)
     * @return true if the room exists
     */
    public boolean roomExists(String roomId) {
        return findRoomById(roomId) != null;
    }

    /**
     * Counts the active (non-cancelled) reservations of a room.
     * A room's availability status is not changed by bookings (one room can
     * hold several bookings on different dates), so this count is shown next
     * to the status in the admin room list.
     *
     * Algorithm: linear search (O(n)) on the reservations list.
     *
     * @param roomId room identifier (case-insensitive)
     * @return number of active reservations for the room
     */
    public int countActiveReservations(String roomId) {
        int count = 0;
        for (K2703399_Reservation r : reservations) {                   // linear search
            if (r.getRoom().getRoomId().equalsIgnoreCase(roomId) && !r.isCancelled()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Checks whether a room has at least one active (non-cancelled) reservation.
     *
     * @param roomId room identifier (case-insensitive)
     * @return true if an active reservation exists for the room
     */
    public boolean hasActiveReservations(String roomId) {
        return countActiveReservations(roomId) > 0;
    }

    // =======================================================================
    // GUEST OPERATIONS
    // =======================================================================

    /**
     * Returns the next unique guest ID (G0001, G0002, ...).
     * The registration screen uses it when it creates a new guest object.
     *
     * @return a new guest ID
     */
    public String generateGuestId() {
        return String.format("G%04d", guestCounter++);
    }

    /**
     * Registers a guest that was created outside this class (Aggregation).
     * Each NIC/passport number must be unique. The guest stores it in upper
     * case, so "n1234567" and "N1234567" are treated as the same guest.
     *
     * @param guest the guest to register
     * @return true if registered, false if the NIC/passport is already registered
     */
    public boolean registerGuest(K2703399_Guest guest) {
        String nicKey = guest.getNicPassport();
        // HashMap lookup — O(1) average time (more efficient than linear scan)
        if (guestByNic.containsKey(nicKey)) {
            return false; // already registered
        }
        guests.add(guest);
        guestByNic.put(nicKey, guest); // store in HashMap for fast lookup
        return true;
    }

    /**
     * Searches for rooms that are available for the given dates and room type.
     *
     * A room is considered available when:
     *   (a) its status is AVAILABLE, AND
     *   (b) it has no overlapping active reservation for the requested dates.
     *
     * Algorithm: linear search (O(n)) over the rooms list.
     *
     * OOP concept: Method Overloading — two versions of searchAvailableRooms()
     * exist in this class (one with roomType filter, one without).
     *
     * @param roomType  desired room type
     * @param checkIn   desired check-in date
     * @param checkOut  desired check-out date
     * @return list of available rooms matching the type and dates
     */
    public List<K2703399_Room> searchAvailableRooms(K2703399_RoomType roomType,
                                                   LocalDate checkIn,
                                                   LocalDate checkOut) {
        List<K2703399_Room> results = new ArrayList<>();
        for (K2703399_Room room : rooms) {                               // linear search
            if (room.getRoomType() == roomType
                    && room.isAvailable()
                    && isRoomFreeForDates(room, checkIn, checkOut)) {
                results.add(room);
            }
        }
        return results;
    }

    /**
     * Searches for available rooms across all room types for the given dates.
     * Overloaded version of searchAvailableRooms (no room-type filter).
     *
     * @param checkIn  desired check-in date
     * @param checkOut desired check-out date
     * @return list of all available rooms for the requested dates
     */
    public List<K2703399_Room> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        List<K2703399_Room> results = new ArrayList<>();
        for (K2703399_Room room : rooms) {                               // linear search
            if (room.isAvailable() && isRoomFreeForDates(room, checkIn, checkOut)) {
                results.add(room);
            }
        }
        return results;
    }

    /**
     * Checks the booking dates against the business rules.
     *
     * @param checkIn  requested check-in date
     * @param checkOut requested check-out date
     * @return null if the dates are valid, otherwise a message naming the rule broken
     */
    public String checkBookingDates(LocalDate checkIn, LocalDate checkOut) {
        // Must book at least MIN_ADVANCE_BOOKING_DAYS days in advance
        long daysUntilCheckIn = ChronoUnit.DAYS.between(LocalDate.now(), checkIn);
        if (daysUntilCheckIn < MIN_ADVANCE_BOOKING_DAYS) {
            return "Bookings must be made at least " + MIN_ADVANCE_BOOKING_DAYS
                 + " days in advance (earliest check-in: "
                 + LocalDate.now().plusDays(MIN_ADVANCE_BOOKING_DAYS) + ").";
        }
        // Check-out must be after check-in
        if (!checkOut.isAfter(checkIn)) {
            return "Check-out date must be after the check-in date.";
        }
        return null;
    }

    /**
     * Checks the dates and number of guests for a booking of the given room.
     * The maximum occupancy comes from the room's subclass (Polymorphism).
     *
     * @param room           room being booked
     * @param checkIn        requested check-in date
     * @param checkOut       requested check-out date
     * @param numberOfGuests number of guests
     * @return null if valid, otherwise a message naming the rule broken
     */
    public String checkBookingDetails(K2703399_Room room, LocalDate checkIn,
                                      LocalDate checkOut, int numberOfGuests) {
        String dateError = checkBookingDates(checkIn, checkOut);
        if (dateError != null) return dateError;

        if (numberOfGuests < 1) {
            return "Number of guests must be at least 1.";
        }
        if (numberOfGuests > room.getMaxOccupancy()) {
            return "A " + room.getRoomType() + " room allows a maximum of "
                 + room.getMaxOccupancy() + " guests.";
        }
        return null;
    }

    /**
     * Validates a new reservation against every business rule.
     *
     * Validation checks (in order):
     *   1. Guest must be registered (NIC/passport lookup via HashMap).
     *   2. Room must exist.
     *   3. Room availability status must be AVAILABLE.
     *   4. Check-in must be at least MIN_ADVANCE_BOOKING_DAYS days from today.
     *   5. Check-out must be after check-in.
     *   6. Number of guests must be between 1 and the room's maximum occupancy.
     *   7. Room must have no date-overlapping active reservation.
     *
     * @param nicPassport    guest's NIC or passport number
     * @param roomId         room to reserve
     * @param checkIn        check-in date
     * @param checkOut       check-out date
     * @param numberOfGuests number of guests
     * @return null if the reservation is allowed, otherwise the reason it is refused
     */
    public String validateReservation(String nicPassport, String roomId,
                                      LocalDate checkIn, LocalDate checkOut,
                                      int numberOfGuests) {
        // 1. Validate guest
        if (findGuestByNic(nicPassport) == null) {
            return "NIC/Passport is not registered. Please register first.";
        }

        // 2. Validate room exists
        K2703399_Room room = findRoomById(roomId);
        if (room == null) return "Room not found.";

        // 3. Room must be AVAILABLE
        if (!room.isAvailable()) {
            return "Room " + room.getRoomId() + " is not available (status: "
                 + room.getAvailabilityStatus() + ").";
        }

        // 4-6. Dates and number of guests
        String detailError = checkBookingDetails(room, checkIn, checkOut, numberOfGuests);
        if (detailError != null) return detailError;

        // 7. No date overlap with existing active reservations
        if (!isRoomFreeForDates(room, checkIn, checkOut)) {
            return "Room " + room.getRoomId() + " is already booked for some of these dates.";
        }
        return null;
    }

    /**
     * Creates a new reservation after validating all business rules
     * (see validateReservation()).
     *
     * @param nicPassport    guest's NIC or passport number
     * @param roomId         room to reserve
     * @param checkIn        check-in date
     * @param checkOut       check-out date
     * @param numberOfGuests number of guests
     * @return the new K2703399_Reservation, or null if any validation fails
     */
    public K2703399_Reservation makeReservation(String nicPassport, String roomId,
                                               LocalDate checkIn, LocalDate checkOut,
                                               int numberOfGuests) {
        if (validateReservation(nicPassport, roomId, checkIn, checkOut, numberOfGuests) != null) {
            return null;
        }

        // All checks passed — create the reservation
        String bookingId = String.format("BK%05d", bookingCounter++);
        K2703399_Reservation reservation = new K2703399_Reservation(
            bookingId, findGuestByNic(nicPassport), findRoomById(roomId),
            checkIn, checkOut, numberOfGuests
        );
        reservations.add(reservation);
        return reservation;
    }

    /**
     * Searches all reservations for those whose guest name contains the
     * given search string (case-insensitive partial match).
     *
     * Algorithm: linear search (O(n)) on the reservations list.
     *
     * @param name partial or full guest name to search for
     * @return list of matching reservations (empty if none found)
     */
    public List<K2703399_Reservation> searchReservationsByGuestName(String name) {
        List<K2703399_Reservation> results = new ArrayList<>();
        String lowerName = name.toLowerCase();
        for (K2703399_Reservation r : reservations) {                   // linear search
            if (r.getGuest().getFullName().toLowerCase().contains(lowerName)) {
                results.add(r);
            }
        }
        return results;
    }

    /**
     * Finds a single reservation by its booking ID.
     *
     * Algorithm: linear search (O(n)) on the reservations list.
     *
     * @param bookingId exact booking ID to find
     * @return the matching K2703399_Reservation, or null if not found
     */
    public K2703399_Reservation searchReservationById(String bookingId) {
        for (K2703399_Reservation r : reservations) {                   // linear search
            if (r.getBookingId().equalsIgnoreCase(bookingId)) {
                return r;
            }
        }
        return null;
    }

    /**
     * Returns all active reservations for a specific check-in date,
     * sorted by booking ID using Collections.sort().
     *
     * Algorithm: linear filter then Collections.sort() with Comparator.
     *
     * @param date the check-in date to filter by
     * @return sorted list of active reservations on that date
     */
    public List<K2703399_Reservation> getReservationsByCheckInDate(LocalDate date) {
        List<K2703399_Reservation> results = new ArrayList<>();
        for (K2703399_Reservation r : reservations) {                   // linear filter
            if (r.getCheckInDate().equals(date) && !r.isCancelled()) {
                results.add(r);
            }
        }
        // Sort results by booking ID (ascending) — Collections.sort() algorithm
        Collections.sort(results, Comparator.comparing(K2703399_Reservation::getBookingId));
        return results;
    }

    /**
     * Returns all reservations sorted by check-in date (ascending).
     * Used by the Admin Portal's "View All Reservations" option.
     *
     * Algorithm: Collections.sort() with Comparator — demonstrates use of
     * sorting algorithm on a List of objects.
     *
     * @return date-sorted list of all reservations
     */
    public List<K2703399_Reservation> getAllReservationsSortedByCheckIn() {
        List<K2703399_Reservation> sorted = new ArrayList<>(reservations);
        Collections.sort(sorted, Comparator.comparing(K2703399_Reservation::getCheckInDate));
        return sorted;
    }

    /**
     * Checks whether a booking may be modified or cancelled.
     *
     * @param bookingId the booking to check
     * @return null if it may be changed, otherwise the reason it may not
     */
    public String checkCanChangeBooking(String bookingId) {
        K2703399_Reservation reservation = searchReservationById(bookingId);
        if (reservation == null)        return "Booking not found.";
        if (reservation.isCancelled())  return "Booking is already cancelled.";
        if (!reservation.isEligibleForChange()) {
            return "Bookings cannot be modified or cancelled within 24 hours of check-in.";
        }
        return null;
    }

    /**
     * Checks whether a booking may be modified: it must be changeable
     * (see checkCanChangeBooking()) and, as for a new booking, its room's
     * status must be AVAILABLE. A booking on an unavailable room can still
     * be cancelled.
     *
     * @param bookingId the booking to check
     * @return null if it may be modified, otherwise the reason it may not
     */
    public String checkCanModifyBooking(String bookingId) {
        String changeError = checkCanChangeBooking(bookingId);
        if (changeError != null) return changeError;

        K2703399_Room room = searchReservationById(bookingId).getRoom();
        if (!room.isAvailable()) {
            return "Room " + room.getRoomId() + " is not available (status: "
                 + room.getAvailabilityStatus() + "), so the booking cannot be modified.";
        }
        return null;
    }

    /**
     * Validates a change of dates and/or number of guests for a booking.
     * The booking keeps the same room; the room must still be AVAILABLE and
     * free for the new dates, ignoring the booking's own current dates.
     *
     * @param bookingId      the booking to modify
     * @param newCheckIn     new check-in date
     * @param newCheckOut    new check-out date
     * @param newGuestCount  new number of guests
     * @return null if the change is allowed, otherwise the reason it is refused
     */
    public String validateModification(String bookingId, LocalDate newCheckIn,
                                       LocalDate newCheckOut, int newGuestCount) {
        String modifyError = checkCanModifyBooking(bookingId);
        if (modifyError != null) return modifyError;

        K2703399_Reservation reservation = searchReservationById(bookingId);
        K2703399_Room room = reservation.getRoom();

        String detailError = checkBookingDetails(room, newCheckIn,
                                         newCheckOut, newGuestCount);
        if (detailError != null) return detailError;

        if (!isRoomFreeForDates(room, newCheckIn, newCheckOut, reservation)) {
            return "Room " + room.getRoomId() + " is already booked for some of the new dates.";
        }
        return null;
    }

    /**
     * Modifies the dates and number of guests of a booking after validating
     * all business rules (see validateModification()).
     *
     * @param bookingId      the booking to modify
     * @param newCheckIn     new check-in date
     * @param newCheckOut    new check-out date
     * @param newGuestCount  new number of guests
     * @return true if modified, false if any validation fails
     */
    public boolean modifyReservation(String bookingId, LocalDate newCheckIn,
                                     LocalDate newCheckOut, int newGuestCount) {
        if (validateModification(bookingId, newCheckIn, newCheckOut, newGuestCount) != null) {
            return false;
        }
        searchReservationById(bookingId).updateBooking(newCheckIn, newCheckOut, newGuestCount);
        return true;
    }

    /**
     * Cancels a reservation if it is eligible (not within 24 h of check-in).
     *
     * @param bookingId the booking to cancel
     * @return true if cancelled successfully, false otherwise
     */
    public boolean cancelReservation(String bookingId) {
        if (checkCanChangeBooking(bookingId) != null) return false;
        searchReservationById(bookingId).setCancelled(true);
        return true;
    }

    /**
     * Generates and returns an invoice for an active reservation.
     * The invoice number is taken from the booking number (BK00001 -> INV00001),
     * so re-printing an invoice always shows the same invoice number.
     *
     * @param bookingId the booking for which to generate an invoice
     * @return a populated K2703399_Invoice, or null if booking not found / cancelled
     */
    public K2703399_Invoice generateInvoice(String bookingId) {
        K2703399_Reservation reservation = searchReservationById(bookingId);
        if (reservation == null || reservation.isCancelled()) return null;
        String invoiceId = "INV" + reservation.getBookingId().substring(2);
        return reservation.generateInvoice(invoiceId); // Reservation creates the Invoice
    }

    // =======================================================================
    // ADDITIONAL ACCESSORS (for admin views / reporting)
    // =======================================================================

    /** @return defensive copy of the full guest list */
    public List<K2703399_Guest> getAllGuests() {
        return new ArrayList<>(guests);
    }

    /**
     * Looks up a guest by NIC/passport number using the HashMap.
     *
     * @param nicPassport NIC or passport (case-insensitive)
     * @return the matching K2703399_Guest, or null if not found
     */
    public K2703399_Guest findGuestByNic(String nicPassport) {
        return guestByNic.get(nicPassport.toUpperCase());
    }

    // =======================================================================
    // PRIVATE UTILITY METHODS
    // =======================================================================

    /**
     * Finds a room by its ID using a linear search.
     *
     * @param roomId room identifier (case-insensitive)
     * @return the matching K2703399_Room, or null if not found
     */
    private K2703399_Room findRoomById(String roomId) {
        for (K2703399_Room room : rooms) {                              // linear search
            if (room.getRoomId().equalsIgnoreCase(roomId)) {
                return room;
            }
        }
        return null;
    }

    /**
     * Checks whether a room has no overlapping active reservations for the
     * specified date range.
     *
     * @param room     the room to check
     * @param checkIn  start of the requested period
     * @param checkOut end of the requested period
     * @return true if the room is free for those dates, false if there is a clash
     */
    private boolean isRoomFreeForDates(K2703399_Room room,
                                       LocalDate checkIn, LocalDate checkOut) {
        return isRoomFreeForDates(room, checkIn, checkOut, null);
    }

    /**
     * Overloaded version that ignores one reservation, so a booking being
     * modified does not clash with its own current dates.
     *
     * Overlap condition (standard interval check):
     *   existing.checkIn  < requested.checkOut
     *   AND
     *   existing.checkOut > requested.checkIn
     *
     * @param room     the room to check
     * @param checkIn  start of the requested period
     * @param checkOut end of the requested period
     * @param ignore   reservation to leave out of the check (or null)
     * @return true if the room is free for those dates, false if there is a clash
     */
    private boolean isRoomFreeForDates(K2703399_Room room, LocalDate checkIn,
                                       LocalDate checkOut, K2703399_Reservation ignore) {
        for (K2703399_Reservation r : reservations) {
            if (r != ignore
                    && r.getRoom().getRoomId().equalsIgnoreCase(room.getRoomId())
                    && !r.isCancelled()) {
                // Standard date-range overlap test
                boolean overlaps = r.getCheckInDate().isBefore(checkOut)
                                && r.getCheckOutDate().isAfter(checkIn);
                if (overlaps) return false;
            }
        }
        return true;
    }
}
