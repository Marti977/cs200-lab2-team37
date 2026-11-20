package chocan;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Builds readable, date-filtered reports from saved services. Author: Faizan Khan. */
public class ReportGenerator {
    private final DataStore data;
    private final Path outputDirectory;
    public ReportGenerator(DataStore data) { this(data, Path.of("reports")); }
    public ReportGenerator(DataStore data, Path outputDirectory) {
        this.data = data; this.outputDirectory = outputDirectory;
    }
    public List<ServiceRecord> records(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to))
            throw new IllegalArgumentException("Report start date must be on or before end date.");
        return data.getServiceRecords().stream()
                .filter(r -> !r.getServiceDate().isBefore(from) && !r.getServiceDate().isAfter(to))
                .sorted(Comparator.comparing(ServiceRecord::getServiceDate)
                        .thenComparing(ServiceRecord::getReceivedAt)).toList();
    }
    private String header(String title, LocalDate from, LocalDate to) {
        return "CHOCAN " + title + "\nService dates: " + from + " through " + to + " (inclusive)\n\n";
    }
    private String memberName(String id) {
        Member m = data.findMember(id); return m == null ? "Deleted member " + id : m.getName();
    }
    private String providerName(String id) {
        Provider p = data.findProvider(id); return p == null ? "Deleted provider " + id : p.getName();
    }
    private BigDecimal total(List<ServiceRecord> rows) {
        return rows.stream().map(ServiceRecord::getFee).reduce(new BigDecimal("0.00"), BigDecimal::add);
    }
    public String memberReport(String id, LocalDate from, LocalDate to) {
        Validation.id(id, 9, "Member number");
        List<ServiceRecord> all = records(from, to);
        Member member = data.findMember(id);
        if (member == null && data.getServiceRecords().stream().noneMatch(r -> r.getMemberNumber().equals(id)))
            throw new IllegalArgumentException("Member not found");
        StringBuilder text = new StringBuilder(header("MEMBER REPORT", from, to));
        text.append(member == null ? "Member: " + memberName(id) : member.toString()).append("\n\n");
        List<ServiceRecord> rows = all.stream().filter(r -> r.getMemberNumber().equals(id)).toList();
        for (ServiceRecord r : rows) text.append(r.getServiceDate()).append(" | ")
                .append(providerName(r.getProviderNumber())).append(" | ").append(r.getServiceName()).append('\n');
        if (rows.isEmpty()) text.append("No services in this period.\n");
        return text.append("Total services: ").append(rows.size()).append('\n').toString();
    }
    public String providerReport(String id, LocalDate from, LocalDate to) {
        Validation.id(id, 9, "Provider number");
        List<ServiceRecord> all = records(from, to);
        Provider provider = data.findProvider(id);
        if (provider == null && data.getServiceRecords().stream().noneMatch(r -> r.getProviderNumber().equals(id)))
            throw new IllegalArgumentException("Provider not found");
        StringBuilder text = new StringBuilder(header("PROVIDER REPORT", from, to));
        text.append(provider == null ? "Provider: " + providerName(id) : provider.toString()).append("\n\n");
        List<ServiceRecord> rows = all.stream().filter(r -> r.getProviderNumber().equals(id)).toList();
        for (ServiceRecord r : rows) text.append("Service date: ").append(r.getServiceDate())
            .append(" | Received: ").append(r.getReceivedAt()).append('\n')
            .append("Member: ").append(memberName(r.getMemberNumber())).append(" (")
            .append(r.getMemberNumber()).append(") | Code: ").append(r.getServiceCode())
            .append(" | ").append(r.getServiceName()).append(" | $").append(r.getFee()).append('\n');
        if (rows.isEmpty()) text.append("No services in this period.\n");
        return text.append("Total consultations: ").append(rows.size())
                .append("\nTotal fee: $").append(total(rows)).append('\n').toString();
    }
    public String summaryReport(LocalDate from, LocalDate to) {
        List<ServiceRecord> rows = records(from, to);
        Map<String, List<ServiceRecord>> grouped = groupProviders(rows);
        StringBuilder text = new StringBuilder(header("SUMMARY REPORT", from, to));
        for (var entry : grouped.entrySet()) text.append(entry.getKey()).append(" | ")
            .append(providerName(entry.getKey())).append(" | Consultations: ").append(entry.getValue().size())
            .append(" | Fee: $").append(total(entry.getValue())).append('\n');
        return text.append("Providers to pay: ").append(grouped.size())
            .append("\nTotal consultations: ").append(rows.size())
            .append("\nOverall fee: $").append(total(rows)).append('\n').toString();
    }
    private Map<String, List<ServiceRecord>> groupProviders(List<ServiceRecord> rows) {
        Map<String, List<ServiceRecord>> grouped = new TreeMap<>();
        for (ServiceRecord r : rows)
            grouped.computeIfAbsent(r.getProviderNumber(), k -> new java.util.ArrayList<>()).add(r);
        return grouped;
    }
    /** Local EFT data file only; this does not execute payments. */
    public String eftReport(LocalDate from, LocalDate to) {
        StringBuilder text = new StringBuilder("provider_number,provider_name,amount\n");
        for (var entry : groupProviders(records(from, to)).entrySet())
            text.append(entry.getKey()).append(",\"")
                .append(providerName(entry.getKey()).replace("\"", "\"\""))
                .append("\",").append(total(entry.getValue())).append('\n');
        return text.toString();
    }
    public Path writeReport(String filename, String text, LocalDate from, LocalDate to) {
        records(from, to); // Reject invalid date ranges even when writing a supplied report.
        if (!filename.matches("[A-Za-z0-9_-]+\\.(txt|csv)"))
            throw new IllegalArgumentException("Invalid report filename");
        Path file = outputDirectory.resolve(from + "_to_" + to).resolve(filename);
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, text, StandardCharsets.UTF_8);
        } catch (IOException e) { throw new UncheckedIOException("Could not write report: " + file, e); }
        return file;
    }
}
