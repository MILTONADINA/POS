package POSPD;

/** Personal details (name, address, contact, SSN) for a {@link Cashier}. */
public class Person {

    /** The person's full name. */
    private String name;

    /** Street address. */
    private String address;

    /** Home city. */
    private String city;

    /** Home state. */
    private String state;

    /** Postal/zip code. */
    private String zip;

    /** Phone number. */
    private String phone;

    /** Social Security Number. */
    private String sSN;

    /** The cashier this person is associated with (may be {@code null}). */
    private Cashier cashier;

    /** Creates an empty person with blank fields and no associated cashier. */
    public Person() {
        name = "";
        address = "";
        city = "";
        state = "";
        zip = "";
        phone = "";
        sSN = "";
        cashier = null;
    }

    /**
     * Creates a fully populated person.
     *
     * @param name the person's name
     * @param sSN the Social Security Number
     * @param address the street address
     * @param city the home city
     * @param state the home state
     * @param zip the postal/zip code
     * @param phone the phone number
     * @param cashier the associated cashier (may be {@code null})
     */
    public Person(
            String name,
            String sSN,
            String address,
            String city,
            String state,
            String zip,
            String phone,
            Cashier cashier) {
        this.name = name;
        this.sSN = sSN;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.phone = phone;
        this.cashier = cashier;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSSN() {
        return this.sSN;
    }

    public void setSSN(String sSN) {
        this.sSN = sSN;
    }

    public Cashier getCashier() {
        return cashier;
    }

    public void setCashier(Cashier cashier) {
        this.cashier = cashier;
    }

    @Override
    public String toString() {
        String cashierNumber = (cashier != null) ? cashier.getNumber() : "none";
        return "\nName: "
                + name
                + "\nAddress: "
                + address
                + "\nPhone: "
                + phone
                + "\nSSN: "
                + sSN
                + "\nCashier: "
                + cashierNumber;
    }
}
