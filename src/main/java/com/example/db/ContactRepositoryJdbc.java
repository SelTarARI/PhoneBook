package com.example.db;

import com.example.model.Contact;
import com.example.util.PhoneUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContactRepositoryJdbc {

    public List<Contact> findAll() {
        String sql = """
                SELECT id, name, street, city, country, phone, email, version
                FROM contacts
                ORDER BY name
                """;
        return queryMany(sql, ps -> {});
    }

    public List<Contact> search(String q) {
        if (q == null || q.trim().isEmpty()) return findAll();
        String like = "%" + q.trim() + "%";

        String sql = """
                SELECT id, name, street, city, country, phone, email, version
                FROM contacts
                WHERE name LIKE ?
                   OR phone LIKE ?
                   OR email LIKE ?
                   OR city LIKE ?
                   OR country LIKE ?
                ORDER BY name
                """;

        return queryMany(sql, ps -> {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
        });
    }

    public Contact insert(Contact c) {
        sanitizeAndValidate(c);

        String sql = """
                INSERT INTO contacts (name, street, city, country, phone, email, version)
                VALUES (?, ?, ?, ?, ?, ?, 0)
                """;

        try (Connection conn = DriverManager.getConnection(DbConfig.URL, DbConfig.USER, DbConfig.PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            fillContactParams(ps, c);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) c.setId(keys.getLong(1));
            }
            c.setVersion(0);
            return c;

        } catch (SQLException ex) {
            if ("23000".equals(ex.getSQLState())) { // unique violation etc.
                throw new IllegalArgumentException("Phone number must be unique.");
            }
            throw new RuntimeException("DB insert failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Optimistic locking update (multi-user warning):
     * Updates only if (id, version) match and increments version.
     */
    public Contact update(Contact c, int expectedVersion) {
        sanitizeAndValidate(c);
        if (c.getId() == null) throw new IllegalArgumentException("Missing contact id.");

        String sql = """
                UPDATE contacts
                SET name=?,
                    street=?,
                    city=?,
                    country=?,
                    phone=?,
                    email=?,
                    version = version + 1
                WHERE id=? AND version=?
                """;

        try (Connection conn = DriverManager.getConnection(DbConfig.URL, DbConfig.USER, DbConfig.PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            fillContactParams(ps, c);
            ps.setLong(7, c.getId());
            ps.setInt(8, expectedVersion);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new IllegalStateException("This contact was updated by another user. Please refresh.");
            }

            c.setVersion(expectedVersion + 1);
            return c;

        } catch (SQLException ex) {
            if ("23000".equals(ex.getSQLState())) {
                throw new IllegalArgumentException("Phone number must be unique.");
            }
            throw new RuntimeException("DB update failed: " + ex.getMessage(), ex);
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM contacts WHERE id=?";

        try (Connection conn = DriverManager.getConnection(DbConfig.URL, DbConfig.USER, DbConfig.PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("DB delete failed: " + ex.getMessage(), ex);
        }
    }

    // ---------- helpers ----------

    private interface StatementFiller {
        void fill(PreparedStatement ps) throws SQLException;
    }

    private List<Contact> queryMany(String sql, StatementFiller filler) {
        List<Contact> out = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DbConfig.URL, DbConfig.USER, DbConfig.PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            filler.fill(ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Contact c = new Contact();
                    c.setId(rs.getLong("id"));
                    c.setName(rs.getString("name"));
                    c.setStreet(rs.getString("street"));
                    c.setCity(rs.getString("city"));
                    c.setCountry(rs.getString("country"));
                    c.setPhone(rs.getString("phone"));
                    c.setEmail(rs.getString("email"));
                    c.setVersion(rs.getInt("version"));
                    out.add(c);
                }
            }
            return out;

        } catch (SQLException ex) {
            throw new RuntimeException("DB query failed: " + ex.getMessage(), ex);
        }
    }

    // Sets parameters 1..6 for name/street/city/country/phone/email
    private void fillContactParams(PreparedStatement ps, Contact c) throws SQLException {
        ps.setString(1, c.getName());              // already trimmed + required
        ps.setString(2, c.getStreet());            // already trimmed or null
        ps.setString(3, c.getCity());              // already trimmed or null
        ps.setString(4, c.getCountry());           // already trimmed or null
        ps.setString(5, c.getPhone());             // already normalized + validated
        ps.setString(6, c.getEmail());             // already trimmed or null
    }

    private void sanitizeAndValidate(Contact c) {
        if (c == null) throw new IllegalArgumentException("Contact is required.");

        // whitespace cleanup
        c.setName(PhoneUtil.trimToNull(c.getName()));
        c.setStreet(PhoneUtil.trimToNull(c.getStreet()));
        c.setCity(PhoneUtil.trimToNull(c.getCity()));
        c.setCountry(PhoneUtil.trimToNull(c.getCountry()));
        c.setEmail(PhoneUtil.trimToNull(c.getEmail()));

        if (c.getName() == null) throw new IllegalArgumentException("Name is required.");

        // normalize + validate phone
        c.setPhone(PhoneUtil.normalizeAndValidateOrThrow(c.getPhone()));
    }
}
