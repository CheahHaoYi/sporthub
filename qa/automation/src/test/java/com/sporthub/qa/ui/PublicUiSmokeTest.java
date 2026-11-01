package com.sporthub.qa.ui;

import com.sporthub.qa.base.DriverFactory;
import com.sporthub.qa.base.TestConfig;
import com.sporthub.qa.pages.LoginPage;
import com.sporthub.qa.pages.RegisterPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** Browser smoke tests for frontend behavior that does not depend on missing APIs. */
public class PublicUiSmokeTest {
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeMethod
    public void startBrowser() {
        driver = DriverFactory.createChrome();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterMethod(alwaysRun = true)
    public void closeBrowser() {
        if (driver != null) driver.quit();
    }

    @Test(groups = {"ui", "smoke"})
    public void loginPageExposesRequiredEmailAndPasswordFields() {
        LoginPage page = new LoginPage(driver);
        page.open(TestConfig.uiBaseUrl());

        wait.until(ExpectedConditions.titleContains("SportHub"));
        assertEquals(page.email().getAttribute("type"), "email");
        assertTrue(page.email().getAttribute("required") != null);
        assertTrue(page.password().getAttribute("required") != null);
        assertTrue(page.submit().isDisplayed());
    }

    @Test(groups = {"ui", "sanity"})
    public void registrationRejectsAChildAccountBeforeSendingAnApiRequest() {
        RegisterPage page = new RegisterPage(driver);
        page.open(TestConfig.uiBaseUrl());
        page.fullName().sendKeys("Young Player");
        page.email().sendKeys("young-player@example.test");
        page.password().sendKeys("Password123!");

        // Set the date through the native input setter so React observes the controlled-input change.
        ((JavascriptExecutor) driver).executeScript(
                "const input = arguments[0];" +
                        "const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;" +
                        "setter.call(input, arguments[1]);" +
                        "input.dispatchEvent(new Event('input', { bubbles: true }));" +
                        "input.dispatchEvent(new Event('change', { bubbles: true }));",
                page.dateOfBirth(), "2015-01-01");
        page.submit().click();

        String alert = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[role='alert']"))).getText();
        assertEquals(alert, "You must be 18 or older to create an account.");
    }

    @Test(groups = {"ui", "sanity"})
    public void customerRoleIsRedirectedAwayFromTheAdminRoute() {
        driver.get(TestConfig.uiBaseUrl());
        ((JavascriptExecutor) driver).executeScript(
                "localStorage.setItem('sporthub_user', arguments[0]);",
                "{\"id\":1,\"email\":\"customer@example.test\",\"role\":\"CUSTOMER\"}");
        driver.get(TestConfig.uiBaseUrl() + "/admin");

        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getCurrentUrl().endsWith("/login"));
    }
}
