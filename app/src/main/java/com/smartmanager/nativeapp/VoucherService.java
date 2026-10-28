package com.smartmanager.nativeapp;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

/** Pure voucher allocation helpers; the router remains the source of truth. */
public final class VoucherService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private VoucherService() { }

    public static int nextSerial(List<Map<String,String>> users, String prefix) {
        long max = 0;
        for (Map<String,String> row : users) {
            String name = row.get("name");
            if (name == null || !name.startsWith(prefix)) continue;
            String suffix = name.substring(prefix.length());
            if (!suffix.matches("[0-9]+")) continue;
            try { max = Math.max(max, Long.parseLong(suffix)); }
            catch (NumberFormatException ignored) { }
        }
        return (int)Math.min(Integer.MAX_VALUE, max + 1);
    }

    public static String password() {
        StringBuilder value = new StringBuilder(6);
        for (int i = 0; i < 6; i++) value.append((char)('0' + RANDOM.nextInt(10)));
        return value.toString();
    }
}
