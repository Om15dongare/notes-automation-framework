package com.notesapp.utils;

import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;

/**
 * AllureUtils — helper to attach screenshots, JSON, and log text to Allure reports.
 */
public class AllureUtils {

    private static final Logger log = LogManager.getLogger(AllureUtils.class);

    private AllureUtils() {}

    public static void attachScreenshot(String name, byte[] screenshot) {
        if (screenshot.length == 0) {
            log.warn("Screenshot byte array is empty — skipping attachment");
            return;
        }
        Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshot), ".png");
        log.info("Screenshot attached to Allure: {}", name);
    }

    public static void attachJson(String name, String json) {
        Allure.addAttachment(name, "application/json",
                new ByteArrayInputStream(json.getBytes()), ".json");
    }

    public static void attachText(String name, String text) {
        Allure.addAttachment(name, "text/plain",
                new ByteArrayInputStream(text.getBytes()), ".txt");
    }

    public static void step(String stepName) {
        Allure.step(stepName);
    }
}
