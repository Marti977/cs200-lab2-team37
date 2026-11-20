package chocan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/** Validates members and records billable services. Author: Faizan Khan. */
public class ServiceManager {
    private final DataStore data;
    private final Scanner input;
    // Sample catalog for the course demonstration, not a real ChocAn fee schedule.
    private final List<Service> services = List.of(
        new Service("100001", "Dietitian consultation", new BigDecimal("75.00")),
        new Service("100002", "Exercise session", new BigDecimal("40.00")),
        new Service("100003", "Group therapy", new BigDecimal("35.00")),
        new Service("100004", "Individual counseling", new BigDecimal("90.00")),
        new Service("100005", "Nutrition workshop", new BigDecimal("25.00")));

    public ServiceManager(DataStore data, Scanner input) { this.data = data; this.input = input; }
    public List<Service> getServices() {
        return services.stream().sorted(Comparator.comparing(Service::name)).toList();
    }
    public Service findService(String code) {
        return services.stream().filter(s -> s.code().equals(code)).findFirst().orElse(null);
    }
    public String validateMember(String number) {
        if (number == null || !number.matches("[0-9]{9}")) return "Invalid member number";
        Member member = data.findMember(number);
        if (member == null) return "Invalid member number";
        return member.isActive() ? "Validated" : "Member suspended";
    }
    public void validateMember() {
        System.out.print("Member number (9 digits): ");
        System.out.println(validateMember(input.nextLine().trim()));
    }
    /** Programmatic entry point also used by the CLI and regression checks. */
    public ServiceRecord record(String providerId, String memberId, LocalDate date,
                                String code, String comments) {
        if (!"Validated".equals(validateMember(memberId)))
            throw new IllegalArgumentException(validateMember(memberId));
        if (data.findProvider(providerId) == null) throw new IllegalArgumentException("Provider not found");
        Service service = findService(code);
        if (service == null) throw new IllegalArgumentException("Invalid service code");
        ServiceRecord record = new ServiceRecord(LocalDateTime.now(), date, providerId,
                memberId, code, service.name(), service.fee(), comments);
        data.addServiceRecord(record);
        return record;
    }
    public void recordService(String providerId) {
        if (providerId == null) {
            System.out.print("Provider number (9 digits): ");
            providerId = input.nextLine().trim();
        }
        if (data.findProvider(providerId) == null) {
            System.out.println("Provider not found. Ask the operator to add this provider first."); return;
        }
        System.out.print("Member number (9 digits): ");
        String memberId = input.nextLine().trim();
        String status = validateMember(memberId);
        System.out.println(status);
        if (!status.equals("Validated")) return;
        System.out.print("Service date (YYYY-MM-DD): ");
        LocalDate date;
        try { date = LocalDate.parse(input.nextLine().trim()); }
        catch (DateTimeParseException e) { System.out.println("Invalid date. Use YYYY-MM-DD."); return; }
        if (date.isAfter(LocalDate.now())) { System.out.println("Service date cannot be in the future."); return; }
        System.out.print("Service code (6 digits; directory lists codes): ");
        String code = input.nextLine().trim();
        Service service = findService(code);
        if (service == null) { System.out.println("Invalid service code."); return; }
        System.out.println(service.name() + " | Fee: $" + service.fee());
        System.out.print("Confirm service (Y/N): ");
        if (!input.nextLine().trim().equalsIgnoreCase("Y")) { System.out.println("Service cancelled."); return; }
        System.out.print("Comments (optional, max 100 characters): ");
        record(providerId, memberId, date, code, input.nextLine());
        System.out.println("Service recorded successfully. Fee: $" + service.fee());
    }
}
