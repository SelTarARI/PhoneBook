package com.example.service;

import com.example.model.Contact;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ContactService {

    // phone -> contact (phone is unique)
    private final Map<String, Contact> byPhone = new ConcurrentHashMap<>();

    public List<Contact> findAll() {
        return byPhone.values().stream()
                .sorted(Comparator.comparing(Contact::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public List<Contact> search(String query) {
        if (query == null || query.trim().isEmpty()) return findAll();
        String q = query.trim().toLowerCase();

        return byPhone.values().stream()
                .filter(c -> contains(c.getName(), q)
                        || contains(c.getEmail(), q)
                        || contains(c.getPhone(), q)
                        || contains(c.getCity(), q)
                        || contains(c.getCountry(), q))
                .sorted(Comparator.comparing(Contact::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public void add(Contact contact) {
        validate(contact);
        String phone = normalizePhone(contact.getPhone());

        // uniqueness check
        if (byPhone.containsKey(phone)) {
            throw new IllegalArgumentException("Phone number must be unique.");
        }
        contact.setPhone(phone);
        byPhone.put(phone, contact);
    }

    public void delete(Contact contact) {
        if (contact == null || contact.getPhone() == null) return;
        byPhone.remove(normalizePhone(contact.getPhone()));
    }

    public void update(String originalPhone, Contact updated) {
        validate(updated);

        String newPhone = normalizePhone(updated.getPhone());
        String oldPhone = normalizePhone(originalPhone);

        if (!byPhone.containsKey(oldPhone)) {
            throw new IllegalStateException("Contact no longer exists.");
        }

        // If phone changed, ensure uniqueness
        if (!oldPhone.equals(newPhone) && byPhone.containsKey(newPhone)) {
            throw new IllegalArgumentException("Phone number must be unique.");
        }

        updated.setPhone(newPhone);
        byPhone.remove(oldPhone);
        byPhone.put(newPhone, updated);
    }


    private void validate(Contact c) {
        if (c == null) throw new IllegalArgumentException("Contact is required.");
        if (c.getName() == null || c.getName().trim().isEmpty())
            throw new IllegalArgumentException("Name is required.");
        if (c.getPhone() == null || c.getPhone().trim().isEmpty())
            throw new IllegalArgumentException("Phone is required.");
    }

    private String normalizePhone(String phone) {
        return phone.trim();
    }

    private boolean contains(String field, String q) {
        return field != null && field.toLowerCase().contains(q);
    }
}
