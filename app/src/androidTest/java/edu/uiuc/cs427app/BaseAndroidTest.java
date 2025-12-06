package edu.uiuc.cs427app;

/**
 * Contains shared helper methods and constants for
 * thread sleeping and synchonization for Android API tests.
 */
public abstract class BaseAndroidTest {

    public static final int uiThreadWaitMs = 500;

    /**
     * Sends the current thread to sleep for the specified duration.
     * @param timeMS Duration in milliseconds to sleep
     */
    protected void sleep(long timeMs) {
        try {
            Thread.sleep(timeMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Sends the current thread to sleep for a default duration.
     */
    protected void sleep() {
        sleep(uiThreadWaitMs);
    }
}