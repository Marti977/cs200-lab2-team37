/** Author: Team 37 (original upload by taking10).
 * Integration and completion: Faizan Khan. */
package chocan;

import java.util.Scanner;

public class Operator {
    private final Scanner in;
    private final MemberManager members;
    private final ProviderManager providers;
    private final ServiceManager services;
    public Operator(Scanner in, MemberManager members, ProviderManager providers, ServiceManager services) {
        this.in = in;
        this.members = members;
        this.providers = providers;
        this.services = services;
    }
    public void displayMenu() {
        while (true) {
            System.out.println("\n--- Operator Menu ---");
            System.out.println("1 Add member  2 Edit member  3 Delete member  4 List members");
            System.out.println("5 Add provider  6 Edit provider  7 Delete provider  8 List providers");
            System.out.println("9 Record service  10 Apply Acme membership updates  0 Logout");
            String c = in.nextLine().trim();
            try {
                switch (c) {
                    case "1" -> members.addMember();
                    case "2" -> members.editMember();
                    case "3" -> members.deleteMember();
                    case "4" -> members.listMembers();
                    case "5" -> providers.addProvider();
                    case "6" -> providers.editProvider();
                    case "7" -> providers.deleteProvider();
                    case "8" -> providers.listProviders();
                    case "9" -> services.recordService(null);
                    case "10" -> members.applyAcmeMembershipUpdates();
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
