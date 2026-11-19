package chocan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/** A billed service, including its original fee for later accounting reports.
 * Author: Maggie Skinner (AI-assisted implementation).
 */
public final class ServiceRecord {
    private final LocalDateTime receivedAt;
    private final LocalDate serviceDate;
    private final String providerNumber;
    private final String memberNumber;
    private final String serviceCode;
    private final String serviceName;
    private final BigDecimal fee;
    private final String comments;

    public ServiceRecord(LocalDateTime receivedAt, LocalDate serviceDate,
                         String providerNumber, String memberNumber,
                         String serviceCode, String serviceName,
                         BigDecimal fee, String comments) {
        this.receivedAt = Objects.requireNonNull(receivedAt, "Received time is required");
        this.serviceDate = Objects.requireNonNull(serviceDate, "Service date is required");
        if (serviceDate.isAfter(receivedAt.toLocalDate())) {
            throw new IllegalArgumentException("Service date cannot follow received date");
        }
        this.providerNumber = digits(providerNumber, 9, "Provider number");
        this.memberNumber = digits(memberNumber, 9, "Member number");
        this.serviceCode = digits(serviceCode, 6, "Service code");
        if (serviceName == null || serviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("Service name is required");
        }
        this.serviceName = serviceName;
        this.fee = Objects.requireNonNull(fee, "Fee is required").setScale(2, RoundingMode.UNNECESSARY);
        if (this.fee.signum() < 0) throw new IllegalArgumentException("Fee cannot be negative");
        this.comments = comments == null ? "" : comments;
        if (this.comments.length() > 100) throw new IllegalArgumentException("Comments exceed 100 characters");
    }

    private static String digits(String value, int length, String label) {
        if (value == null || !value.matches("[0-9]{" + length + "}")) {
            throw new IllegalArgumentException(label + " must have " + length + " digits");
        }
        return value;
    }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public LocalDate getServiceDate() { return serviceDate; }
    public String getProviderNumber() { return providerNumber; }
    public String getMemberNumber() { return memberNumber; }
    public String getServiceCode() { return serviceCode; }
    public String getServiceName() { return serviceName; }
    public BigDecimal getFee() { return fee; }
    public String getComments() { return comments; }
}
