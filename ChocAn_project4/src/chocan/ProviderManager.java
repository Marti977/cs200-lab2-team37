package chocan;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Manages ChocAn provider records.
 * Author: Alyssa Duncan (original CRUD implementation).
 * Integration, validation, and persistent updates: Faizan Khan.
 */
public class ProviderManager {
    private final DataStore data;
    private final Scanner input;
    private final ArrayList<Provider> providers;
    public ProviderManager(DataStore data, Scanner input) {
        this.data = data; this.input = input; this.providers = data.getProviders();
    }
    private String read(String prompt) {
        System.out.print(prompt + ": "); return input.nextLine().trim();
    }
    private String edit(String label, String current) {
        String value = read(label + " [" + current + "] (ENTER keeps value)");
        return value.isEmpty() ? current : value;
    }
    private void saveChange(Runnable rollback) {
        try { data.save(); } catch (RuntimeException e) { rollback.run(); throw e; }
    }
    public Provider findProvider(String number) { return data.findProvider(number); }
    public ArrayList<Provider> getProviders() { return providers; }
    public void addProvider() {
        String number = read("Provider number (9 digits)");
        Validation.id(number, 9, "Provider number");
        if (findProvider(number) != null) { System.out.println("Provider already exists."); return; }
        if (data.getServiceRecords().stream().anyMatch(r -> r.getProviderNumber().equals(number))) {
            System.out.println("That number is reserved by historical service records."); return;
        }
        String name = read("Name");
        String streetAddress = read("Street address");
        String city = read("City");
        String state = read("State");
        String zipCode = read("ZIP code");
        String providerType = read("Provider type");
        String email = read("Email");
        Validation.person(number, name, streetAddress, city, state, zipCode);
        Validation.text(providerType, 60, "Provider type");
        Validation.text(email, 254, "Email");
        Provider record = new Provider(number, name, streetAddress, city, state.toUpperCase(java.util.Locale.ROOT), zipCode, providerType, email);
        providers.add(record);
        saveChange(() -> providers.remove(record));
        System.out.println("Provider added successfully.");
    }
    public void editProvider() {
        String number = read("Provider number");
        Provider old = findProvider(number);
        if (old == null) { System.out.println("Provider not found."); return; }
        String name = edit("Name", old.getName());
        String streetAddress = edit("Street address", old.getStreetAddress());
        String city = edit("City", old.getCity());
        String state = edit("State", old.getState());
        String zipCode = edit("ZIP code", old.getZipCode());
        String providerType = edit("Provider type", old.getProviderType());
        String email = edit("Email", old.getEmail());
        Validation.person(number, name, streetAddress, city, state, zipCode);
        Validation.text(providerType, 60, "Provider type");
        Validation.text(email, 254, "Email");
        Provider updated = new Provider(number, name, streetAddress, city, state.toUpperCase(java.util.Locale.ROOT), zipCode, providerType, email);
        int index = providers.indexOf(old);
        providers.set(index, updated);
        saveChange(() -> providers.set(index, old));
        System.out.println("Provider updated successfully.");
    }
    public void deleteProvider() {
        Provider old = findProvider(read("Provider number"));
        if (old == null) { System.out.println("Provider not found."); return; }
        if (!read("Delete " + old.getName() + "? (Y/N)").equalsIgnoreCase("Y")) {
            System.out.println("Delete cancelled."); return;
        }
        int index = providers.indexOf(old);
        providers.remove(index);
        saveChange(() -> providers.add(index, old));
        System.out.println("Provider deleted successfully.");
    }
    public void listProviders() {
        System.out.println("\n--- Provider List ---");
        if (providers.isEmpty()) System.out.println("No providers found.");
        for (Provider record : providers) System.out.println(record + "\n");
    }
}
