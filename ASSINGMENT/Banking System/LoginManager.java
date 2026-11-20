public class LoginManager {

    public static final int MAX_USERS = 5;

    private static String[] usernames = new String[MAX_USERS];
    private static String[] passwords = new String[MAX_USERS];
    private static int userCount = 0;

    // Runs once when the class is first used - creates the allowed users
    static {
        usernames[0] = "admin";  passwords[0] = "admin123";
        usernames[1] = "teller"; passwords[1] = "teller123";
        userCount = 2;
    }

    // Returns "OK" or "ERROR: ..."
    public static String login(String username, String password) {
        for (int i = 0; i < userCount; i++) {
            if (usernames[i].equals(username) && passwords[i].equals(password)) {
                return "OK";
            }
        }
        return "ERROR: Invalid username or password.";
    }
}