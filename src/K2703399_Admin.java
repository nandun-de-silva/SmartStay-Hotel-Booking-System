/**
 * Represents a SmartStay administrator who manages rooms and the system.
 *
 * OOP concepts demonstrated:
 *   - Inheritance   : extends K2703399_Person, reusing common person fields.
 *   - Polymorphism  : overrides getDetails() with admin-specific formatting.
 *   - Encapsulation : adminId is private with a getter.
 *
 * @author  K.H. Nandun Lakshan De Silva
 * @id      K2703399
 * @module  CI6115 Programming III
 */
public class K2703399_Admin extends K2703399_Person {

    // -----------------------------------------------------------------------
    // Private field
    // -----------------------------------------------------------------------
    private String adminId;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates an Admin with a given ID and personal details.
     *
     * @param adminId       unique administrator identifier
     * @param fullName      administrator's full name
     * @param contactNumber contact phone number
     * @param email         email address
     */
    public K2703399_Admin(String adminId, String fullName,
                         String contactNumber, String email) {
        super(fullName, contactNumber, email); // invoke parent constructor
        this.adminId = adminId;
    }

    // -----------------------------------------------------------------------
    // Overridden method (Polymorphism)
    // -----------------------------------------------------------------------

    /**
     * Returns a formatted string with admin details.
     * Overrides the abstract method declared in K2703399_Person.
     *
     * @return formatted admin details
     */
    @Override
    public String getDetails() {
        return String.format(
            "Admin ID: %-6s | Name: %-25s | Contact: %-12s | Email: %s",
            adminId, fullName, contactNumber, email // inherited protected fields
        );
    }

    // -----------------------------------------------------------------------
    // Getter (Encapsulation)
    // -----------------------------------------------------------------------

    public String getAdminId() { return adminId; }
}
