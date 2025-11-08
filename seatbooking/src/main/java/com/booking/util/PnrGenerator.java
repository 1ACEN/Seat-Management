package com.booking.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/**
 * Simple PNR generator used by BookingService when creating tickets.
 * Uses current timestamp and random bytes to create a short identifier.
 */
public final class PnrGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    private PnrGenerator() { }

    public static String generate() {
        long ts = Instant.now().toEpochMilli();
        byte[] rand = new byte[6];
        RANDOM.nextBytes(rand);
        String r = Base64.getUrlEncoder().withoutPadding().encodeToString(rand);
        // Keep it compact: TS hex (last 6 chars) + random
        String tsHex = Long.toHexString(ts);
        if (tsHex.length() > 6) tsHex = tsHex.substring(tsHex.length() - 6);
        return (tsHex + "-" + r).toUpperCase();
    }
}
