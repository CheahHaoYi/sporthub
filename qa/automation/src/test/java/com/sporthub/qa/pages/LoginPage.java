package com.sporthub.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** Page Object: keeps login-page selectors and actions in one place. */
public class LoginPage {
    private final WebDriver driver;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/login");
    }

    public WebElement email() {
        return driver.findElement(By.cssSelector("[data-testid='login-email']"));
    }

    public WebElement password() {
        return driver.findElement(By.cssSelector("[data-testid='login-password']"));
    }

    public WebElement submit() {
        return driver.findElement(By.cssSelector("[data-testid='login-submit']"));
    }
}
