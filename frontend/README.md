# Campus Library frontend

Next.js UI for the student flows in the requirement spec: login, catalog search, borrow, and my loans.

```bash
npm install
npm run dev
```

The app is at `http://localhost:3000`. `/api/*` is proxied to the Spring Boot server on port 8080.

Start the API first with `./mvnw spring-boot:run`. The default startup uses the demo profile with in-memory H2 and seeded data.

For production PostgreSQL, use `./mvnw spring-boot:run -Dspring-boot.run.profiles=prod` with the required database variables and `JWT_SECRET`.

Demo students (password `Password123`):

- `ada@library.test` — active, with mixed loan states
- `suspended@library.test` — borrow denied
- `fines@library.test` — outstanding fines above the borrow threshold
