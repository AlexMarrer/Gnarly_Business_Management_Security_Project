# Phase 7 – Dokumentation & Abgabe

## Ziel
Sicherheits-Nachweisdokument erstellen, DB-Skript vorbereiten und alles in einem Abgabe-ZIP zusammenstellen.

---

## Aufgaben

### 7a – Sicherheits-Nachweisdokument erstellen

- [ ] Dokument erstellen: `Nachweisdokument_M183_Uscata_Guener.pdf` (oder .docx)
- [ ] Pro Kriterium: Thema, Kommentar, Code-Ausschnitt, Doku-Link

> **WICHTIG:** Ohne Nachweis = "teilweise erfüllt". Jede Framework-Funktion muss mit Quelle belegt sein!

---

### 7b – DB-Skript erstellen

- [ ] Datei: `src/main/resources/db/init.sql`
- [ ] Inhalt: Tabellen-Struktur + Beispieldaten + eingeschränkter DB-User

```sql
-- src/main/resources/db/init.sql

-- Datenbank anlegen
CREATE DATABASE IF NOT EXISTS businessproject CHARACTER SET utf8mb4;
USE businessproject;

-- Eingeschränkter App-User
CREATE USER IF NOT EXISTS 'app_user'@'localhost'
    IDENTIFIED BY 'sicheres_passwort';
GRANT SELECT, INSERT, UPDATE, DELETE ON businessproject.* TO 'app_user'@'localhost';
FLUSH PRIVILEGES;

-- Beispieldaten Admin (Passwort muss BCrypt+Pepper Hash sein!)
-- Hash generieren und hier eintragen:
INSERT INTO admin (id, admin_name, admin_email, admin_password, admin_number)
VALUES (UUID(), 'Admin', 'admin@test.ch', '$2a$12$HASH_HIER_EINSETZEN', '0791234567');

-- Beispieldaten User
INSERT INTO user (id, uname, uemail, upassword, unumber)
VALUES (UUID(), 'Test User', 'user@test.ch', '$2a$12$HASH_HIER_EINSETZEN', '0797654321');

-- Beispieldaten Produkte
INSERT INTO product (id, pname, pprice, pdesc)
VALUES (UUID(), 'Produkt 1', 19.90, 'Beschreibung Produkt 1');
```

---

### 7c – Abgabe-ZIP zusammenstellen

**Dateiname:** `M183_Abgabe_Uscata_Guener.zip`

```
M183_Abgabe_Uscata_Guener.zip
├── BusinessManagementProject/         ← kompletter IntelliJ Projektordner
│   ├── src/
│   ├── pom.xml
│   └── ...
├── db/
│   └── init.sql                       ← DB-Skript mit Beispieldaten
├── Nachweisdokument_M183_Uscata_Guener.pdf
└── tryhackme/
    ├── alex_uscata_bank_rott.pdf
    └── furkan_guener_bank_rott.pdf
```

- [ ] Projektordner komplett (ohne `target/` Ordner → `.gitignore` beachten)
- [ ] DB-Skript mit funktionierenden Beispieldaten
- [ ] Nachweisdokument vollständig
- [ ] TryHackMe-Dokumentation beider Teammitglieder

---

## Vorlage: Sicherheits-Nachweisdokument

---

### Kriterium 1: Funktionsfähigkeit (0.2)

**Was gemacht:** Projekt auf Spring Boot X.X + Java 25 aktualisiert. Alle CRUD-Funktionen für User, Admin, Produkte und Bestellungen funktionieren.

**Nachweis:** Live-Demo bei Abnahme

---

### Kriterium 2: Rollen/Rechte ⚠️ (0.4 – DOPPELT)

**Was gemacht:**
- URL-Absicherung in `SecurityConfig.java`: `/admin/**` → `ROLE_ADMIN`, `/product/**` → `ROLE_USER`
- `@PreAuthorize("hasRole('ADMIN')")` auf allen Admin-Controller-Methoden
- Thymeleaf `sec:authorize="hasRole('ADMIN')"` für bedingte UI-Anzeige

**Code-Ausschnitt:**
```java
// SecurityConfig.java
.requestMatchers("/admin/**").hasRole("ADMIN")

// AdminController.java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/services")
public String returnBack(Model model) { ... }
```

```html
<!-- Navigation.html -->
<a th:href="@{/admin/services}" sec:authorize="hasRole('ADMIN')">Admin-Panel</a>
```

**Quellen:**
- https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html
- https://github.com/thymeleaf/thymeleaf-extras-springsecurity

**Nachweis bei Abnahme:** URL ohne Rechte aufrufen → 403. UI als verschiedene Rollen zeigen.

---

### Kriterium 3: Sicherer Login ⚠️ (0.4 – DOPPELT)

**Was gemacht:**
- Login via Spring Security POST-Formular (war vorher GET)
- Generische Fehlermeldung: "Benutzername oder Passwort falsch"
- Brute-Force-Schutz: `LoginAttemptService` sperrt nach 5 Fehlversuchen

**Code-Ausschnitt:**
```java
// SecurityConfig.java
.formLogin(form -> form
    .loginPage("/login")
    .failureUrl("/login?error=true")
)
```

```html
<!-- Login.html -->
<div th:if="${param.error}">Benutzername oder Passwort falsch.</div>
```

**Quellen:**
- https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/form.html

**Nachweis:** Browser-DevTools zeigen POST, Fehlermeldung demonstrieren, 5x falsch → gesperrt.

---

### Kriterium 4: Sichere Passwort-Speicherung (0.2)

**Was gemacht:**
- BCrypt mit Cost Factor 12 + Pepper
- Salt automatisch durch BCrypt generiert
- Pepper als Umgebungsvariable (`PEPPER_SECRET`)

**Code-Ausschnitt:**
```java
// PepperPasswordEncoder.java
public String encode(String rawPassword) {
    return bcrypt.encode(rawPassword + pepper);
}
```

**Quellen:**
- https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html
- https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html

**Nachweis:** DB-Eintrag live zeigen: `$2a$12$...` statt Klartext.

---

### Kriterium 5: Session-Management (0.2)

**Was gemacht:**
- Session Fixation Protection: `migrateSession()`
- Timeout: 30 Minuten
- Cookie: `HttpOnly=true`, `Secure=true`, `SameSite=Lax`
- Logout invalidiert Session und löscht Cookie

**Code-Ausschnitt:**
```java
// SecurityConfig.java
.sessionManagement(session -> session
    .sessionFixation().migrateSession()
    .maximumSessions(1)
)
```

```properties
# application.properties
server.servlet.session.timeout=30m
server.servlet.session.cookie.http-only=true
```

**Quellen:**
- https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html
- https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html

**Nachweis:** Cookie-Flags in Browser-DevTools zeigen.

---

### Kriterium 6: Injection-Schutz (0.2)

**Was gemacht:**
- Hibernate/JPA nutzt automatisch Prepared Statements
- Keine native SQL-Queries mit String-Concatenation
- DB-User `app_user` mit eingeschränkten Rechten (nur SELECT/INSERT/UPDATE/DELETE)
- Serverseitige Validierung mit `@Valid` + Bean Validation auf allen Entitäten

**Code-Ausschnitt:**
```java
// UserRepository.java
User findByUemail(String email);
// Hibernate generiert intern: SELECT * FROM user WHERE uemail = ?
```

```sql
GRANT SELECT, INSERT, UPDATE, DELETE ON businessproject.* TO 'app_user'@'localhost';
-- KEIN: DROP, CREATE, ALTER, GRANT
```

**Quellen:**
- https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html
- https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html

**Nachweis:** `SHOW GRANTS FOR 'app_user'@'localhost'` live ausführen.

---

### Kriterium 7: XSS-Schutz (0.2)

**Was gemacht:**
- Alle Thymeleaf-Templates nutzen `th:text` (automatisches Escaping)
- Kein `th:utext` für User-Input verwendet
- Security Headers: `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `X-XSS-Protection`
- HttpOnly-Flag auf Session-Cookie

**Code-Ausschnitt:**
```html
<!-- Alle Templates -->
<p th:text="${product.description}">Beschreibung</p>
<!-- Kein th:utext für User-Input! -->
```

```java
// SecurityConfig.java
.headers(headers -> headers
    .xssProtection(...)
    .contentTypeOptions(...)
    .frameOptions(frame -> frame.deny())
)
```

**Quellen:**
- https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html#unescaped-text
- https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Scripting_Prevention_Cheat_Sheet.html

**Nachweis:** `<script>alert(1)</script>` als Eingabe → wird escaped angezeigt. Response Headers in DevTools.

---

### Kriterium 8: TryHackMe (0.2)

**Was gemacht:** Challenge "Bank Rott" abgeschlossen, Vorgehen dokumentiert, Bezug zum Projekt hergestellt.

**Nachweis:** TryHackMe-Profil zeigen, Dokumentation vorlegen.

---

## Abnahme-Checkliste (Bewertungsraster)

| # | Kriterium | Gewichtung | Nachweis vorbereitet |
|---|---|---|---|
| 1 | Funktionsfähigkeit | 0.2 | [ ] Live-Demo |
| 2 | Rollen/Rechte | **0.4** | [ ] URL-Test + UI-Rollen |
| 3 | Sicherer Login | **0.4** | [ ] POST zeigen + Fehlermeldung |
| 4 | Passwort-Speicherung | 0.2 | [ ] DB-Eintrag + Code |
| 5 | Session-Management | 0.2 | [ ] Cookie-Flags + Logout |
| 6 | Injection-Schutz | 0.2 | [ ] Prepared Statements + DB-User |
| 7 | XSS-Schutz | 0.2 | [ ] Escaping-Demo + Headers |
| 8 | TryHackMe | 0.2 | [ ] Profil + Dokumentation |

**Maximale Note:** 6.0 (alle Kriterien erfüllt)
**Startnote:** 4.0
**Bewertungsschritt:** 0.2 pro Kriterium
