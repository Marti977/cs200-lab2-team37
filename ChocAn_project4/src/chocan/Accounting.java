package chocan;

import java.time.LocalDate;
import java.util.List;
import java.util.TreeSet;

/** Generates a full accounting report set without issuing payments. Author: Faizan Khan. */
public class Accounting {
    private final DataStore data;
    private final ReportGenerator reports;
    public Accounting(DataStore data, ReportGenerator reports) { this.data = data; this.reports = reports; }
    public void runAccountingProcedure() {
        runAccountingProcedure(LocalDate.now().minusDays(6), LocalDate.now());
    }
    public void runAccountingProcedure(LocalDate from, LocalDate to) {
        List<ServiceRecord> rows = reports.records(from, to);
        // Include existing accounts with zero activity and deleted accounts with historical activity.
        TreeSet<String> members = new TreeSet<>(), providers = new TreeSet<>();
        data.getMembers().forEach(m -> members.add(m.getMemberNumber()));
        data.getProviders().forEach(p -> providers.add(p.getProviderNumber()));
        rows.forEach(r -> { members.add(r.getMemberNumber()); providers.add(r.getProviderNumber()); });
        for (String id : members)
            reports.writeReport("member-" + id + ".txt", reports.memberReport(id, from, to), from, to);
        for (String id : providers)
            reports.writeReport("provider-" + id + ".txt", reports.providerReport(id, from, to), from, to);
        reports.writeReport("eft.csv", reports.eftReport(from, to), from, to);
        String summary = reports.summaryReport(from, to);
        var path = reports.writeReport("summary.txt", summary, from, to);
        System.out.println(summary);
        System.out.println("Accounting complete: " + members.size() + " member reports, "
                + providers.size() + " provider reports, summary and EFT data file.\nSaved in: "
                + path.toAbsolutePath().getParent());
    }
}
