SHOW tables;
describe contacts;

INSERT INTO contacts (name, street, city, country, phone, email)
VALUES ('Test Person', 'Test St', 'Istanbul', 'Turkey', '+90-555-0000', 'test@example.com');

SELECT * FROM contacts;
