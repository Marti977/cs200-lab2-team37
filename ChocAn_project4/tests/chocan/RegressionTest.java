package chocan;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Scanner;

/** Executable integration/regression checks. Author: Faizan Khan. */
public class RegressionTest {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    private static void rejected(Runnable action, String message) {
        try { action.run(); } catch (IllegalArgumentException | java.io.UncheckedIOException e) {
            checks++; return;
        }
        throw new AssertionError(message);
    }
    private static Member member(String id) {
        return new Member(id, "Alex Member", "1 Main St", "Tuscaloosa", "AL", "35401", "active");
    }
    private static Provider provider(String id) {
        return new Provider(id, "Dana Provider", "2 Main St", "Tuscaloosa", "AL", "35401", "Counselor", "dana@example.com");
    }
    public static void main(String[] args) throws Exception {
        Path temp = Files.createTempDirectory("chocan-regression-");
        DataStore data = new DataStore(temp.resolve("data.properties"));
        data.getMembers().add(member("100000001"));
        data.getMembers().add(member("100000002"));
        data.getProviders().add(provider("200000000"));
        data.getProviders().add(provider("200000001"));
        data.save();
        ServiceManager services = new ServiceManager(data, new Scanner(""));
        check(services.validateMember("100000001").equals("Validated"), "Active member");
        check(services.validateMember("bad").contains("Invalid"), "Malformed member");
        check(services.validateMember("999999999").contains("Invalid"), "Missing member");
        data.findMember("100000002").setStatus("suspended");
        check(services.validateMember("100000002").equals("Member suspended"), "Suspended member");
        LocalDate day = LocalDate.now();
        rejected(() -> services.record("200000000", "100000002", day, "100001", ""), "Suspended billing accepted");
        rejected(() -> services.record("999999999", "100000001", day, "100001", ""), "Missing provider accepted");
        rejected(() -> services.record("200000000", "100000001", day, "999999", ""), "Bad service accepted");
        rejected(() -> services.record("200000000", "100000001", day.plusDays(1), "100001", ""), "Future date accepted");
        rejected(() -> services.record("200000000", "100000001", day, "100001", "x".repeat(101)), "Long comments accepted");
        check(data.getServiceRecords().isEmpty(), "Invalid requests persisted");
        services.record("200000000", "100000001", day, "100001", "Session one");
        services.record("200000000", "100000001", day.minusDays(1), "100002", "");
        services.record("200000001", "100000001", day, "100003", "");
        services.record("200000000", "100000001", day.minusDays(10), "100004", "Outside period");
        check(data.getServiceRecords().size() == 4, "Record count");
        DataStore loaded = new DataStore(temp.resolve("data.properties"));
        check(loaded.getServiceRecords().size() == 4 && loaded.getMembers().size() == 2, "Persistence reload");
        check(loaded.findMember("100000002").isSuspended(), "Status persistence");
        check(loaded.getServiceRecords().get(0).getFee().equals(new BigDecimal("75.00")), "Fee persistence");
        ReportGenerator reports = new ReportGenerator(loaded, temp.resolve("reports"));
        LocalDate from = day.minusDays(6);
        check(reports.records(from, day).size() == 3, "Date filtering");
        String summary = reports.summaryReport(from, day);
        check(summary.contains("Overall fee: $150.00"), "Summary money");
        check(summary.contains("Providers to pay: 2"), "Summary providers");
        check(summary.contains("Total consultations: 3"), "Summary consultations");
        check(reports.providerReport("200000000", from, day).contains("Total fee: $115.00"), "Provider total");
        String memberReport = reports.memberReport("100000001", from, day);
        check(memberReport.indexOf(day.minusDays(1) + " |") < memberReport.indexOf(day + " |"), "Chronological member report");
        check(reports.memberReport("100000002", from, day).contains("No services"), "Empty member report");
        check(reports.eftReport(from, day).contains("200000000,\"Dana Provider\",115.00"), "EFT aggregation");
        rejected(() -> reports.records(day, from), "Reversed date range accepted");
        rejected(() -> reports.memberReport("999999999", from, day), "Unknown member report accepted");
        rejected(() -> reports.writeReport("../escape.txt", "bad", from, day), "Unsafe filename accepted");
        Accounting accounting = new Accounting(loaded, reports);
        accounting.runAccountingProcedure(from, day);
        Path summaryFile = temp.resolve("reports/" + from + "_to_" + day + "/summary.txt");
        check(Files.readString(summaryFile).equals(summary), "Saved summary");
        accounting.runAccountingProcedure(from, day);
        check(loaded.getServiceRecords().size() == 4 && Files.readString(summaryFile).equals(summary), "Repeated accounting changed data/totals");
        loaded.getProviders().remove(loaded.findProvider("200000001"));
        loaded.getMembers().remove(loaded.findMember("100000001"));
        loaded.save();
        DataStore history = new DataStore(temp.resolve("data.properties"));
        ReportGenerator historical = new ReportGenerator(history, temp.resolve("historical"));
        check(historical.providerReport("200000001", from, day).contains("Deleted provider"), "Deleted provider history");
        check(historical.memberReport("100000001", from, day).contains("Total services: 3"), "Deleted member history");
        check(new ProviderDirectory(services).directoryText().indexOf("Dietitian") <
                new ProviderDirectory(services).directoryText().indexOf("Nutrition"), "Directory sorting");
        // Failure to save a service must roll back the in-memory append.
        Path blocked = temp.resolve("blocked");
        DataStore failed = new DataStore(blocked.resolve("data.properties"));
        failed.getMembers().add(member("100000001")); failed.getProviders().add(provider("200000000"));
        Files.writeString(blocked, "not a directory");
        rejected(() -> new ServiceManager(failed, new Scanner("")).record("200000000", "100000001", day, "100001", ""), "Disk failure ignored");
        check(failed.getServiceRecords().isEmpty(), "Failed service not rolled back");
        // Manager additions share storage and survive process-style reconstruction.
        DataStore crud = new DataStore(temp.resolve("crud.properties"));
        new MemberManager(crud, new Scanner("100000003\nTest Member\n1 Oak St\nTuscaloosa\nAL\n35401\n")).addMember();
        check(new DataStore(temp.resolve("crud.properties")).findMember("100000003") != null, "Member manager persistence");
        new ProviderManager(crud, new Scanner("200000003\nTest Provider\n2 Oak St\nTuscaloosa\nAL\n35401\nTherapist\ntest@example.com\n")).addProvider();
        check(new DataStore(temp.resolve("crud.properties")).findProvider("200000003") != null, "Provider manager persistence");
        rejected(() -> new MemberManager(crud, new Scanner("abc\n")).addMember(), "Bad member ID accepted");
        new MemberManager(crud, new Scanner("100000003\nUpdated Member\n\n\n\n\nsuspended\n")).editMember();
        check(new DataStore(temp.resolve("crud.properties")).findMember("100000003").isSuspended(), "Member edit persistence");
        new ProviderManager(crud, new Scanner("200000003\nUpdated Provider\n\n\n\n\n\n\n")).editProvider();
        check(new DataStore(temp.resolve("crud.properties")).findProvider("200000003").getName().equals("Updated Provider"), "Provider edit persistence");
        new MemberManager(crud, new Scanner("100000003\nN\n")).deleteMember();
        check(crud.findMember("100000003") != null, "Delete cancellation");
        new MemberManager(crud, new Scanner("100000003\nY\n")).deleteMember();
        new ProviderManager(crud, new Scanner("200000003\nY\n")).deleteProvider();
        check(new DataStore(temp.resolve("crud.properties")).getMembers().isEmpty(), "Member deletion persistence");
        check(new DataStore(temp.resolve("crud.properties")).getProviders().isEmpty(), "Provider deletion persistence");
        // Deleted IDs with historical billing cannot be assigned to a different person.
        new MemberManager(history, new Scanner("100000001\n")).addMember();
        new ProviderManager(history, new Scanner("200000001\n")).addProvider();
        check(history.findMember("100000001") == null, "Historical member ID reused");
        check(history.findProvider("200000001") == null, "Historical provider ID reused");
        // Wrong password must not produce an authenticated session.
        check(new Login(new Scanner("manager\nwrong\n")).authenticate() == null, "Invalid login accepted");
        check(new Login(new Scanner("manager\nmg123\n")).authenticate().role() == Role.MANAGER, "Manager login");
        check(new Login(new Scanner("provider\npr123\n")).authenticate().providerId().equals("200000000"), "Provider identity");
        check(new Login(new Scanner("operator\nop123\n")).authenticate().role() == Role.OPERATOR, "Operator login");
        rejected(() -> new ServiceRecord(LocalDateTime.now(), day, "200000000", "100000001", "100001", "Test", new BigDecimal("-1.00"), ""), "Negative fee accepted");
        System.out.println("PASS: " + checks + " regression checks.");
    }
}
