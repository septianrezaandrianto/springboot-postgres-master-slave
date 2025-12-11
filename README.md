# Spring Boot Postgres Master–Slave (Read/Write Splitting)

Proyek ini adalah contoh bagaimana meng-implementasikan **read/write splitting** di Spring Boot dengan PostgreSQL **master–slave (primary–replica)**.

Aplikasi akan:
- Mengirim **query WRITE** (INSERT/UPDATE/DELETE) ke database **master**
- Mengirim **query READ** (SELECT dengan `@Transactional(readOnly = true)`) ke database **slave**
- Menggunakan `AbstractRoutingDataSource` + `LazyConnectionDataSourceProxy` untuk menentukan data source berdasarkan **status transaksi** yang sedang berjalan.

---

## Teknologi yang digunakan

- Java 25
- Spring Boot 4.0.0
  - spring-boot-starter-webmvc
  - spring-boot-starter-data-jpa
  - spring-boot-starter-actuator
- PostgreSQL
- Lombok
- Maven

---

## Konsep Utama

### 1. Dua DataSource: WRITE & READ

Aplikasi mendefinisikan dua koneksi database:

- `writeDataSource` → mengarah ke **master**
- `readDataSource` → mengarah ke **slave**

Keduanya dikonfigurasi lewat properti:

```yaml
spring:
  datasource:
    write:
      url: jdbc:postgresql://localhost:5432/replication-rnd
      username: postgres
      password: postgres
      driver-class-name: org.postgresql.Driver

    read:
      url: jdbc:postgresql://localhost:5433/replication-rnd
      username: repl
      password: postgres
      driver-class-name: org.postgresql.Driver
```
```
## Struktur Project
src
└── main
    ├── java
    │   └── com.demo.java_25_rnd
    │       ├── Java25RndApplication.java (Main Spring Boot app)
    │       ├── configs
    │       │   └── DataSourceConfig.java (Konfigurasi WRITE/READ + routing)
    │       ├── controllers
    │       │   └── UserController.java (REST controller untuk User)
    │       ├── entities
    │       │   └── User.java (Entity JPA)
    │       ├── repositories
    │       │   └── UserRepository.java (JpaRepository<User, String>)
    │       └── services
    │           └── UserService.java (Bisnis logic + @Transactional)
    └── resources
        └── application.yaml
