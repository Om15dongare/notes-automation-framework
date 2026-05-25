package com.notesapp.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * LogUtils — shared logger factory and step boundary logging.
 * Consistent format: === STEP: ... === for easy log scanning.
 */
public class LogUtils {

    private LogUtils() {}

    public static Logger getLogger(Class<?> clazz) {
        return LogManager.getLogger(clazz);
    }

    public static void step(Logger log, String stepDescription) {
        log.info("===== STEP: {} =====", stepDescription);
    }

    public static void pass(Logger log, String testName) {
        log.info("✓ PASS — {}", testName);
    }

    public static void fail(Logger log, String testName, String reason) {
        log.error("✗ FAIL — {} | Reason: {}", testName, reason);
    }
}
