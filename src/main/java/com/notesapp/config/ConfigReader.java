package com.notesapp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigReader — loads config.properties and exposes typed getters.
 * Singleton pattern; properties loaded once at class load time.
 */
public class ConfigReader {

    private static final Properties props = new Properties();

    static {
        try (InputStream is = ConfigReader.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (is == null) {
                throw new RuntimeException("config.properties not found in src/test/resources");
            }
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties: " + e.getMessage(), e);
        }
    }

    private ConfigReader() {}

    public static String get(String key) {
        String value = props.getProperty(key);
        if (value == null) throw new RuntimeException("Missing config key: " + key);
        return value.trim();
    }

    public static String getBaseUrl()           { return get("base.url"); }
    public static String getApiBaseUrl()        { return get("api.base.url"); }
    public static String getUserEmail()         { return get("user.email"); }
    public static String getUserPassword()      { return get("user.password"); }
    public static String getBrowser()           { return get("browser"); }
    public static boolean isHeadless()          { return Boolean.parseBoolean(get("headless")); }
    public static int getImplicitWait()         { return Integer.parseInt(get("implicit.wait")); }
    public static int getExplicitWait()         { return Integer.parseInt(get("explicit.wait")); }
    public static int getPageLoadTimeout()      { return Integer.parseInt(get("page.load.timeout")); }
    public static long getApiResponseTimeMs()   { return Long.parseLong(get("api.response.time.ms")); }
    public static int getApiRetryCount()        { return Integer.parseInt(get("api.retry.count")); }
    public static boolean screenshotOnFailure() { return Boolean.parseBoolean(get("screenshot.on.failure")); }
}
