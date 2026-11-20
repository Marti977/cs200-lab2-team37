package chocan;

/**
 * Stores information about a ChocAn provider.
 *
 * Author: Alyssa Duncan
 */
public class Provider {

    private String providerNumber;
    private String name;
    private String streetAddress;
    private String city;
    private String state;
    private String zipCode;
    private String providerType;
    private String email;

    public Provider() {
    }

    public Provider(String providerNumber, String name,
                    String streetAddress, String city,
                    String state, String zipCode,
                    String providerType, String email) {

        this.providerNumber = providerNumber;
        this.name = name;
        this.streetAddress = streetAddress;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.providerType = providerType;
        this.email = email;
    }

    public String getProviderNumber() {
        return providerNumber;
    }

    public void setProviderNumber(String providerNumber) {
        this.providerNumber = providerNumber;
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

    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Provider Number: " + providerNumber +
               "\nName: " + name +
               "\nAddress: " + streetAddress +
               "\nCity: " + city +
               "\nState: " + state +
               "\nZIP: " + zipCode +
               "\nProvider Type: " + providerType +
               "\nEmail: " + email;
    }
}
