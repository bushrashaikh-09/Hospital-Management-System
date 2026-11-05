# Hospital Management System

A Java Spring Boot hospital management website with:

- Admin login
- User signup & login
- Admin and user dashboards
- Sidebar toggle
- User profile page
- MySQL database integration

## Tech stack

- Java 21
- Spring Boot 3.3.2
- Thymeleaf
- Spring Data JPA
- MySQL 8

## MySQL Workbench setup

1. Open MySQL Workbench
2. Connect to your MySQL 8 server
3. Run the SQL file:
   `hospital_management_mysql.sql`
4. Make sure the database `hospital_management` exists

## Default database config

The project uses:

- Database: `hospital_management`
- Username: `root`
- Password: `Mysql123`

These values are already configured in:

- [src/main/resources/application.properties](src/main/resources/application.properties)

## MySQL auto-update behavior

The project is configured with:

- `spring.jpa.hibernate.ddl-auto=update`

This means when you change the Java entities or website data models, Hibernate will automatically update the MySQL schema and tables based on the application model.

> If you want a clean database setup, run the SQL file first:
> `hospital_management_mysql.sql`

## Run the project

```bash
mvn spring-boot:run
```

Open:

```text
http://localhost:8081/login
```

## Default admin login

- Email: `admin@hospital.com`
- Password: `admin123`

## User signup

- Go to the registration page from the login screen
- Create a new account
- Login with your new credentials
