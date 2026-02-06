package com.example.db;

public class DbConfig {
    public static final String URL =
            env("PHONEBOOK_DB_URL",
                    "");

    public static final String USER =
            env("PHONEBOOK_DB_USER", "");

    public static final String PASSWORD =
            env("PHONEBOOK_DB_PASSWORD", "");

    private static String env(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? fallback : v;
    }
}
