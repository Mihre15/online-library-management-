-- Seed data for demo / Selenium
-- Demo password for every seeded student: Password123

INSERT INTO members (full_name, email, password_hash, status, role, registered_at, outstanding_fines)
VALUES ('Ada Lovelace', 'ada@library.test', '$2y$10$aZv6yiPXjjtMi8fVyDuRbOiYaczZkhfKrj05xH2mxe0anZ2HpFMy.', 'ACTIVE', 'STUDENT', CURRENT_DATE, 0.00);

INSERT INTO members (full_name, email, password_hash, status, role, registered_at, outstanding_fines)
VALUES ('Suspended Student', 'suspended@library.test', '$2y$10$aZv6yiPXjjtMi8fVyDuRbOiYaczZkhfKrj05xH2mxe0anZ2HpFMy.', 'SUSPENDED', 'STUDENT', CURRENT_DATE, 0.00);

INSERT INTO members (full_name, email, password_hash, status, role, registered_at, outstanding_fines)
VALUES ('Fine Holder', 'fines@library.test', '$2y$10$aZv6yiPXjjtMi8fVyDuRbOiYaczZkhfKrj05xH2mxe0anZ2HpFMy.', 'ACTIVE', 'STUDENT', CURRENT_DATE, 10.01);

INSERT INTO members (full_name, email, password_hash, status, role, registered_at, outstanding_fines)
VALUES ('Library Administrator', 'admin@library.test', '$2y$10$aZv6yiPXjjtMi8fVyDuRbOiYaczZkhfKrj05xH2mxe0anZ2HpFMy.', 'ACTIVE', 'ADMIN', CURRENT_DATE, 0.00);

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
VALUES ('Clean Code', 'Robert Martin', '9780132350884', 'Software', 3, 3);

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
VALUES ('Domain-Driven Design', 'Eric Evans', '9780321125217', 'Software', 2, 1);

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
VALUES ('The Pragmatic Programmer', 'Andrew Hunt', '9780135957059', 'Software', 1, 0);

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
VALUES ('Out of Print Atlas', 'Unknown Cartographer', '0000000000000', 'Reference', 0, 0);

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
VALUES ('1984', 'George Orwell', '9780451524935', 'Fiction', 4, 4);

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
VALUES ('Pride and Prejudice', 'Jane Austen', '9780141439518', 'Fiction', 3, 3);

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
VALUES ('Things Fall Apart', 'Chinua Achebe', '9780385474542', 'Fiction', 3, 2);

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
VALUES ('The Odyssey', 'Homer', '9780140268867', 'Classics', 2, 2);

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
VALUES ('The Hobbit', 'J.R.R. Tolkien', '9780547928227', 'Fiction', 2, 2);

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
VALUES ('To Kill a Mockingbird', 'Harper Lee', '9780061120084', 'Fiction', 3, 3);

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
VALUES ('The Design of Everyday Things', 'Don Norman', '9780465050659', 'Design', 2, 2);

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
VALUES ('A Brief History of Time', 'Stephen Hawking', '9780553380163', 'Science', 2, 1);

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
VALUES ('Thinking, Fast and Slow', 'Daniel Kahneman', '9780374533557', 'Science', 2, 2);

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
VALUES ('Designing Data-Intensive Applications', 'Martin Kleppmann', '9781449373320', 'Software', 2, 2);

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
VALUES ('The Republic', 'Plato', '9780140455113', 'Classics', 2, 2);

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
VALUES ('Crime and Punishment', 'Fyodor Dostoevsky', '9780140449136', 'Classics', 2, 2);

-- NOTE: Loans require valid member_id and book_id. These will be inserted after members and books are created.
-- Active loan (Ada / Domain-Driven Design)
INSERT INTO loans (member_id, book_id, borrow_date, due_date, return_date, status, fine_amount)
SELECT m.id, b.id, CURRENT_DATE - 3, CURRENT_DATE + 11, NULL, 'ACTIVE', 0.00
FROM members m, books b
WHERE m.email = 'ada@library.test' AND b.isbn = '9780321125217';

-- Past-due ACTIVE loan (Ada / Pragmatic Programmer)
INSERT INTO loans (member_id, book_id, borrow_date, due_date, return_date, status, fine_amount)
SELECT m.id, b.id, CURRENT_DATE - 20, CURRENT_DATE - 6, NULL, 'ACTIVE', 0.00
FROM members m, books b
WHERE m.email = 'ada@library.test' AND b.isbn = '9780135957059';

-- Returned loan with a fine (Ada / Clean Code)
INSERT INTO loans (member_id, book_id, borrow_date, due_date, return_date, status, fine_amount)
SELECT m.id, b.id, CURRENT_DATE - 30, CURRENT_DATE - 16, CURRENT_DATE - 10, 'RETURNED', 2.50
FROM members m, books b
WHERE m.email = 'ada@library.test' AND b.isbn = '9780132350884';
