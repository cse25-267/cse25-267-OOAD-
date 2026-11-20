import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class CustomerController {

    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField idField, addressField, firstNameField, surnameField, companyField;
    @FXML private HBox individualBox, companyBox;
    @FXML private TableView<Customer> customersTable;
    @FXML private TableColumn<Customer, String> idCol, nameCol, typeCol, addressCol;

    private Bank bank = Bank.getInstance();

    @FXML
    private void initialize() {
        typeCombo.getItems().add("Individual");
        typeCombo.getItems().add("Company");
        typeCombo.getSelectionModel().selectFirst();
        onTypeChanged();

        idCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        nameCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getDisplayName()));
        typeCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getCustomerType()));

        refreshTable();
    }

    @FXML
    private void onTypeChanged() {
        boolean individual = "Individual".equals(typeCombo.getValue());
        individualBox.setVisible(individual);
        individualBox.setManaged(individual);
        companyBox.setVisible(!individual);
        companyBox.setManaged(!individual);
    }

    @FXML
    private void onAddCustomer() {
        String id      = idField.getText().trim();
        String address = addressField.getText().trim();
        if (id.isEmpty() || address.isEmpty()) {
            Dialogs.error("Customer ID and address are required.");
            return;
        }

        Customer customer;
        if ("Individual".equals(typeCombo.getValue())) {
            String first   = firstNameField.getText().trim();
            String surname = surnameField.getText().trim();
            if (first.isEmpty() || surname.isEmpty()) {
                Dialogs.error("First name and surname are required.");
                return;
            }
            customer = new IndividualCustomer(id, address, first, surname);
        } else {
            String company = companyField.getText().trim();
            if (company.isEmpty()) {
                Dialogs.error("Company name is required.");
                return;
            }
            customer = new CompanyCustomer(id, address, company);
        }

        String result = bank.addCustomer(customer);
        if (result.startsWith("ERROR")) {
            Dialogs.error(result.substring(7));
        } else {
            onClear();
            refreshTable();
        }
    }

    @FXML
    private void onClear() {
        idField.clear();
        addressField.clear();
        firstNameField.clear();
        surnameField.clear();
        companyField.clear();
    }

    // Copies the customers array into the table
    private void refreshTable() {
        customersTable.getItems().clear();
        for (int i = 0; i < bank.getCustomerCount(); i++) {
            customersTable.getItems().add(bank.getCustomer(i));
        }
    }
}
