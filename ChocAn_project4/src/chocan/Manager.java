package chocan;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/** Manager menu for accounting, individual reports and the directory. Author: Faizan Khan. */
public class Manager {
    private final Scanner input;
    private final Accounting accounting;
    private final ReportGenerator reports;
    private final ProviderDirectory directory;
    public Manager(Scanner input, Accounting accounting, ReportGenerator reports, ProviderDirectory directory) {
        this.input = input; this.accounting = accounting; this.reports = reports; this.directory = directory;
    }
    private String read(String prompt) { System.out.print(prompt); return input.nextLine().trim(); }
    public void displayMenu() {
        while (true) {
            System.out.println("\n--- Manager Menu ---\n1 Run accounting  2 Summary report  3 Member report"
                    + "  4 Provider report  5 Provider directory  0 Logout");
            String choice = input.nextLine().trim();
            if (choice.equals("0")) return;
            try {
                if (choice.equals("5")) { directory.displayDirectory(); directory.generateDirectoryFile(); continue; }
                if (!choice.matches("[1-4]")) { System.out.println("Invalid choice."); continue; }
                String start = read("Start date YYYY-MM-DD (ENTER = six days ago): ");
                String end = read("End date YYYY-MM-DD (ENTER = today): ");
                LocalDate from = start.isEmpty() ? LocalDate.now().minusDays(6) : LocalDate.parse(start);
                LocalDate to = end.isEmpty() ? LocalDate.now() : LocalDate.parse(end);
                reports.records(from, to);
                if (choice.equals("1")) { accounting.runAccountingProcedure(from, to); continue; }
                String text, filename;
                switch (choice) {
                    case "2" -> { text = reports.summaryReport(from, to); filename = "summary.txt"; }
                    case "3" -> {
                        String id = read("Member number: ");
                        text = reports.memberReport(id, from, to); filename = "member-" + id + ".txt";
                    }
                    case "4" -> {
                        String id = read("Provider number: ");
                        text = reports.providerReport(id, from, to); filename = "provider-" + id + ".txt";
                    }
                    default -> throw new IllegalArgumentException("Invalid choice");
                }
                System.out.println(text);
                System.out.println("Saved: " + reports.writeReport(filename, text, from, to).toAbsolutePath());
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Use YYYY-MM-DD.");
            } catch (IllegalArgumentException | java.io.UncheckedIOException e) {
                System.out.println("Unable to complete request: " + e.getMessage());
            }
        }
    }
}
