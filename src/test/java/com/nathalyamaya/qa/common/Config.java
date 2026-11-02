package com.nathalyamaya.qa.common;

/**
 * Central place for environment settings. Every value can be overridden with a
 * JVM system property (-Dkey=value) or an environment variable (KEY_WITH_UNDERSCORES),
 * so the same tests run locally and in CI without code changes.
 */
public final class Config {

    private Config() {
    }

    public static String uiBaseUrl() {
        return get("ui.baseUrl", "https://www.saucedemo.com");
    }

    public static String apiBaseUrl() {
        return get("api.baseUrl", "https://restful-booker.herokuapp.com");
    }

    public static String soapBaseUrl() {
        return get("soap.baseUrl", "http://www.dneonline.com");
    }

    public static String appiumUrl() {
        return get("appium.url", "http://127.0.0.1:4723");
    }

    /** Headless by default so tests run in CI; pass -Dheadless=false to watch the browser. */
    public static boolean headless() {
        return Boolean.parseBoolean(get("headless", "true"));
    }

    public static int waitSeconds() {
        return Integer.parseInt(get("wait.seconds", "10"));
    }

    /** Retries per failed test. Set -Dretries=0 when checking for flakiness. */
    public static int retries() {
        return Integer.parseInt(get("retries", "1"));
    }

    /** Practice-site credentials are public and documented by the site owners; not secrets. */
    public static String apiAdminUser() {
        return get("api.user", "admin");
    }

    public static String apiAdminPassword() {
        return get("api.password", "password123");
    }

    static String get(String key, String defaultValue) {
        String fromProperty = System.getProperty(key);
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }
        String envKey = key.replace('.', '_').toUpperCase();
        String fromEnv = System.getenv(envKey);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return defaultValue;
    }
}
