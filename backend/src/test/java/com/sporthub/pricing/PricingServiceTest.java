package com.sporthub.pricing;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.expectThrows;

public class PricingServiceTest {
    private final PricingService pricing = new PricingService();

    @DataProvider(name = "ageBoundaries")
    public Object[][] ageBoundaries() {
        return new Object[][] {
                {"2008-11-02", "2026-11-01", PricingService.Classification.CHILD},
                {"2008-11-01", "2026-11-01", PricingService.Classification.ADULT},
                {"1966-11-02", "2026-11-01", PricingService.Classification.ADULT},
                {"1966-11-01", "2026-11-01", PricingService.Classification.SENIOR}
        };
    }

    @Test(dataProvider = "ageBoundaries", groups = "unit")
    public void classifiesOnAppointmentDate(String dob, String date, PricingService.Classification expected) {
        assertEquals(pricing.classify(LocalDate.parse(dob), LocalDate.parse(date)), expected);
    }

    @Test(groups = "unit")
    public void appliesCategoryDiscountAndCurrencyRounding() {
        PricingService.Price price = pricing.facilityPrice(new BigDecimal("19.99"),
                LocalDate.parse("2010-01-01"), LocalDate.parse("2026-01-01"));
        assertEquals(price.discount(), new BigDecimal("4.00"));
        assertEquals(price.payable(), new BigDecimal("15.99"));
        assertEquals(price.classification(), PricingService.Classification.CHILD);
    }

    @Test(groups = "unit")
    public void adultPaysBasePriceWithoutDemographicDiscount() {
        PricingService.Price price = pricing.facilityPrice(new BigDecimal("20"),
                LocalDate.parse("2000-01-01"), LocalDate.parse("2026-01-01"));

        assertEquals(price.base(), new BigDecimal("20.00"));
        assertEquals(price.discount(), new BigDecimal("0.00"));
        assertEquals(price.payable(), new BigDecimal("20.00"));
        assertEquals(price.classification(), PricingService.Classification.ADULT);
    }

    @Test(groups = "unit")
    public void appliesSeniorDiscount() {
        PricingService.Price price = pricing.facilityPrice(new BigDecimal("100.00"),
                LocalDate.parse("1966-01-01"), LocalDate.parse("2026-01-01"));

        assertEquals(price.discount(), new BigDecimal("15.00"));
        assertEquals(price.payable(), new BigDecimal("85.00"));
        assertEquals(price.classification(), PricingService.Classification.SENIOR);
    }

    @Test(groups = "unit")
    public void acceptsZeroPriceAndRoundsBaseToCurrencyScale() {
        PricingService.Price zero = pricing.facilityPrice(BigDecimal.ZERO,
                LocalDate.parse("2000-01-01"), LocalDate.parse("2026-01-01"));
        PricingService.Price rounded = pricing.facilityPrice(new BigDecimal("19.995"),
                LocalDate.parse("2000-01-01"), LocalDate.parse("2026-01-01"));

        assertEquals(zero.payable(), new BigDecimal("0.00"));
        assertEquals(rounded.base(), new BigDecimal("20.00"));
        assertEquals(rounded.payable(), new BigDecimal("20.00"));
    }

    @Test(groups = "unit")
    public void rejectsAppointmentBeforeBirth() {
        expectThrows(IllegalArgumentException.class, () -> pricing.classify(LocalDate.parse("2020-01-01"), LocalDate.parse("2019-12-31")));
    }

    @Test(groups = "unit")
    public void rejectsNegativeBasePrice() {
        expectThrows(IllegalArgumentException.class, () -> pricing.facilityPrice(new BigDecimal("-0.01"),
                LocalDate.parse("2000-01-01"), LocalDate.parse("2026-01-01")));
    }

    @Test(groups = "unit")
    public void rejectsMissingDatesAndPrice() {
        assertNotNull(expectThrows(NullPointerException.class, () -> pricing.classify(null, LocalDate.now())));
        expectThrows(NullPointerException.class, () -> pricing.classify(LocalDate.now(), null));
        expectThrows(NullPointerException.class, () -> pricing.facilityPrice(null,
                LocalDate.parse("2000-01-01"), LocalDate.parse("2026-01-01")));
    }
}
