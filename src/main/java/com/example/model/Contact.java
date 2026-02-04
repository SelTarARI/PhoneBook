package com.example.model;

public class Contact {

    private Long id;        // DB primary key
    private int version;    // for optimistic locking

    private String name;
    private String street;
    private String city;
    private String country;
    private String phone;
    private String email;

    public Contact() {}

    public Contact(String name, String street, String city,
                   String country, String phone, String email) {
        this.name = name;
        this.street = street;
        this.city = city;
        this.country = country;
        this.phone = phone;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
