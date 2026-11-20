package chocan;

import java.math.BigDecimal;

/** An entry in the demonstration service catalog. Author: Faizan Khan. */
public record Service(String code, String name, BigDecimal fee) {
    public Service {
        Validation.id(code, 6, "Service code");
        Validation.text(name, 40, "Service name");
        if (fee == null || fee.signum() < 0) throw new IllegalArgumentException("Invalid fee.");
        fee = fee.setScale(2, java.math.RoundingMode.UNNECESSARY);
    }
}
