package com.example.db;

public class DbConfig {
    public static final String URL =
            env("PHONEBOOK_DB_URL",
                    "jdbc:mysql://localhost:3306/phonebook?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");

    public static final String USER =
            env("PHONEBOOK_DB_USER", "root");

    public static final String PASSWORD =
            env("PHONEBOOK_DB_PASSWORD", "Root@123");

    private static String env(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? fallback : v;
    }
}
