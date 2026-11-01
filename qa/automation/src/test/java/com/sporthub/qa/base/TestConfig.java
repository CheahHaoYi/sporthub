package com.sporthub.qa.base;

public final class TestConfig {
    private TestConfig() { }
    public static String apiBaseUrl() { return value("api.baseUrl", "API_BASE_URL", "http://localhost:8080/api/v1"); }
    public static String uiBaseUrl() { return value("ui.baseUrl", "UI_BASE_URL", "http://localhost:5173"); }
    public static boolean headless() { return Boolean.parseBoolean(value("browser.headless", "BROWSER_HEADLESS", "true")); }
    private static String value(String property, String environment, String fallback) {
        String result = System.getProperty(property);
        if (result == null || result.isBlank()) result = System.getenv(environment);
        return result == null || result.isBlank() || result.equals("null") ? fallback : result;
    }
}
