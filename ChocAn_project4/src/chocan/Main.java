/** Author: Team 37 (original upload by taking10).
 * Integration and completion: Faizan Khan. */
package chocan;

import java.util.Scanner;

/** Starts ChocAn, authenticates users, and opens the correct role menu. */
public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        DataStore data = new DataStore();

        Login login = new Login(input);
        MemberManager members = new MemberManager(data, input);
        ProviderManager providers = new ProviderManager(data, input);
        ServiceManager services = new ServiceManager(data, input);

        ReportGenerator reports = new ReportGenerator(data);
        Accounting accounting = new Accounting(data, reports);
        ProviderDirectory directory = new ProviderDirectory(services);

        System.out.println("====================================");
        System.out.println(" CHOCOHOLICS ANONYMOUS (ChocAn)");
        System.out.println("====================================");
        System.out.println("Demo login: operator/op123 | provider/pr123 | manager/mg123");

        try {
            while (true) {
                User user = login.authenticate();

                if (user == null) {
                    continue;
                }

                if (user.username().equals("__EXIT__")) {
                    break;
                }

                switch (user.role()) {
                    case OPERATOR ->
                        new Operator(input, members, providers, services).displayMenu();

                    case PROVIDER ->
                        new ProviderUser(input, services, data, user).displayMenu();

                    case MANAGER ->
                        new Manager(input, accounting, reports, directory).displayMenu();
                }
            }

        } catch (java.util.NoSuchElementException e) {
            System.out.println("\nInput closed.");
        } finally {
            data.save();
        }

        System.out.println("ChocAn closed. Saved records remain available next time.");
    }
}
