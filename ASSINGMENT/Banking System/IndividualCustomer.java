public class IndividualCustomer extends Customer {

    private String firstName;
    private String surname;

    public IndividualCustomer(String customerId, String address,
                              String firstName, String surname) {
        super(customerId, address);   // calls Customer constructor
        this.firstName = firstName;
        this.surname   = surname;
    }

    public String getFirstName() { return firstName; }
    public String getSurname()   { return surname; }

    @Override
    public String getDisplayName() {
        return firstName + " " + surname;
    }

    @Override
    public String getCustomerType() {
        return "Individual";
    }
}
