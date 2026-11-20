import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load saved customers, accounts and transactions from the database
            String loadResult = Bank.getInstance().loadFromDatabase();
            if (loadResult.startsWith("ERROR")) {
                Dialogs.error(loadResult.substring(7));
            }

            // Load the FXML file
            Parent root = FXMLLoader.load(getClass().getResource("LoginView.fxml"));

            // Set up the stage (window)
            primaryStage.setTitle("Login");
            primaryStage.setScene(new Scene(root, 400, 320));
            primaryStage.show();
        } catch (Exception e) {
            System.out.println("Error loading the application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
