package com.notesapp.utils;

import java.util.UUID;

/**
 * DataUtils — test data generation helpers.
 * Generates unique emails, titles, and random strings for test isolation.
 */
public class DataUtils {

    private DataUtils() {}

    public static String uniqueEmail() {
        return "testuser_" + System.currentTimeMillis() + "@qatest.com";
    }

    public static String uniqueTitle(String prefix) {
        return prefix + "_" + System.currentTimeMillis();
    }

    public static String randomString(int length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, Math.min(length, 32));
    }

    public static String[] categories() {
        return new String[]{"Home", "Work", "Personal"};
    }

    public static String randomCategory() {
        String[] cats = categories();
        return cats[(int) (Math.random() * cats.length)];
    }
}
