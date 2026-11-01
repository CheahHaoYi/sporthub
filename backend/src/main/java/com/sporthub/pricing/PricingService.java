package com.sporthub.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/** Pure pricing rules shared by cart previews and checkout recalculation. */
public final class PricingService {
    public enum Classification { CHILD, ADULT, SENIOR }
    public record Price(BigDecimal base, BigDecimal discount, BigDecimal payable, Classification classification) { }

    private static final BigDecimal CHILD_RATE = new BigDecimal("0.20");
    private static final BigDecimal SENIOR_RATE = new BigDecimal("0.15");
    private static final BigDecimal ZERO_RATE = BigDecimal.ZERO;

    public Classification classify(LocalDate dateOfBirth, LocalDate appointmentDate) {
        Objects.requireNonNull(dateOfBirth, "dateOfBirth");
        Objects.requireNonNull(appointmentDate, "appointmentDate");
        if (dateOfBirth.isAfter(appointmentDate)) throw new IllegalArgumentException("Date of birth cannot be after appointment date.");
        int age = Period.between(dateOfBirth, appointmentDate).getYears();
        if (age < 18) return Classification.CHILD;
        if (age >= 60) return Classification.SENIOR;
        return Classification.ADULT;
    }

    public Price facilityPrice(BigDecimal basePrice, LocalDate dateOfBirth, LocalDate appointmentDate) {
        Objects.requireNonNull(basePrice, "basePrice");
        if (basePrice.signum() < 0) throw new IllegalArgumentException("Base price cannot be negative.");
        Classification classification = classify(dateOfBirth, appointmentDate);
        BigDecimal rate = switch (classification) {
            case CHILD -> CHILD_RATE;
            case SENIOR -> SENIOR_RATE;
            case ADULT -> ZERO_RATE;
        };
        BigDecimal base = basePrice.setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = base.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        return new Price(base, discount, base.subtract(discount).setScale(2, RoundingMode.HALF_UP), classification);
    }
}
