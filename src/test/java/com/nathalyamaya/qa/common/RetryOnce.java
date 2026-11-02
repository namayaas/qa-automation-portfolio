package com.nathalyamaya.qa.common;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.logging.Logger;

/**
 * Retries a failed test once (configurable with -Dretries=N; 0 disables retries).
 *
 * These suites hit shared public practice sites, so an occasional network blip or a
 * 503 is not a product defect. One retry absorbs that noise; anything that fails twice
 * is reported as a real failure. Every retry is logged as "RETRY ..." so flakiness stays
 * visible instead of being hidden.
 */
public class RetryOnce implements IRetryAnalyzer {

    private static final Logger LOG = Logger.getLogger(RetryOnce.class.getName());
    private final int maxRetries = Config.retries();
    private int attempts = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (attempts < maxRetries) {
            attempts++;
            LOG.warning(() -> "RETRY " + result.getMethod().getQualifiedName()
                    + " after: " + result.getThrowable());
            return true;
        }
        return false;
    }
}
