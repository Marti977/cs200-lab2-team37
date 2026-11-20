package chocan;

/** Input checks shared by the record managers. Author: Faizan Khan. */
public final class Validation {
    private Validation() { }
    public static void id(String value, int length, String label) {
        if (value == null || !value.matches("[0-9]{" + length + "}"))
            throw new IllegalArgumentException(label + " must contain " + length + " digits.");
    }
    public static void text(String value, int max, String label) {
        if (value == null || value.isBlank() || value.length() > max)
            throw new IllegalArgumentException(label + " must contain 1-" + max + " characters.");
    }
    public static void person(String number, String name, String street, String city,
                              String state, String zip) {
        id(number, 9, "Number");
        text(name, 25, "Name"); text(street, 25, "Street address"); text(city, 14, "City");
        if (state == null || !state.matches("[A-Za-z]{2}"))
            throw new IllegalArgumentException("State must contain two letters.");
        id(zip, 5, "ZIP code");
    }
}
