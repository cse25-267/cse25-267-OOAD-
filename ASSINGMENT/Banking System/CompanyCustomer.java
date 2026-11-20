public class CompanyCustomer extends Customer {

    private String companyName;

    public CompanyCustomer(String customerId, String address, String companyName) {
        super(customerId, address);
        this.companyName = companyName;
    }

    public String getCompanyName() { return companyName; }

    @Override
    public String getDisplayName() {
        return companyName;
    }

    @Override
    public String getCustomerType() {
        return "Company";
    }
}
