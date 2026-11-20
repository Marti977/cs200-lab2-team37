/** Author: Team 37 (original upload by taking10).
 * Integration and completion: Faizan Khan. */
package chocan;

import java.util.Scanner;

public class ProviderUser {
    private final Scanner in;
    private final ServiceManager services;
    private final DataStore data;
    private final User user;
    public ProviderUser(Scanner in, ServiceManager services, DataStore data, User user) {
        this.in = in;
        this.services = services;
        this.data = data;
        this.user = user;
    }
    public void displayMenu() {
        while (true) {
            System.out.println("\n--- Provider Menu ---");
            System.out.println("1 Validate member and record service  2 View provider directory  3 Validate member  0 Logout");
            String c = in.nextLine().trim();
            try {
                switch (c) {
                    case "1" -> services.recordService(user.providerId());
                    case "2" -> {
                        ProviderDirectory directory = new ProviderDirectory(services);
                        directory.displayDirectory();
                        directory.generateDirectoryFile();
                        }
                    case "3" -> services.validateMember();
                    case "0" -> {
                        return;
                        }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (IllegalArgumentException | java.io.UncheckedIOException e) {
                System.out.println("Unable to complete request: " + e.getMessage());
            }
        }
    }
}
