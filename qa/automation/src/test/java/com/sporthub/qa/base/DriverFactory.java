package com.sporthub.qa.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.WebDriver;

public final class DriverFactory {
    private DriverFactory() { }
    public static WebDriver createChrome() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        if (TestConfig.headless()) options.addArguments("--headless=new");
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--window-size=1440,1000");
        return new ChromeDriver(options);
    }
}
