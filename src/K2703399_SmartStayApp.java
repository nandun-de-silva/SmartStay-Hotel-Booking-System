import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Main entry point for the SmartStay Hotel Booking System CLI.
 *
 * Provides a text-based Command Line Interface with two access portals:
 *   - Admin Portal  : manage rooms and availability; view reservations and guests.
 *   - Guest Portal  : register, search, book, modify and cancel rooms; view invoices.
 *
 * UI/UX design decisions:
 *   - Numbered menus with clear labels keep navigation intuitive.
 *   - Every action gives immediate, clear feedback ([OK] or [!]); when an
 *     action is refused, the message names the exact rule that failed.
 *   - Input methods loop until valid data is entered (no silent failures).
 *   - Choices are picked from numbered lists instead of typed, where possible.
 *   - A divider line visually separates sections for readability.
 *   - Sample data is pre-loaded so the system is demo-ready on first run.
 *
 * OOP note: main() creates one K2703399_SmartStayApp object and calls run().
 * The hotel system, the Scanner, the administrator and every menu method
 * belong to that object, so only main() and the text constants are static.
 *
 * @author  K.H. Nandun Lakshan De Silva
 * @id      K2703399
 * @module  CI6115 Programming III
 */
public class K2703399_SmartStayApp {

    // -----------------------------------------------------------------------
    // Text constants (static — shared, never change)
    // -----------------------------------------------------------------------
    private static final DateTimeFormatter    DATE_FMT   = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String               DIVIDER    = "-".repeat(66);
    private static final String               THICK_DIV  = "=".repeat(66);

    // -----------------------------------------------------------------------
    // Instance fields (Encapsulation — each belongs to the application object)
    // -----------------------------------------------------------------------
    private final K2703399_HotelSystem  system  = new K2703399_HotelSystem();
    private final Scanner              scanner = new Scanner(System.in);

    // Administrator operating the Admin Portal. Declared with the abstract
    // K2703399_Person type so getDetails() is resolved at runtime (Polymorphism).
    private final K2703399_Person       admin   = new K2703399_Admin(
        "A001", "System Administrator", "0112345678", "admin@smartstay.lk");

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /** Creates the application and loads the sample rooms so it is ready to use. */
    public K2703399_SmartStayApp() {
        loadSampleData();
    }

    // -----------------------------------------------------------------------
    // Application entry point
    // -----------------------------------------------------------------------

    /**
     * Launches the SmartStay CLI application by creating the application
     * object and running it.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        new K2703399_SmartStayApp().run();
    }

    /** Shows the welcome banner and the main menu in a loop until the user exits. */
    public void run() {
        System.out.println();
        System.out.println(THICK_DIV);
        System.out.println("        WELCOME TO SMARTSTAY HOTEL BOOKING SYSTEM");
        System.out.println("           Sri Lanka's Premier Hotel Experience");
        System.out.println(THICK_DIV);

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("  Select: ");
            switch (choice) {
                case 1: adminPortal(); break;
                case 2: guestPortal(); break;
                case 0:
                    System.out.println("\n  Thank you for using SmartStay. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("  [!] Invalid option. Please try again.");
            }
        }
        scanner.close();
    }

    // -----------------------------------------------------------------------
    // Main menu
    // -----------------------------------------------------------------------

    private void printMainMenu() {
        System.out.println();
        System.out.println(THICK_DIV);
        System.out.println("  MAIN MENU");
        System.out.println(DIVIDER);
        System.out.println("  [1] Admin Portal");
        System.out.println("  [2] Guest Portal");
        System.out.println("  [0] Exit");
        System.out.println(DIVIDER);
    }

    // =======================================================================
    // ADMIN PORTAL
    // =======================================================================

    private void adminPortal() {
        boolean inAdmin = true;
        while (inAdmin) {
            System.out.println();
            System.out.println(THICK_DIV);
            System.out.println("  ADMIN PORTAL");
            System.out.println("  Logged in as -> " + admin.getDetails());
            System.out.println(DIVIDER);
            System.out.println("  [1] Add Room");
            System.out.println("  [2] Update Room");
            System.out.println("  [3] Remove Room");
            System.out.println("  [4] Update Room Availability");
            System.out.println("  [5] View All Rooms");
            System.out.println("  [6] View All Reservations");
            System.out.println("  [7] View Registered Guests");
            System.out.println("  [0] Back");
            System.out.println(DIVIDER);

            int choice = readInt("  Select: ");
            switch (choice) {
                case 1: adminAddRoom();            break;
                case 2: adminUpdateRoom();         break;
                case 3: adminRemoveRoom();         break;
                case 4: adminUpdateAvailability(); break;
                case 5: adminViewAllRooms();       break;
                case 6: adminViewAllReservations();break;
                case 7: adminViewGuests();         break;
                case 0: inAdmin = false;           break;
                default: System.out.println("  [!] Invalid option.");
            }
        }
    }

    /** Prompts the admin to enter details for a new room and adds it. */
    private void adminAddRoom() {
        System.out.println();
        System.out.println("  -- ADD NEW ROOM --");
        // Room ID format: 'R' followed by three digits (case-insensitive input)
        String roomId = readMatching("  Room ID (e.g. R009): ", "[Rr]\\d{3}",
            "Room ID must be R followed by 3 digits (e.g. R009).").toUpperCase();
        if (system.roomExists(roomId)) {
            System.out.println("  [!] Room ID already exists. No changes made.");
            return;
        }

        K2703399_RoomType roomType = selectRoomType();
        K2703399_BedType  bedType  = selectBedType();

        // HotelSystem creates the room object itself (Composition)
        K2703399_Room room = system.addRoom(roomId, roomType, bedType);
        if (room != null) {
            System.out.printf("  [OK] Room %s added. Daily rate: LKR %,.2f%n",
                roomId, room.getDailyRate());
        } else {
            System.out.println("  [!] Room ID already exists. No changes made.");
        }
    }

    /**
     * Allows the admin to update the room type or bed type of a room.
     * The daily rate is room-type specific, so it changes with the room type.
     */
    private void adminUpdateRoom() {
        System.out.println();
        System.out.println("  -- UPDATE ROOM --");
        String roomId = readString("  Room ID to update: ").toUpperCase();
        if (!system.roomExists(roomId)) {
            System.out.println("  [!] Room not found.");
            return;
        }
        if (system.hasActiveReservations(roomId)) {
            System.out.println("  [!] Room " + roomId + " has active reservations and cannot be updated.");
            return;
        }

        System.out.println("  What to update?");
        System.out.println("    [1] Room Type (daily rate changes with the type)");
        System.out.println("    [2] Bed Type");
        System.out.println("    [0] Cancel");
        int choice = readChoice("  Select: ", 0, 2);

        boolean updated;
        if (choice == 1) {
            updated = system.updateRoom(roomId, selectRoomType(), null);
        } else if (choice == 2) {
            updated = system.updateRoom(roomId, null, selectBedType());
        } else {
            return;
        }

        System.out.println(updated
            ? "  [OK] Room updated successfully."
            : "  [!] Room could not be updated.");
    }

    /** Removes a room from the system (blocked if the room has active reservations). */
    private void adminRemoveRoom() {
        System.out.println();
        System.out.println("  -- REMOVE ROOM --");
        String roomId = readString("  Room ID to remove: ").toUpperCase();
        if (!system.roomExists(roomId)) {
            System.out.println("  [!] Room not found.");
        } else if (system.hasActiveReservations(roomId)) {
            System.out.println("  [!] Room " + roomId + " has active reservations and cannot be removed.");
        } else if (system.removeRoom(roomId)) {
            System.out.println("  [OK] Room " + roomId + " removed.");
        }
    }

    /** Lets the admin change the availability status of a room. */
    private void adminUpdateAvailability() {
        System.out.println();
        System.out.println("  -- UPDATE ROOM AVAILABILITY --");
        String roomId = readString("  Room ID: ").toUpperCase();
        if (!system.roomExists(roomId)) {
            System.out.println("  [!] Room not found.");
            return;
        }

        // Numbered list built from the enum, so it always matches the statuses
        K2703399_AvailabilityStatus[] statuses = K2703399_AvailabilityStatus.values();
        System.out.println("  New Status:");
        for (int i = 0; i < statuses.length; i++) {
            System.out.printf("    [%d] %s%n", i + 1, statuses[i]);
        }
        K2703399_AvailabilityStatus status = statuses[readChoice("  Select: ", 1, statuses.length) - 1];

        if (system.updateRoomAvailability(roomId, status)) {
            System.out.println("  [OK] Status of room " + roomId + " updated to " + status + ".");
        }
    }

    /**
     * Displays a table of all rooms registered in the system, with the number
     * of active bookings of each room next to its availability status.
     */
    private void adminViewAllRooms() {
        List<K2703399_Room> rooms = system.getAllRooms();
        System.out.println();
        System.out.printf("  -- ALL ROOMS  (%d total) --%n", rooms.size());
        if (rooms.isEmpty()) {
            System.out.println("  No rooms registered.");
            return;
        }
        System.out.println("  " + DIVIDER);
        for (K2703399_Room r : rooms) {
            System.out.printf("  %s | Active bookings: %d%n",
                r, system.countActiveReservations(r.getRoomId()));
        }
        System.out.println("  " + DIVIDER);
    }

    /** Displays every reservation (active and cancelled), sorted by check-in date. */
    private void adminViewAllReservations() {
        System.out.println();
        System.out.println("  -- ALL RESERVATIONS (sorted by check-in date) --");
        printReservations(system.getAllReservationsSortedByCheckIn());
    }

    /**
     * Displays all registered guests. Each guest is printed through
     * toString(), which calls the overridden getDetails() (Polymorphism).
     */
    private void adminViewGuests() {
        List<K2703399_Guest> guests = system.getAllGuests();
        System.out.println();
        System.out.printf("  -- REGISTERED GUESTS  (%d total) --%n", guests.size());
        if (guests.isEmpty()) {
            System.out.println("  No guests registered.");
            return;
        }
        System.out.println("  " + DIVIDER);
        for (K2703399_Guest g : guests) {
            System.out.println("  " + g);
        }
        System.out.println("  " + DIVIDER);
    }

    // =======================================================================
    // GUEST PORTAL
    // =======================================================================

    private void guestPortal() {
        boolean inGuest = true;
        while (inGuest) {
            System.out.println();
            System.out.println(THICK_DIV);
            System.out.println("  GUEST PORTAL");
            System.out.println(DIVIDER);
            System.out.println("  [1] Register");
            System.out.println("  [2] Search Available Rooms");
            System.out.println("  [3] Make a Reservation");
            System.out.println("  [4] Search Reservations");
            System.out.println("  [5] View Reservations by Check-In Date");
            System.out.println("  [6] Modify a Reservation");
            System.out.println("  [7] Cancel a Reservation");
            System.out.println("  [8] Generate Invoice");
            System.out.println("  [0] Back");
            System.out.println(DIVIDER);

            int choice = readInt("  Select: ");
            switch (choice) {
                case 1: guestRegister();           break;
                case 2: guestSearchRooms();        break;
                case 3: guestMakeReservation();    break;
                case 4: guestSearchReservations(); break;
                case 5: guestViewByCheckIn();      break;
                case 6: guestModifyReservation();  break;
                case 7: guestCancelReservation();  break;
                case 8: guestGenerateInvoice();    break;
                case 0: inGuest = false;           break;
                default: System.out.println("  [!] Invalid option.");
            }
        }
    }

    /** Collects guest details and registers the guest in the system. */
    private void guestRegister() {
        System.out.println();
        System.out.println("  -- GUEST REGISTRATION --");
        // NIC / passport: letters and digits only (old NIC 991234567V,
        // new NIC 200012345678, passport N1234567)
        String nic     = readMatching("  NIC / Passport Number : ", "[A-Za-z0-9]{5,15}",
            "Enter 5-15 letters/digits (e.g. 200012345678, 991234567V or N1234567).");
        if (system.findGuestByNic(nic) != null) {
            System.out.println("  [!] NIC/Passport already registered.");
            return;
        }

        // Full name: starts with a letter; letters, spaces, dots, hyphens, apostrophes
        String name    = readMatching("  Full Name             : ", "[A-Za-z][A-Za-z .'-]*",
            "Name may only contain letters, spaces, dots, hyphens and apostrophes.");

        // Contact number: digits only, optional leading '+' for international guests
        String contact = readMatching("  Contact Number        : ", "\\+?\\d{9,15}",
            "Enter 9-15 digits (e.g. 0771234567 or +94771234567).");

        // Email: must have a name, '@' and a domain (e.g. name@example.com)
        String email   = readMatching("  Email Address         : ", "[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+",
            "Enter a valid email address (e.g. name@example.com).");

        // The guest object is created here and passed to HotelSystem (Aggregation)
        K2703399_Guest guest = new K2703399_Guest(system.generateGuestId(), nic, name, contact, email);
        if (system.registerGuest(guest)) {
            System.out.println("  [OK] Registration successful!");
            System.out.println("  " + guest.getDetails());
        } else {
            System.out.println("  [!] NIC/Passport already registered.");
        }
    }

    /**
     * Lets the guest search for available rooms either by room type or
     * across all types, for a chosen date range.
     */
    private void guestSearchRooms() {
        System.out.println();
        System.out.println("  -- SEARCH AVAILABLE ROOMS --");
        System.out.println("  [1] By Room Type   [2] All Types");
        int mode = readChoice("  Select: ", 1, 2);

        // Only dates that could actually be booked are searched
        LocalDate[] dates  = readBookingDates("  Check-In  Date (yyyy-MM-dd): ",
                                              "  Check-Out Date (yyyy-MM-dd): ");
        LocalDate checkIn  = dates[0];
        LocalDate checkOut = dates[1];

        List<K2703399_Room> results;
        if (mode == 1) {
            results = system.searchAvailableRooms(selectRoomType(), checkIn, checkOut);
        } else {
            results = system.searchAvailableRooms(checkIn, checkOut);
        }

        System.out.printf("%n  Found %d available room(s):%n", results.size());
        if (results.isEmpty()) {
            System.out.println("  No rooms match your search. Try different dates or type.");
        } else {
            System.out.println("  " + DIVIDER);
            for (K2703399_Room r : results) {
                System.out.println("  " + r);
            }
            System.out.println("  " + DIVIDER);
        }
    }

    /**
     * Collects the details required by the case study (room type, number of
     * guests, check-in and check-out dates), then lets the guest choose one of
     * the rooms of that type that are free for those dates.
     */
    private void guestMakeReservation() {
        System.out.println();
        System.out.println("  -- MAKE A RESERVATION --");
        System.out.printf("  Note: A refundable advance of LKR %,.0f will be charged.%n",
            K2703399_Reservation.BOOKING_ADVANCE);
        System.out.println();

        String nic = readString("  Your NIC / Passport   : ");
        if (system.findGuestByNic(nic) == null) {
            System.out.println("  [!] NIC/Passport is not registered. Please register first (Guest Portal -> 1).");
            return;
        }

        // Dates and number of guests are re-asked until they satisfy the rules
        K2703399_RoomType roomType = selectRoomType();
        LocalDate[] dates  = readBookingDates("  Check-In  (yyyy-MM-dd): ",
                                              "  Check-Out (yyyy-MM-dd): ");
        LocalDate checkIn  = dates[0];
        LocalDate checkOut = dates[1];

        // List the free rooms of the chosen type and let the guest pick one
        List<K2703399_Room> available = system.searchAvailableRooms(roomType, checkIn, checkOut);
        if (available.isEmpty()) {
            System.out.println("  [!] No " + roomType + " rooms are available for these dates.");
            return;
        }
        System.out.println("  Available " + roomType + " rooms:");
        for (int i = 0; i < available.size(); i++) {
            System.out.printf("    [%d] %s%n", i + 1, available.get(i));
        }
        K2703399_Room room = available.get(readChoice("  Select room: ", 1, available.size()) - 1);
        int guests = readGuestCount("  Number of Guests      : ", room, checkIn, checkOut);

        String error = system.validateReservation(nic, room.getRoomId(), checkIn, checkOut, guests);
        if (error != null) {
            System.out.println("  [!] Reservation failed: " + error);
            return;
        }

        K2703399_Reservation reservation =
            system.makeReservation(nic, room.getRoomId(), checkIn, checkOut, guests);
        System.out.println();
        System.out.println("  [OK] Reservation confirmed!");
        System.out.println("  " + reservation);
        System.out.printf("  Booking Advance Charged: LKR %,.0f (fully refundable)%n",
            K2703399_Reservation.BOOKING_ADVANCE);
    }

    /** Searches reservations by guest name or by booking ID. */
    private void guestSearchReservations() {
        System.out.println();
        System.out.println("  -- SEARCH RESERVATIONS --");
        System.out.println("  [1] By Guest Name   [2] By Booking ID");
        int mode = readChoice("  Select: ", 1, 2);

        if (mode == 1) {
            String name = readString("  Guest Name (partial match): ");
            printReservations(system.searchReservationsByGuestName(name));
        } else {
            String id = readString("  Booking ID: ");
            K2703399_Reservation r = system.searchReservationById(id);
            if (r != null) {
                System.out.println("  " + r);
            } else {
                System.out.println("  [!] Booking not found.");
            }
        }
    }

    /** Lists active reservations for a given check-in date, sorted by booking ID. */
    private void guestViewByCheckIn() {
        System.out.println();
        System.out.println("  -- RESERVATIONS BY CHECK-IN DATE --");
        LocalDate date = readDate("  Check-In Date (yyyy-MM-dd): ");
        List<K2703399_Reservation> results = system.getReservationsByCheckInDate(date);
        System.out.printf("%n  Reservations with check-in on %s:%n", date);
        printReservations(results);
    }

    /**
     * Changes the dates and/or number of guests of a reservation
     * (not allowed within 24 hours of check-in).
     */
    private void guestModifyReservation() {
        System.out.println();
        System.out.println("  -- MODIFY RESERVATION --");
        String bookingId = readString("  Booking ID: ");
        String modifyError = system.checkCanModifyBooking(bookingId);
        if (modifyError != null) {
            System.out.println("  [!] " + modifyError);
            return;
        }

        K2703399_Reservation current = system.searchReservationById(bookingId);
        System.out.println("  Current booking:");
        System.out.println("  " + current);
        System.out.println("  Enter the new details (the room stays the same):");
        LocalDate[] dates  = readBookingDates("  New Check-In  (yyyy-MM-dd): ",
                                              "  New Check-Out (yyyy-MM-dd): ");
        LocalDate checkIn  = dates[0];
        LocalDate checkOut = dates[1];
        int       guests   = readGuestCount("  New Number of Guests       : ",
                                            current.getRoom(), checkIn, checkOut);

        String error = system.validateModification(bookingId, checkIn, checkOut, guests);
        if (error != null) {
            System.out.println("  [!] Modification failed: " + error);
            return;
        }
        system.modifyReservation(bookingId, checkIn, checkOut, guests);
        System.out.println("  [OK] Reservation updated.");
        System.out.println("  " + system.searchReservationById(bookingId));
    }

    /** Cancels a reservation (if eligibility rules are satisfied). */
    private void guestCancelReservation() {
        System.out.println();
        System.out.println("  -- CANCEL RESERVATION --");
        String bookingId = readString("  Booking ID: ");
        String error = system.checkCanChangeBooking(bookingId);
        if (error != null) {
            System.out.println("  [!] Cannot cancel: " + error);
            return;
        }
        system.cancelReservation(bookingId);
        System.out.println("  [OK] Reservation " + bookingId.toUpperCase() + " has been cancelled.");
        System.out.printf("  Your refundable advance of LKR %,.0f will be refunded.%n",
            K2703399_Reservation.BOOKING_ADVANCE);
    }

    /** Generates and prints a detailed invoice for an active reservation. */
    private void guestGenerateInvoice() {
        System.out.println();
        System.out.println("  -- GENERATE INVOICE --");
        String bookingId = readString("  Booking ID: ");
        K2703399_Invoice invoice = system.generateInvoice(bookingId);
        if (invoice != null) {
            invoice.printInvoice();
        } else {
            System.out.println("  [!] Invoice not available (booking not found or cancelled).");
        }
    }

    // =======================================================================
    // REUSABLE UI HELPERS
    // =======================================================================

    /**
     * Displays a numbered room-type menu built from the K2703399_RoomType enum
     * and returns the selected type. Rates and other package details are shown
     * when rooms are listed, because they belong to the room subclasses. Re-prompts until a valid number is chosen.
     */
    private K2703399_RoomType selectRoomType() {
        K2703399_RoomType[] types = K2703399_RoomType.values();
        System.out.println("  Room Types:");
        for (int i = 0; i < types.length; i++) {
            K2703399_RoomType t = types[i];
            System.out.printf("    [%d] %s%n", i + 1, t);
        }
        return types[readChoice("  Select type: ", 1, types.length) - 1];
    }

    /**
     * Displays a numbered bed-type menu built from the K2703399_BedType enum
     * and returns the selected type. Re-prompts until a valid number is chosen.
     */
    private K2703399_BedType selectBedType() {
        K2703399_BedType[] beds = K2703399_BedType.values();
        System.out.print("  Bed Types:");
        for (int i = 0; i < beds.length; i++) {
            System.out.printf(" [%d] %s ", i + 1, beds[i]);
        }
        System.out.println();
        return beds[readChoice("  Select type: ", 1, beds.length) - 1];
    }

    /** Prints a list of reservations, or a "not found" message if the list is empty. */
    private void printReservations(List<K2703399_Reservation> list) {
        if (list.isEmpty()) {
            System.out.println("  No reservations found.");
            return;
        }
        System.out.printf("  %d reservation(s) found:%n", list.size());
        System.out.println("  " + DIVIDER);
        for (K2703399_Reservation r : list) {
            System.out.println("  " + r);
        }
        System.out.println("  " + DIVIDER);
    }

    // -----------------------------------------------------------------------
    // Input utilities — each loops until valid input is received
    // -----------------------------------------------------------------------

    /**
     * Prints a prompt and reads one trimmed line of input.
     * If the input stream has ended (e.g. Ctrl+Z / Ctrl+D), the program
     * closes cleanly instead of crashing with an exception.
     *
     * @param prompt message displayed before reading input
     * @return trimmed input line (may be empty)
     */
    private String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println("\n  Input closed. Thank you for using SmartStay. Goodbye!");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    /**
     * Reads and returns a trimmed, non-empty string, re-prompting on blank input.
     *
     * @param prompt message displayed before reading input
     * @return trimmed input string
     */
    private String readString(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty()) return value;
            System.out.println("  [!] This field cannot be empty.");
        }
    }

    /**
     * Reads a non-empty string that matches a regular expression,
     * re-prompting with the given error message until it does.
     *
     * @param prompt  message displayed before reading input
     * @param regex   pattern the whole input must match
     * @param errorMessage message shown when the input does not match
     * @return the valid input string
     */
    private String readMatching(String prompt, String regex, String errorMessage) {
        while (true) {
            String value = readString(prompt);
            if (value.matches(regex)) return value;
            System.out.println("  [!] " + errorMessage);
        }
    }

    /**
     * Reads an integer from the user, re-prompting on invalid input.
     *
     * @param prompt message displayed before reading input
     * @return parsed integer value
     */
    private int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("  [!] Please enter a whole number.");
            }
        }
    }

    /**
     * Reads a menu choice within a range, re-prompting until it is valid.
     *
     * @param prompt message displayed before reading input
     * @param min    smallest valid choice
     * @param max    largest valid choice
     * @return the chosen number
     */
    private int readChoice(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) return value;
            System.out.printf("  [!] Please choose a number from %d to %d.%n", min, max);
        }
    }

    /**
     * Reads a date in yyyy-MM-dd format, re-prompting on invalid input.
     *
     * @param prompt message displayed before reading input
     * @return parsed LocalDate
     */
    private LocalDate readDate(String prompt) {
        while (true) {
            try {
                return LocalDate.parse(readLine(prompt), DATE_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("  [!] Invalid date. Use format: yyyy-MM-dd (e.g. 2026-12-25)");
            }
        }
    }

    /**
     * Reads a check-in and check-out date, re-asking both until they satisfy
     * the booking date rules in K2703399_HotelSystem (2-day advance booking,
     * check-out after check-in).
     *
     * @param checkInPrompt  prompt for the check-in date
     * @param checkOutPrompt prompt for the check-out date
     * @return array of two dates: [0] = check-in, [1] = check-out
     */
    private LocalDate[] readBookingDates(String checkInPrompt, String checkOutPrompt) {
        while (true) {
            LocalDate checkIn  = readDate(checkInPrompt);
            LocalDate checkOut = readDate(checkOutPrompt);
            String error = system.checkBookingDates(checkIn, checkOut);
            if (error == null) return new LocalDate[] { checkIn, checkOut };
            System.out.println("  [!] " + error);
        }
    }

    /**
     * Reads the number of guests, re-asking until it is between 1 and the
     * maximum occupancy of the chosen room (rule checked by K2703399_HotelSystem).
     *
     * @param prompt   message displayed before reading input
     * @param room     room being booked
     * @param checkIn  valid check-in date
     * @param checkOut valid check-out date
     * @return a valid number of guests
     */
    private int readGuestCount(String prompt, K2703399_Room room,
                                      LocalDate checkIn, LocalDate checkOut) {
        while (true) {
            int guests = readInt(prompt);
            String error = system.checkBookingDetails(room, checkIn, checkOut, guests);
            if (error == null) return guests;
            System.out.println("  [!] " + error);
        }
    }

    // -----------------------------------------------------------------------
    // Sample data loader
    // -----------------------------------------------------------------------

    /**
     * Pre-loads a set of sample rooms into the system.
     * This allows the application to be demonstrated immediately without
     * manual data entry. In a production system this would read from a database.
     */
    private void loadSampleData() {
        // HotelSystem builds each room through the factory method (Composition)
        system.addRoom("R001", K2703399_RoomType.STANDARD,     K2703399_BedType.SINGLE);
        system.addRoom("R002", K2703399_RoomType.STANDARD,     K2703399_BedType.DOUBLE);
        system.addRoom("R003", K2703399_RoomType.DELUXE,       K2703399_BedType.QUEEN);
        system.addRoom("R004", K2703399_RoomType.DELUXE,       K2703399_BedType.KING);
        system.addRoom("R005", K2703399_RoomType.SUITE,        K2703399_BedType.KING);
        system.addRoom("R006", K2703399_RoomType.SUITE,        K2703399_BedType.QUEEN);
        system.addRoom("R007", K2703399_RoomType.FAMILY_SUITE, K2703399_BedType.KING);
        system.addRoom("R008", K2703399_RoomType.FAMILY_SUITE, K2703399_BedType.QUEEN);
    }
}
