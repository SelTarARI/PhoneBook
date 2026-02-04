package com.example.db;

import com.example.model.Contact;

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
        validate(c);
        String sql = """
                INSERT INTO contacts (name, street, city, country, phone, email, version)
                VALUES (?, ?, ?, ?, ?, ?, 0)
                """;

        try (Connection conn = DriverManager.getConnection(DbConfig.URL, DbConfig.USER, DbConfig.PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            fillContactParams(ps, c);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getLong(1));
                }
            }
            c.setVersion(0);
            return c;

        } catch (SQLIntegrityConstraintViolationException dup) {
            // Unique phone violation
            throw new IllegalArgumentException("Phone number must be unique.");
        } catch (SQLException ex) {
            // MySQL also sometimes throws generic SQLException for unique constraint, handle by SQLState
            if ("23000".equals(ex.getSQLState())) {
                throw new IllegalArgumentException("Phone number must be unique.");
            }
            throw new RuntimeException("DB insert failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Optimistic locking update:
     * - Only updates if (id, version) match.
     * - Increments version.
     * @return updated contact with version incremented
     */
    public Contact update(Contact c, int expectedVersion) {
        validate(c);
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

            // 1..6 = fields
            fillContactParams(ps, c);
            // 7..8 = where
            ps.setLong(7, c.getId());
            ps.setInt(8, expectedVersion);

            int updatedRows = ps.executeUpdate();
            if (updatedRows == 0) {
                // Someone else updated or deleted it
                throw new IllegalStateException("This contact was updated by another user. Please refresh.");
            }

            c.setVersion(expectedVersion + 1);
            return c;

        } catch (SQLIntegrityConstraintViolationException dup) {
            throw new IllegalArgumentException("Phone number must be unique.");
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
        ps.setString(1, nullIfBlank(c.getName()));
        ps.setString(2, nullIfBlank(c.getStreet()));
        ps.setString(3, nullIfBlank(c.getCity()));
        ps.setString(4, nullIfBlank(c.getCountry()));
        ps.setString(5, nullIfBlank(c.getPhone()));
        ps.setString(6, nullIfBlank(c.getEmail()));
    }

    private void validate(Contact c) {
        if (c == null) throw new IllegalArgumentException("Contact is required.");
        if (c.getName() == null || c.getName().trim().isEmpty())
            throw new IllegalArgumentException("Name is required.");
        if (c.getPhone() == null || c.getPhone().trim().isEmpty())
            throw new IllegalArgumentException("Phone is required.");
    }

    private String nullIfBlank(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
