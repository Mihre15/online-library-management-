# Online Library Management System
[![CI](https://github.com/Mihre15/Ahadu-estate-/actions/workflows/ci.yml/badge.svg)](https://github.com/Mihre15/Ahadu-estate-/actions)
Group project for Software Testing and Validation (Addis Ababa University).


## What this is

A small library management backend: members can register, books can be added to
the catalogue, and members borrow and return books. The domain was chosen to
exercise every technique required by the course:

- **Equivalence partitioning / boundary value analysis** — [`FineCalculator`](src/main/java/com/library_management/library_project/service/FineCalculator.java)
  computes a late-return fine from a banded schedule (no fine / 0.50 per day /
  1.00 per day / 2.00 per day), with class boundaries at 0, 1, 7, 8, 30 and 31
  days overdue.
- **Decision table** — [`LoanEligibilityService`](src/main/java/com/library_management/library_project/service/LoanEligibilityService.java)
  decides whether a member may borrow a book from four independent conditions
  (membership active, under the loan limit, a copy available, fines within
  the limit).
- **State transition testing** — [`LoanService`](src/main/java/com/library_management/library_project/service/LoanService.java)
  drives a loan through `ACTIVE → OVERDUE → RETURN_PENDING → RETURNED` / `LOST`,
  with `RETURNED` and `LOST` as terminal states.

## Tech stack

Java 21, Spring Boot 4.1.1 (Web MVC, Data JPA, Validation), PostgreSQL at
runtime, H2 in-memory for tests, JUnit 5 + Mockito + AssertJ, JaCoCo for
coverage.

## Running the application

A plain `./mvnw spring-boot:run` starts the self-contained `demo` profile with
H2, so collaborators do not need local PostgreSQL or JWT setup for a
presentation. Production uses the explicit `prod` profile.

Set a unique JWT signing secret before starting the production profile. It must be
at least 32 bytes and must not be committed to the repository:

```bash
export JWT_SECRET="$(openssl rand -base64 48)"
```

The `demo` profile has a local-only development secret so it can run without
additional configuration; never use that secret outside local demonstrations.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

The REST API is then available under `http://localhost:8080/api`.

### Spec aliases (frontend / Selenium)

Student UI and system tests should call these endpoints (JWT required except login and catalog):

| Endpoint | Method | Purpose |
|---|---|---|
| `/api/auth/login` | POST | `{ email, password }` → `{ token, studentId }` |
| `/api/books?query=` | GET | Search by title, author, or ISBN (`copiesAvailable` is included) |
| `/api/books/{id}` | GET | Book detail |
| `/api/loans/borrow` | POST | `{ bookId }` + `Authorization: Bearer` → `{ success, loanId?, denialReason? }` |
| `/api/loans/{id}/return` | POST | Student return request → `{ success, status: "RETURN_PENDING", message }` |
| `/api/loans/{id}/confirm-return` | POST | Admin confirms physical receipt and finalizes the return |
| `/api/students/{id}/loans` | GET | Loan history with nested `book` |

The Next.js app lives in `frontend/` (`npm install && npm run dev` in that folder, proxying `/api` to port 8080).

To run the API without PostgreSQL (in-memory H2 + seed data):

```bash
./mvnw spring-boot:run
```

Demo logins (password `Password123`): `ada@library.test` (active student),
`suspended@library.test`, `fines@library.test`, and `admin@library.test`
(administrator).

### Legacy member/admin endpoints

Legacy member, catalog-management, and loan-management endpoints require an
`ADMIN` JWT. Catalog reads (`GET /api/books` and `GET /api/books/{id}`) remain
public for browsing. Student borrowing, returning, and loan-history endpoints
require a `STUDENT` or `ADMIN` JWT and enforce ownership for student requests.

Student returns are intentionally two-step: the student submits a return
request, which changes the loan to `RETURN_PENDING` but does not restore
inventory or calculate the final fine. A librarian confirms physical receipt
through `POST /api/loans/{id}/confirm-return`; only then does the loan become
`RETURNED`, the fine use the confirmation date, and the copy return to stock.
Pending returns continue to count as open loans.

| Resource | Endpoints |
|---|---|
| Members | `POST /api/members`, `GET /api/members`, `GET /api/members/{id}`, `PUT /api/members/{id}/suspend`, `PUT /api/members/{id}/reactivate` |
| Books | `POST /api/books`, `GET /api/books[?category=]`, `GET /api/books/{id}`, `PUT /api/books/{id}/copies?count=` |
| Loans | `POST /api/loans`, `PUT /api/loans/{id}/return`, `PUT /api/loans/{id}/mark-overdue`, `PUT /api/loans/{id}/report-lost`, `GET /api/loans/{id}`, `GET /api/loans/member/{memberId}` |

## Running the tests

```bash
./mvnw test
```

This runs the whole suite, organised by the test pyramid:

- **Unit tests** (`src/test/java/.../service/*Test.java`) — pure business
  logic with no Spring context. Repositories and collaborators are Mockito
  test doubles, so `FineCalculator` can be stubbed independently of
  `LoanService` and repositories are mocked rather than hitting a database.
- **Integration tests** (`src/test/java/.../repository/*Test.java`,
  `src/test/java/.../integration/*Test.java`) — `@DataJpaTest` for the
  repository/entity mapping layer, and `@SpringBootTest` (with `@AutoConfigureMockMvc`
  for the controller tests) wiring the real service, repository and an
  in-memory H2 database together, including full HTTP-request-to-database
  round trips through the REST controllers.

Test-only configuration lives in `src/test/resources/application-test.properties`
(H2 datasource, activated via the `test` Spring profile).

### Coverage

JaCoCo runs automatically with `./mvnw test` and writes an HTML report to
`target/site/jacoco/index.html`. Core business logic (`FineCalculator`,
`LoanEligibilityService`, `LoanService`, `MemberService`, `BookService`) is
at 100% branch coverage; the thin controller/DTO layer is intentionally not
targeted for full coverage since it holds no branching business logic.

## Continuous integration

Two independent pipelines build and run the backend test suite on every change.
Both compile with `./mvnw clean compile`, run `./mvnw test`, publish the JUnit
results, and archive the JaCoCo HTML report as a build artifact.

### GitHub Actions

[`.github/workflows/ci.yml`](.github/workflows/ci.yml) runs on every push or
pull request to `main` or `backend`:

- **backend** job — JDK 21, build, test, uploads JUnit results and the JaCoCo
  report as downloadable artifacts.
- **frontend** job — `npm ci` then `npx tsc --noEmit` (type-checks the Next.js
  app).

Results appear under the repository's **Actions** tab; open a run and download
the `jacoco-report` artifact to view coverage.

### Jenkins

The [`Jenkinsfile`](Jenkinsfile) at the repo root defines the same backend
pipeline for a self-hosted Jenkins instance. To run Jenkins locally via Docker:

```bash
docker compose -f docker-compose.jenkins.yml up -d
```

Jenkins comes up on **http://localhost:8081** (8080 is left free for the Spring
Boot app itself). Retrieve the initial admin password with:

```bash
docker exec library-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Complete the setup wizard, then create a new **Pipeline** job with
**Pipeline script from SCM** → Git → this repository's URL, script path
`Jenkinsfile`.

### Selenium / end-to-end tests

`src/test/java/.../selenium/` contains a Page Object Model suite (Selenium
WebDriver 4 + JUnit 5) covering the student flows (login, registration,
search, borrowing, loans) and the full admin console (books, members, loans,
dashboard), tagged `@Tag("selenium")`.

These are **excluded from both CI pipelines by default** — `pom.xml`'s
Surefire configuration sets `excludedGroups: selenium` — because they need a
real browser and a running frontend **and** backend, not a CI build agent.
Run them explicitly against a locally running instance of the app:

```bash
./mvnw test -DexcludedGroups= -Dgroups=selenium
```

## Project structure

```
src/main/java/.../entity      JPA entities (Member, Book, Loan) and enums
src/main/java/.../repository  Spring Data JPA repositories
src/main/java/.../service     Business logic (fine calculation, eligibility, loan lifecycle)
src/main/java/.../controller  REST controllers
src/main/java/.../dto         Request/response DTOs
src/main/java/.../exception   Domain exceptions + a global @RestControllerAdvice
src/test/java/.../service     Unit tests (test doubles, no Spring context)
src/test/java/.../repository  @DataJpaTest integration tests
src/test/java/.../integration @SpringBootTest integration tests (service + controller layers)
src/test/java/.../selenium     Selenium WebDriver + Page Object Model end-to-end tests
```
