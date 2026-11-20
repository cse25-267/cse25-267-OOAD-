import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            Dialogs.error("Enter your username and password.");
            return;
        }

        String result = LoginManager.login(username, password);
        if (result.startsWith("ERROR")) {
            Dialogs.error(result.substring(7));
            passwordField.clear();
            return;
        }

        openMainView();
    }

    // Replaces the login screen with the main banking screen in the same window
    private void openMainView() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("MainView.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 960, 600));
            stage.setTitle("Banking System");
        } catch (Exception e) {
            Dialogs.error("Could not open the main screen: " + e.getMessage());
        }
    }
}