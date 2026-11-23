/**
 * Abstract base class representing any person in the SmartStay system.
 *
 * OOP concepts demonstrated:
 *   - Abstraction   : class is abstract; getDetails() is abstract — subclasses
 *                     must provide their own implementation.
 *   - Encapsulation : fields are protected — subclasses can use them directly,
 *                     other classes must go through the getters/setters.
 *   - Inheritance   : K2703399_Guest and K2703399_Admin both extend this class.
 *
 * @author  K.H. Nandun Lakshan De Silva
 * @id      K2703399
 * @module  CI6115 Programming III
 */
public abstract class K2703399_Person {

    // -----------------------------------------------------------------------
    // Protected fields (Encapsulation — visible to subclasses only)
    // -----------------------------------------------------------------------
    protected String fullName;
    protected String contactNumber;
    protected String email;

    // -----------------------------------------------------------------------
    // Constructors (Constructor Overloading)
    // -----------------------------------------------------------------------

    /** Default constructor — initialises fields to empty strings.
     *  Protected: only subclasses can call it, because Person is abstract. */
    protected K2703399_Person() {
        this.fullName      = "";
        this.contactNumber = "";
        this.email         = "";
    }

    /**
     * Parameterised constructor.
     *
     * @param fullName      the person's full name
     * @param contactNumber the person's phone / mobile number
     * @param email         the person's email address
     */
    protected K2703399_Person(String fullName, String contactNumber, String email) {
        this.fullName      = fullName;
        this.contactNumber = contactNumber;
        this.email         = email;
    }

    // -----------------------------------------------------------------------
    // Abstract method — forces subclasses to define their own details string.
    // This enables runtime polymorphism: calling getDetails() on an
    // K2703399_Person reference resolves to the correct subclass version.
    // -----------------------------------------------------------------------

    /**
     * Returns a formatted string describing this person.
     * Each subclass provides its own implementation (Polymorphism).
     *
     * @return formatted details string
     */
    public abstract String getDetails();

    // -----------------------------------------------------------------------
    // Getters and Setters (Encapsulation)
    // -----------------------------------------------------------------------

    public String getFullName()                        { return fullName; }
    public void   setFullName(String fullName)         { this.fullName = fullName; }

    public String getContactNumber()                   { return contactNumber; }
    public void   setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getEmail()                           { return email; }
    public void   setEmail(String email)               { this.email = email; }

    // -----------------------------------------------------------------------
    // toString delegates to the polymorphic getDetails()
    // -----------------------------------------------------------------------
    @Override
    public String toString() { return getDetails(); }
}
