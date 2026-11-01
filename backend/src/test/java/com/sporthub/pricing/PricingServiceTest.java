package com.sporthub.pricing;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.testng.Assert.assertEquals;
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
    }

    @Test(groups = "unit")
    public void rejectsAppointmentBeforeBirth() {
        expectThrows(IllegalArgumentException.class, () -> pricing.classify(LocalDate.parse("2020-01-01"), LocalDate.parse("2019-12-31")));
    }
}
