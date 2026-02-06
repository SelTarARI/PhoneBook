# Phone Book Application (Vaadin + JDBC)

## Project Description

This project is a **Phone Book application** implemented using **Vaadin Flow** for the user interface and **plain JDBC** for persistence.  
The goal of the assignment was to design and implement a CRUD application step by step, starting with in-memory storage and later migrating to a relational database, while respecting specific constraints.

The application allows users to:
- add contacts
- update contacts
- delete contacts
- search contacts
- handle concurrent (multi-user) updates safely

---

## Assignment Requirements (What Was Wanted)

### Functional Requirements
1. Store contact information including:
   - Name
   - Street
   - City
   - Country
   - Phone number
   - Email address

2. Provide a user interface that allows:
   - Adding a person
   - Deleting a person
   - Searching for a person
   - Updating a person’s information

3. The opening screen must show:
   - A **grid** with a **summary** of contacts  
     (name, email address, phone number)

4. Storage must be implemented **in steps**:
   - **Step 1:** Store contacts **in memory**
   - **Step 2:** Store contacts in a **MySQL database**

5. The phone number must be:
   - **Unique**
   - Uniqueness check should be **O(n)** or better

6. The application must support **multiple users**:
   - If one user updates a contact while another user is editing it, the second user must be **warned**

### Technical Constraints
- Spring or similar frameworks **must not be used**
- CRUD operations must be implemented explicitly

---

## Implementation Summary (What We Provided)

### 1. User Interface (Vaadin)
- Implemented using **Vaadin Flow**
- Uses the **Vaadin `Crud<Contact>` component**
- The opening screen shows a **grid summary**
- Clicking a row opens an editor dialog
- Search functionality is provided

---

### 2. Step 1: In-Memory Storage
- Implemented using an in-memory service (`ContactService`)
- Contacts stored in a `HashMap<String, Contact>` keyed by phone number
- Uniqueness is checked using constant-time map lookup

**Complexity:**  
- Uniqueness check: **O(1)** average case (better than required O(n))

---

### 3. Step 2: MySQL Database Storage
- Implemented using **plain JDBC**
- MySQL table stores all contact fields
- Phone number is enforced as `UNIQUE`
- Database schema matches the domain model

---

### 4. Phone Number Handling & Uniqueness
- Phone numbers stored as strings
- Normalized before storage (separators removed, `00` → `+`)
- Enforced unique both in memory and database

---

### 5. Multi-User Support
- Implemented using **optimistic locking**
- Each contact has a `version` field
- Conflicting updates are detected and warned to the user

---

### 6. Unicode Support
- Supports international characters (e.g. Turkish characters)
- Uses UTF-8 / Unicode end-to-end

---

## Technologies Used
- Java 21
- Vaadin Flow
- Maven
- MySQL
- JDBC
- Jetty

---

## How to Run

```bash
mvn jetty:run
```

Open: http://localhost:8080

---

## Multi-User Test Procedure
1. Open app in two browsers
2. Edit the same contact
3. Save in one browser
4. Save in the other → warning is shown

---

## Conclusion

All assignment requirements have been fully satisfied, including:
- CRUD functionality
- In-memory and database storage
- Efficient uniqueness checking
- Multi-user conflict handling
- No forbidden frameworks
