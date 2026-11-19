package chocan;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * Shared member, provider, and billed-service collections with file persistence.
 * Author: Maggie Skinner (AI-assisted implementation).
 * Single-threaded storage; scheduled jobs must not mutate it concurrently.
 */
public class DataStore {
    private final Path file;
    private final ArrayList<Member> members = new ArrayList<>();
    private final ArrayList<Provider> providers = new ArrayList<>();
    private final ArrayList<ServiceRecord> serviceRecords = new ArrayList<>();

    public DataStore() {
        this(Path.of("data", "member-provider-records.properties"));
    }

    /** Load an existing file, or start empty when no file exists. */
    public DataStore(Path file) {
        this.file = file.toAbsolutePath();
        if (Files.exists(this.file)) {
            load();
        }
    }

    /** Managers must share these collections instead of creating private lists. */
    public ArrayList<Member> getMembers() {
        return members;
    }

    public ArrayList<Provider> getProviders() {
        return providers;
    }

    public List<ServiceRecord> getServiceRecords() {
        return Collections.unmodifiableList(serviceRecords);
    }

    public Member findMember(String number) {
        for (Member member : members) {
            if (member.getMemberNumber().equals(number)) return member;
        }
        return null;
    }

    public Provider findProvider(String number) {
        for (Provider provider : providers) {
            if (provider.getProviderNumber().equals(number)) return provider;
        }
        return null;
    }

    /** Validate current membership and provider existence before accepting a billing record.
     * ServiceManager must also validate the service code against its actual directory.
     * If disk saving fails, roll back the new service so retrying will not double-bill it.
     */
    public void addServiceRecord(ServiceRecord record) {
        java.util.Objects.requireNonNull(record, "Service record is required");
        Member member = findMember(record.getMemberNumber());
        if (member == null || !member.isActive()) {
            throw new IllegalArgumentException("Member is missing or suspended");
        }
        if (findProvider(record.getProviderNumber()) == null) {
            throw new IllegalArgumentException("Provider not found");
        }
        serviceRecords.add(record);
        try {
            save();
        } catch (RuntimeException e) {
            serviceRecords.remove(serviceRecords.size() - 1);
            throw e;
        }
    }

    /** Write through a temporary file so a failed write preserves the previous save. */
    public void save() {
        Properties p = new Properties();
        p.setProperty("version", "1");
        p.setProperty("services.count", String.valueOf(serviceRecords.size()));
        p.setProperty("members.count", String.valueOf(members.size()));
        p.setProperty("providers.count", String.valueOf(providers.size()));
        for (int i = 0; i < members.size(); i++) {
            Member m = members.get(i);
            String key = "member." + i + ".";
            putPerson(p, key, m.getMemberNumber(), m.getName(), m.getStreetAddress(),
                    m.getCity(), m.getState(), m.getZipCode());
            p.setProperty(key + "status", m.getStatus());
        }
        for (int i = 0; i < providers.size(); i++) {
            Provider v = providers.get(i);
            String key = "provider." + i + ".";
            putPerson(p, key, v.getProviderNumber(), v.getName(), v.getStreetAddress(),
                    v.getCity(), v.getState(), v.getZipCode());
            p.setProperty(key + "type", v.getProviderType());
            p.setProperty(key + "email", v.getEmail());
        }
        for (int i = 0; i < serviceRecords.size(); i++) {
            ServiceRecord r = serviceRecords.get(i);
            String key = "service." + i + ".";
            p.setProperty(key + "receivedAt", r.getReceivedAt().toString());
            p.setProperty(key + "serviceDate", r.getServiceDate().toString());
            p.setProperty(key + "provider", r.getProviderNumber());
            p.setProperty(key + "member", r.getMemberNumber());
            p.setProperty(key + "code", r.getServiceCode());
            p.setProperty(key + "name", r.getServiceName());
            p.setProperty(key + "fee", r.getFee().toPlainString());
            p.setProperty(key + "comments", r.getComments());
        }
        Path temporary = null;
        try {
            Files.createDirectories(file.getParent());
            temporary = Files.createTempFile(file.getParent(), "chocan-", ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                p.store(writer, "ChocAn member and provider records");
            }
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save member/provider records to " + file, e);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                    // A cleanup failure must not hide the original save error.
                }
            }
        }
    }

    private static void putPerson(Properties p, String key, String number, String name,
                                  String address, String city, String state, String zip) {
        p.setProperty(key + "number", number);
        p.setProperty(key + "name", name);
        p.setProperty(key + "address", address);
        p.setProperty(key + "city", city);
        p.setProperty(key + "state", state);
        p.setProperty(key + "zip", zip);
    }

    private void load() {
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            p.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load records from " + file, e);
        }
        // Reject incomplete/corrupt files rather than silently replacing them with empty data.
        if (!"1".equals(required(p, "version"))) {
            throw new IllegalArgumentException("Unsupported records file version: " + file);
        }
        int memberCount = count(p, "members.count");
        int providerCount = count(p, "providers.count");
        for (int i = 0; i < memberCount; i++) {
            String k = "member." + i + ".";
            members.add(new Member(required(p, k + "number"), required(p, k + "name"),
                    required(p, k + "address"), required(p, k + "city"),
                    required(p, k + "state"), required(p, k + "zip"), required(p, k + "status")));
        }
        for (int i = 0; i < providerCount; i++) {
            String k = "provider." + i + ".";
            providers.add(new Provider(required(p, k + "number"), required(p, k + "name"),
                    required(p, k + "address"), required(p, k + "city"),
                    required(p, k + "state"), required(p, k + "zip"),
                    required(p, k + "type"), required(p, k + "email")));
        }
            int serviceCount = p.containsKey("services.count") ? count(p, "services.count") : 0;
        for (int i = 0; i < serviceCount; i++) {
            String k = "service." + i + ".";
            // Historical services remain valid even after a member/provider is deleted.
            serviceRecords.add(new ServiceRecord(
                    LocalDateTime.parse(required(p, k + "receivedAt")),
                    LocalDate.parse(required(p, k + "serviceDate")),
                    required(p, k + "provider"), required(p, k + "member"),
                    required(p, k + "code"), required(p, k + "name"),
                    new BigDecimal(required(p, k + "fee")), required(p, k + "comments")));
        }
    }

    private static String required(Properties p, String key) {
        String value = p.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Records file is missing " + key);
        }
        return value;
    }

    private static int count(Properties p, String key) {
        int value = Integer.parseInt(required(p, key));
        if (value < 0) {
            throw new IllegalArgumentException("Negative record count: " + key);
        }
        return value;
    }
}
