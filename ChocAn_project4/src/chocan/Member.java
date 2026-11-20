package chocan;

/**
 * Stores information about a ChocAn member.
 *
 * Author: Alyssa Duncan
 */
public class Member {

    private String memberNumber;
    private String name;
    private String streetAddress;
    private String city;
    private String state;
    private String zipCode;
    private String status;

    public Member() {
        this.status = "active";
    }

    public Member(String memberNumber, String name, String streetAddress,
                  String city, String state, String zipCode, String status) {
        this.memberNumber = memberNumber;
        this.name = name;
        this.streetAddress = streetAddress;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        setStatus(status);
    }

    public String getMemberNumber() {
        return memberNumber;
    }

    public void setMemberNumber(String memberNumber) {
        this.memberNumber = memberNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null ||
            (!status.equalsIgnoreCase("active") &&
             !status.equalsIgnoreCase("suspended"))) {

            throw new IllegalArgumentException(
                "Member status must be active or suspended."
            );
        }

        this.status = status.toLowerCase();
    }

    public boolean isActive() {
        return status.equalsIgnoreCase("active");
    }

    public boolean isSuspended() {
        return status.equalsIgnoreCase("suspended");
    }

    @Override
    public String toString() {
        return "Member Number: " + memberNumber +
               "\nName: " + name +
               "\nAddress: " + streetAddress +
               "\nCity: " + city +
               "\nState: " + state +
               "\nZIP: " + zipCode +
               "\nStatus: " + status;
    }
}
