-- Seed data for demo / Selenium. Idempotent so restarts with ddl-auto=update do not duplicate rows.
-- Demo password for every seeded student: Password123

INSERT INTO members (full_name, email, password_hash, status, role, registered_at, outstanding_fines)
SELECT 'Ada Lovelace', 'ada@library.test', '$2y$10$aZv6yiPXjjtMi8fVyDuRbOiYaczZkhfKrj05xH2mxe0anZ2HpFMy.', 'ACTIVE', 'STUDENT', CURRENT_DATE, 0.00
WHERE NOT EXISTS (SELECT 1 FROM members WHERE email = 'ada@library.test');

INSERT INTO members (full_name, email, password_hash, status, role, registered_at, outstanding_fines)
SELECT 'Suspended Student', 'suspended@library.test', '$2y$10$aZv6yiPXjjtMi8fVyDuRbOiYaczZkhfKrj05xH2mxe0anZ2HpFMy.', 'SUSPENDED', 'STUDENT', CURRENT_DATE, 0.00
WHERE NOT EXISTS (SELECT 1 FROM members WHERE email = 'suspended@library.test');

INSERT INTO members (full_name, email, password_hash, status, role, registered_at, outstanding_fines)
SELECT 'Fine Holder', 'fines@library.test', '$2y$10$aZv6yiPXjjtMi8fVyDuRbOiYaczZkhfKrj05xH2mxe0anZ2HpFMy.', 'ACTIVE', 'STUDENT', CURRENT_DATE, 10.01
WHERE NOT EXISTS (SELECT 1 FROM members WHERE email = 'fines@library.test');

INSERT INTO members (full_name, email, password_hash, status, role, registered_at, outstanding_fines)
SELECT 'Library Administrator', 'admin@library.test', '$2y$10$aZv6yiPXjjtMi8fVyDuRbOiYaczZkhfKrj05xH2mxe0anZ2HpFMy.', 'ACTIVE', 'ADMIN', CURRENT_DATE, 0.00
WHERE NOT EXISTS (SELECT 1 FROM members WHERE email = 'admin@library.test');

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
SELECT 'Clean Code', 'Robert Martin', '9780132350884', 'Software', 3, 3
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9780132350884');

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
SELECT 'Domain-Driven Design', 'Eric Evans', '9780321125217', 'Software', 2, 1
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9780321125217');

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
SELECT 'The Pragmatic Programmer', 'Andrew Hunt', '9780135957059', 'Software', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9780135957059');

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
SELECT 'Out of Print Atlas', 'Unknown Cartographer', '0000000000000', 'Reference', 0, 0
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '0000000000000');

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
SELECT '1984', 'George Orwell', '9780451524935', 'Fiction', 4, 4
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9780451524935');

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
SELECT 'Pride and Prejudice', 'Jane Austen', '9780141439518', 'Fiction', 3, 3
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9780141439518');

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
SELECT 'Things Fall Apart', 'Chinua Achebe', '9780385474542', 'Fiction', 3, 2
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9780385474542');

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
SELECT 'The Odyssey', 'Homer', '9780140268867', 'Classics', 2, 2
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9780140268867');

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
SELECT 'The Hobbit', 'J.R.R. Tolkien', '9780547928227', 'Fiction', 2, 2
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9780547928227');

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
SELECT 'To Kill a Mockingbird', 'Harper Lee', '9780061120084', 'Fiction', 3, 3
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9780061120084');

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
SELECT 'The Design of Everyday Things', 'Don Norman', '9780465050659', 'Design', 2, 2
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9780465050659');

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
SELECT 'A Brief History of Time', 'Stephen Hawking', '9780553380163', 'Science', 2, 1
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9780553380163');

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
SELECT 'Thinking, Fast and Slow', 'Daniel Kahneman', '9780374533557', 'Science', 2, 2
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9780374533557');

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
SELECT 'Designing Data-Intensive Applications', 'Martin Kleppmann', '9781449373320', 'Software', 2, 2
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9781449373320');

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
SELECT 'The Republic', 'Plato', '9780140455113', 'Classics', 2, 2
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9780140455113');

INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
SELECT 'Crime and Punishment', 'Fyodor Dostoevsky', '9780140449136', 'Classics', 2, 2
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9780140449136');

-- Active loan (Ada / Domain-Driven Design)
INSERT INTO loans (member_id, book_id, borrow_date, due_date, return_date, status, fine_amount)
SELECT m.id, b.id, CURRENT_DATE - 3, CURRENT_DATE + 11, NULL, 'ACTIVE', 0.00
FROM members m
JOIN books b ON b.isbn = '9780321125217'
WHERE m.email = 'ada@library.test'
  AND NOT EXISTS (
      SELECT 1 FROM loans l
      WHERE l.member_id = m.id AND l.book_id = b.id AND l.status = 'ACTIVE'
  );

-- Past-due ACTIVE loan so overdue-on-read can flip it (Ada / Pragmatic Programmer)
INSERT INTO loans (member_id, book_id, borrow_date, due_date, return_date, status, fine_amount)
SELECT m.id, b.id, CURRENT_DATE - 20, CURRENT_DATE - 6, NULL, 'ACTIVE', 0.00
FROM members m
JOIN books b ON b.isbn = '9780135957059'
WHERE m.email = 'ada@library.test'
  AND NOT EXISTS (
      SELECT 1 FROM loans l
      WHERE l.member_id = m.id AND l.book_id = b.id AND l.status IN ('ACTIVE', 'OVERDUE')
  );

-- Returned loan with a fine (Ada / Clean Code) — copies already restored on the book row
INSERT INTO loans (member_id, book_id, borrow_date, due_date, return_date, status, fine_amount)
SELECT m.id, b.id, CURRENT_DATE - 30, CURRENT_DATE - 16, CURRENT_DATE - 10, 'RETURNED', 2.50
FROM members m
JOIN books b ON b.isbn = '9780132350884'
WHERE m.email = 'ada@library.test'
  AND NOT EXISTS (
      SELECT 1 FROM loans l
      WHERE l.member_id = m.id AND l.book_id = b.id AND l.status = 'RETURNED'
  );
