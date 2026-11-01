package com.sporthub.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** Page Object for registration interactions used in browser tests. */
public class RegisterPage {
    private final WebDriver driver;

    public RegisterPage(WebDriver driver) {
        this.driver = driver;
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/register");
    }

    public WebElement fullName() {
        return driver.findElement(By.xpath("//label[contains(., 'Full name')]/input"));
    }

    public WebElement email() {
        return driver.findElement(By.cssSelector("[data-testid='register-email']"));
    }

    public WebElement password() {
        return driver.findElement(By.cssSelector("[data-testid='register-password']"));
    }

    public WebElement dateOfBirth() {
        return driver.findElement(By.cssSelector("input[type='date']"));
    }

    public WebElement submit() {
        return driver.findElement(By.cssSelector("[data-testid='register-submit']"));
    }
}
