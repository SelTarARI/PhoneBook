# Phone Book Application (Vaadin + JDBC)

## Overview

This project implements a **Phone Book application** using **Vaadin Flow** for the user interface and **plain JDBC** for persistence.  
The application was developed according to an assignment that required building a CRUD system step by step, starting with in-memory storage and later migrating to a MySQL database, while handling data integrity and multi-user access correctly.

The final solution provides a complete, working phone book system with careful attention to efficiency, correctness, and clarity of design.

---

## Implemented Functionality

### Contact Data Model

Each contact in the system contains the following information:
- Name  
- Street  
- City  
- Country  
- Phone number  
- Email address  

All fields are handled using Unicode, allowing international characters (such as Turkish characters) without any special configuration.

---

### User Interface

The application provides a web-based user interface built with **Vaadin Flow**:

- The opening screen displays a **grid summary** of contacts.
- Summary columns include:
  - Name
  - Email
  - Phone number
- Users can:
  - Add new contacts
  - Edit existing contacts by clicking a row
  - Delete contacts
  - Search contacts using a search field

CRUD operations are implemented explicitly using the Vaadin `Crud<Contact>` component, without relying on backend frameworks such as Spring.

---

### Step 1: In-Memory Storage

As the first development step, contact information was stored **entirely in memory**.

- Contacts are stored in a `HashMap<String, Contact>`, where the **key is the phone number**.
- This design allows direct access to contacts without scanning a list.

**Uniqueness handling:**
- Phone number uniqueness is enforced by checking key existence in the map.
- This check runs in **O(1) average time**, which is more efficient than the required O(n) upper bound.

This step demonstrates that the core CRUD logic works independently of any database.

---

### Step 2: MySQL Database Storage

After validating the in-memory implementation, persistence was added using **MySQL** and **plain JDBC**.

- A database table stores all contact fields.
- The phone number column is defined with a **UNIQUE constraint**.
- CRUD operations (insert, update, delete, search) are implemented manually using SQL.
- No ORM or framework-based persistence is used.

The database schema mirrors the in-memory data model, ensuring consistency across storage layers.

---

### Phone Number Handling and Normalization

Phone numbers are stored as strings to support international formats.

Before storage, phone numbers are normalized:
- Whitespace and separators are removed
- Numbers starting with `00` are converted to `+`
- Only digits and an optional leading `+` are stored

This guarantees:
- Consistent storage format
- Reliable uniqueness checks
- Support for international phone numbers

---

### Multi-User Support

The application supports concurrent access by multiple users.

- Each contact includes a `version` field.
- Updates use **optimistic locking**:
  - An update succeeds only if the record version matches.
  - If another user has modified the contact, the update fails.
- In case of conflict, the user is shown a warning message.

This approach ensures data integrity without locking the database.

---

### Technical Constraints and Design Choices

- No Spring or similar frameworks are used.
- All CRUD logic is implemented explicitly.
- In-memory and database storage are clearly separated.
- Efficient data structures are used where appropriate.
- Unicode support is handled naturally through modern Java, Vaadin, and MySQL (`utf8mb4`).

---

## Technologies Used

- Java 21  
- Vaadin Flow  
- Maven  
- MySQL  
- JDBC  
- Jetty (development server)

---

## Running the Application

1. Ensure Java 21 and MySQL are installed.
2. Create the database using the provided SQL script.
3. Configure database credentials.
4. Run the application:

```bash
mvn jetty:run
```

5. Open a browser at:

```
http://localhost:8080
```

---

## Conclusion

The project delivers a complete phone book application that:
- Implements full CRUD functionality
- Demonstrates in-memory storage and database-backed storage
- Enforces phone number uniqueness efficiently
- Handles multi-user update conflicts correctly
- Respects all technical constraints of the assignment

The final solution is clean, efficient, and aligned with real-world best practices while remaining within the intended scope of the assignment.
