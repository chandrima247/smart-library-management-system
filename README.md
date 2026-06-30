# Smart Library Management System (SLMS)

A production-style college library ERP built with **Java 21 · Spring Boot 3 · Spring Security · Spring Data JPA · Thymeleaf**, with a bold **neobrutalist** UI. It ships with an embedded **H2** database so it runs with zero setup, and a **MySQL** profile for deployment.

## Features (all role-based)

- **Three roles** — Administrator, Librarian, Student — with a single unified login.
- **Authentication & approval** — self-registration creates a *pending* account; an admin must approve it. Passwords hashed with **BCrypt**; CSRF protection and session management via Spring Security.
- **Catalogue** — `Book` (identified by **ISBN**) is kept separate from `BookCopy` (identified by **accession number**; barcode optional). Add titles by ISBN with **Google Books** auto-fill (graceful fallback to manual entry when offline).
- **Circulation** — borrow **requests** → librarian **approve/reject** → **issue** → **return**, supporting both **Home borrow** (with due date) and **in-library Reading**. Overdue **fines** raised automatically on late return.
- **Occupancy** — student **check-in / check-out**, live **current readers** and seat availability.
- **Notifications** — request approved/rejected, issue, return, due reminder, fine.
- **Dashboards** — tailored stats for each role.

## Quick start

You need a **JDK 21**. Maven is *not* required — the project includes the Maven Wrapper (`mvnw`).

> Don't have a JDK? Install Temurin 21: `winget install EclipseAdoptium.Temurin.21.JDK`
> then open a **new** terminal so `JAVA_HOME`/`PATH` refresh.

### Level 1 — run on H2 (zero database setup)

```bat
cd C:\Users\CG\Downloads\slms
mvnw.cmd spring-boot:run
```

Then open **http://localhost:8080**. The first run seeds demo data.

**Demo logins**

| Role        | Username    | Password      |
|-------------|-------------|---------------|
| Admin       | `admin`     | `admin123`    |
| Librarian   | `librarian` | `librarian123`|
| Student     | `student`   | `student123`  |

H2 data persists in `./data/slms.mv.db`. Dev DB console: http://localhost:8080/h2-console
(JDBC URL `jdbc:h2:file:./data/slms`, user `sa`, no password).

### Level 3 — run on MySQL

1. Install MySQL 8 and create the schema:
   ```sql
   CREATE DATABASE slms CHARACTER SET utf8mb4;
   ```
2. Set credentials (env vars or edit `src/main/resources/application-mysql.properties`):
   ```bat
   set DB_USER=root
   set DB_PASSWORD=yourpassword
   ```
3. Run with the MySQL profile:
   ```bat
   mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

Hibernate creates the tables (`ddl-auto=update`). The same seed data is loaded on first run.

### Build a runnable jar

```bat
mvnw.cmd clean package
java -jar target\slms-1.0.0.jar                       REM H2
java -jar target\slms-1.0.0.jar --spring.profiles.active=mysql
```

### Run tests

```bat
mvnw.cmd test
```

## Deploy for free (Render)

The repo includes a `Dockerfile` and a `render.yaml` blueprint for a **free** deployment on [Render](https://render.com):

1. Push this repo to GitHub (already done if you're reading this on GitHub).
2. Sign in to Render → **New +** → **Blueprint** → connect this repository.
3. Render reads `render.yaml`, builds the Docker image, and deploys. Open the generated `*.onrender.com` URL.

Free-plan caveats: the service sleeps after ~15 min idle (first request then cold-starts in ~30s), and the filesystem is ephemeral so the H2 database resets on redeploy — the seeder repopulates demo data automatically. For persistent data, attach a managed database and run with the `mysql`/Postgres profile.

Any Docker host works the same way:

```bat
docker build -t slms .
docker run -p 8080:8080 slms
```

## Project structure

```
src/main/java/com/college/slms
├── config/        SlmsProperties, SecurityConfig wiring, JPA auditing, DataSeeder
├── domain/        JPA entities + enums (Book/BookCopy, Loan, Fine, OccupancySession, ...)
├── repository/    Spring Data JPA repositories
├── service/       UserService, BookService, CirculationService, OccupancyService,
│                  NotificationService, DashboardService, GoogleBooksClient, scheduler
├── security/      UserDetails adapter, role-based login success handler
├── exception/     Domain exceptions + global handler
└── web/           Controllers (auth, student, librarian, admin, notifications) + forms

src/main/resources
├── templates/     Thymeleaf views (fragments/ = layout, nav, head, ui components)
├── static/        neobrutal.css, app.js
└── application*.properties
```

## Configuration (in `application.properties`)

| Property                              | Default | Meaning                         |
|---------------------------------------|---------|---------------------------------|
| `slms.circulation.home-loan-days`     | 14      | Home loan period                |
| `slms.circulation.max-active-loans`   | 5       | Concurrent loans per student    |
| `slms.circulation.fine-per-day`       | 2.00    | Overdue fine per day            |
| `slms.library.reading-hall-capacity`  | 100     | Reading-hall seats              |
| `slms.google-books.enabled`           | true    | ISBN metadata lookup            |
| `slms.google-books.api-key`           | (blank) | Optional Google Books API key   |

## Notes

- `open-in-view=true` is enabled so Thymeleaf can read lazy associations while rendering — a pragmatic choice for this server-rendered MVP.
- A daily scheduler flags overdue loans and sends due reminders; it’s safe to leave running.
```
