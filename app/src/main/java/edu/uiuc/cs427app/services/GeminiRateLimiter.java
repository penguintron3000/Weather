package edu.uiuc.cs427app.services;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Shared rate limiter for all Gemini API requests using the same API key.
 * Enforces a minimum interval between requests to prevent quota exhaustion.
 * 
 * Current setting: 10 requests per minute (1 request every 6 seconds).
 * 
 * To adjust: Change REQUESTS_PER_MINUTE and MIN_REQUEST_INTERVAL_MS will be calculated automatically.
 */
public final class GeminiRateLimiter {
    private static final String TAG = "GeminiRateLimiter";

    private static final int REQUESTS_PER_MINUTE = 30;
    private static final long MIN_REQUEST_INTERVAL_MS = TimeUnit.SECONDS.toMillis(60) / REQUESTS_PER_MINUTE;
    
    private static volatile long lastRequestTimeMs = 0L;
    private static final Object rateLimitLock = new Object();

    /**
     * Hidden constructor - this is a utility class.
     */
    private GeminiRateLimiter() {
    }

    /**
     * Enforces rate limiting: ensures minimum interval between requests to avoid quota exhaustion.
     * This is shared across ALL Gemini services using the same API key (i.e theme generation and weather insights).
     * 
     * Current limit: 10 requests per minute (1 every 6 seconds)
     * 
     * @throws IOException if rate limit is violated (request made too soon)
     */
    public static void enforceRateLimit() throws IOException {
        synchronized (rateLimitLock) {
            long now = System.currentTimeMillis();
            long timeSinceLastRequest = now - lastRequestTimeMs;
            
            if (lastRequestTimeMs > 0 && timeSinceLastRequest < MIN_REQUEST_INTERVAL_MS) {
                long waitTime = MIN_REQUEST_INTERVAL_MS - timeSinceLastRequest;
                long waitSeconds = (waitTime + 999) / 1000; // Round up to nearest second
                Log.w(TAG, String.format("Rate limit: %d ms since last request. Need to wait %d more seconds (limit: %d requests/min).", 
                    timeSinceLastRequest, waitSeconds, REQUESTS_PER_MINUTE));
                throw new IOException(String.format("Rate limit: Please wait %d more seconds before making another request (limit: %d requests/min).", 
                    waitSeconds, REQUESTS_PER_MINUTE));
            }
            
            lastRequestTimeMs = now;
            Log.d(TAG, String.format("Rate limit check passed. Proceeding with API request (limit: %d requests/min, %d ms between requests).", 
                REQUESTS_PER_MINUTE, MIN_REQUEST_INTERVAL_MS));
        }
    }
}

