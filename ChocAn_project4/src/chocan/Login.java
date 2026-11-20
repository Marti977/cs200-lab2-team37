/** Author: Team 37 (original upload by taking10).
 * Integration and completion: Faizan Khan. */
package chocan;

import java.util.Scanner;

/** Validates credentials and routes users to role-specific menus. */
public class Login {
    private final Scanner input;
    public Login(Scanner input) {
        this.input = input;
        }
    public User authenticate() {
        System.out.println("\n--- ChocAn Login ---");
        System.out.print("Username (or EXIT): ");
        String username = input.nextLine().trim();
        if (username.equalsIgnoreCase("EXIT"))
            return new User("__EXIT__", Role.OPERATOR, null);
        System.out.print("Password: ");
        String password = input.nextLine();
        if (username.equals("operator") &&
                password.equals("op123"))
            return new User(username, Role.OPERATOR, null);
        if (username.equals("provider") &&
                password.equals("pr123"))
            return new User(username, Role.PROVIDER, "200000000");
        if (username.equals("manager") &&
                password.equals("mg123"))
            return new User(username, Role.MANAGER, null);
        System.out.println("Invalid username or password.");
        return null;
    }
}
