package chocan;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Manages ChocAn member records.
 * Author: Alyssa Duncan (original CRUD implementation).
 * Integration, validation, and persistent updates: Faizan Khan.
 */
public class MemberManager {
    private final DataStore data;
    private final Scanner input;
    private final ArrayList<Member> members;
    public MemberManager(DataStore data, Scanner input) {
        this.data = data; this.input = input; this.members = data.getMembers();
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
    public Member findMember(String number) { return data.findMember(number); }
    public ArrayList<Member> getMembers() { return members; }
    public void addMember() {
        String number = read("Member number (9 digits)");
        Validation.id(number, 9, "Member number");
        if (findMember(number) != null) { System.out.println("Member already exists."); return; }
        if (data.getServiceRecords().stream().anyMatch(r -> r.getMemberNumber().equals(number))) {
            System.out.println("That number is reserved by historical service records."); return;
        }
        String name = read("Name");
        String streetAddress = read("Street address");
        String city = read("City");
        String state = read("State");
        String zipCode = read("ZIP code");
        Validation.person(number, name, streetAddress, city, state, zipCode);
        Member record = new Member(number, name, streetAddress, city, state.toUpperCase(java.util.Locale.ROOT), zipCode, "active");
        members.add(record);
        saveChange(() -> members.remove(record));
        System.out.println("Member added successfully.");
    }
    public void editMember() {
        String number = read("Member number");
        Member old = findMember(number);
        if (old == null) { System.out.println("Member not found."); return; }
        String name = edit("Name", old.getName());
        String streetAddress = edit("Street address", old.getStreetAddress());
        String city = edit("City", old.getCity());
        String state = edit("State", old.getState());
        String zipCode = edit("ZIP code", old.getZipCode());
        String status = edit("Status (active/suspended)", old.getStatus());
        Validation.person(number, name, streetAddress, city, state, zipCode);
        Member updated = new Member(number, name, streetAddress, city, state.toUpperCase(java.util.Locale.ROOT), zipCode, status);
        int index = members.indexOf(old);
        members.set(index, updated);
        saveChange(() -> members.set(index, old));
        System.out.println("Member updated successfully.");
    }
    public void deleteMember() {
        Member old = findMember(read("Member number"));
        if (old == null) { System.out.println("Member not found."); return; }
        if (!read("Delete " + old.getName() + "? (Y/N)").equalsIgnoreCase("Y")) {
            System.out.println("Delete cancelled."); return;
        }
        int index = members.indexOf(old);
        members.remove(index);
        saveChange(() -> members.add(index, old));
        System.out.println("Member deleted successfully.");
    }
    public void listMembers() {
        System.out.println("\n--- Member List ---");
        if (members.isEmpty()) System.out.println("No members found.");
        for (Member record : members) System.out.println(record + "\n");
    }
    /** Manual simulation of an Acme status update; no external connection. */
    public void applyAcmeMembershipUpdates() {
        Member member = findMember(read("Member number"));
        if (member == null) { System.out.println("Member not found."); return; }
        String old = member.getStatus();
        member.setStatus(read("New status (active/suspended)"));
        saveChange(() -> member.setStatus(old));
        System.out.println("Membership status updated successfully.");
    }
}
