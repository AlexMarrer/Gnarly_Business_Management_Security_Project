# Phase 5 – Injection-Schutz & XSS-Schutz

## Ziel
SQL Injection durch Hibernate Prepared Statements und eingeschränkten DB-User absichern. XSS durch Thymeleaf Output Escaping, Input-Validierung und Security Headers verhindern.

---

## IST-Zustand (analysiert)

### SQL Injection
| Befund | Detail |
|---|---|
| Alle 4 Repositories | Nutzen nur Spring Data JPA derived queries – kein String-Concat |
| Keine `@Query` native SQL | Nicht vorhanden – kein Risiko |
| DB-User | `root` mit vollem Zugriff – muss eingeschränkt werden |
| `UserServices.validateLoginCredentials()` | Lädt alle User in Memory, vergleicht in Java – kein SQL-Risiko, aber ineffizient und wird durch Spring Security ersetzt |

### XSS
| Befund | Detail |
|---|---|
| Alle 16 Thymeleaf-Templates | Verwenden **ausschliesslich `th:text`** – kein einziges `th:utext` gefunden ✓ |
| Security Headers | Nicht konfiguriert – kein `X-Frame-Options`, `X-Content-Type-Options` |
| HttpOnly auf Session-Cookie | Noch nicht konfiguriert (wird in Phase 3 erledigt) |

---

## Aufgaben

### 5a – SQL Injection: Nachweis dokumentieren

- [ ] Nachweis erstellen: Hibernate nutzt automatisch Prepared Statements
- [ ] Kein nativer SQL mit String-Concatenation vorhanden → verifizieren und dokumentieren

**Nachweis-Argument (für Abnahmedokument):**

Spring Data JPA / Hibernate generiert für alle Repository-Methoden intern Prepared Statements. Das bedeutet: Benutzereingaben werden NIEMALS direkt in SQL-Strings eingebaut.

```java
// Beispiel: UserRepository (aktuell CrudRepository<User, Integer>, nach Phase 1: JpaRepository<User, UUID>)
public interface UserRepository extends CrudRepository<User, Integer> {
    User findUserByUemail(String email); // Hibernate generiert: SELECT * FROM user WHERE uemail = ?
    // Der '?' wird als Parameter gebunden – SQL Injection unmöglich
}
```

**Doku-Link für Abnahme:** https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html

---

### 5b – Native Queries prüfen (falls vorhanden)

- [ ] Codebase nach `@Query` und `nativeQuery = true` durchsuchen

```bash
# Prüfen ob native Queries vorhanden sind:
grep -r "nativeQuery" src/
grep -r "@Query" src/
```

Wenn gefunden → auf Named Parameters umstellen:

```java
// UNSICHER – niemals so:
@Query(value = "SELECT * FROM user WHERE uname = '" + name + "'", nativeQuery = true)

// SICHER – Named Parameter:
@Query(value = "SELECT * FROM user WHERE uname = :username", nativeQuery = true)
User findByName(@Param("username") String username);
```

---

### 5c – Eingeschränkten DB-User erstellen

- [ ] SQL-Skript erstellen: `src/main/resources/db/create_app_user.sql`
- [ ] `application.properties` anpassen: `root` → `app_user`

```sql
-- src/main/resources/db/create_app_user.sql
-- Eingeschränkter Datenbankuser für die Applikation

CREATE USER IF NOT EXISTS 'app_user'@'localhost'
    IDENTIFIED BY 'sicheres_passwort_hier_einsetzen';

-- Nur notwendige Rechte:
GRANT SELECT, INSERT, UPDATE, DELETE
    ON businessproject.*
    TO 'app_user'@'localhost';

-- KEIN: DROP, CREATE, ALTER, GRANT, FILE, PROCESS, SUPER
FLUSH PRIVILEGES;

-- Rechte verifizieren:
SHOW GRANTS FOR 'app_user'@'localhost';
```

```properties
# application.properties – DB-User anpassen
spring.datasource.username=app_user
spring.datasource.password=${DB_PASSWORD:sicheres_passwort_hier_einsetzen}
```

> **Fallstrick:** `ddl-auto=update` erfordert Rechte für ALTER TABLE. Entweder auf `validate` umstellen (Schema manuell anlegen) oder für die Entwicklung dem `app_user` temporär `ALTER` erlauben.

---

### 5d – XSS: th:text vs th:utext verifizieren

- [ ] Alle Templates prüfen: `grep -r "th:utext" src/main/resources/templates/`
- [ ] Erwartetes Ergebnis: kein Treffer (bereits analysiert – alle Templates nutzen `th:text`) ✓
- [ ] Nachweis dokumentieren: Thymeleaf escapet mit `th:text` automatisch HTML-Sonderzeichen

**Nachweis-Argument:**

```html
<!-- th:text – SICHER: Automatisches Output Escaping -->
<p th:text="${product.description}">Beschreibung</p>
<!-- Eingabe: <script>alert('XSS')</script>
   Ausgabe: &lt;script&gt;alert('XSS')&lt;/script&gt; -->

<!-- th:utext – GEFÄHRLICH: Kein Escaping – niemals für User-Input! -->
<!-- <p th:utext="${userInput}">...</p> ← VERBOTEN -->
```

**Doku-Link:** https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html#unescaped-text

---

### 5e – Security Headers setzen

- [ ] Security Headers in `SecurityConfig.filterChain()` konfigurieren (bereits in Phase 2 angelegt!)
- [ ] Sicherstellen dass folgende Headers gesetzt sind:

```java
// SecurityConfig.java – headers Block
.headers(headers -> headers
    // XSS-Protection Header
    .xssProtection(xss -> xss.headerValue(
        XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
    // Verhindert MIME-Type Sniffing
    .contentTypeOptions(Customizer.withDefaults())
    // Verhindert Clickjacking via iFrame
    .frameOptions(frame -> frame.deny())
    // Optional: Content Security Policy
    // .contentSecurityPolicy(csp -> csp.policyDirectives(
    //     "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'"))
)
```

**Headers im Browser-DevTools prüfen:**

| Header | Wert | Zweck |
|---|---|---|
| `X-Content-Type-Options` | `nosniff` | Verhindert MIME-Type Sniffing |
| `X-Frame-Options` | `DENY` | Verhindert Clickjacking |
| `X-XSS-Protection` | `1; mode=block` | Browser-seitiger XSS-Filter |

---

### 5f – Input-Validierung gegen XSS (Zusammenspiel mit Phase 1)

- [ ] Sicherstellen: `@Pattern` Annotationen auf Freitextfeldern erlauben keine Script-Tags
- [ ] Bean Validation aus Phase 1 ist die Serverseite des XSS-Schutzes

```java
// Beschreibungsfeld: Script-Tags explizit ausschliessen
// ACHTUNG: Feldname ist pdescription (nicht pdesc!)
@NotBlank
@Size(max = 500)
@Pattern(regexp = "^[^<>\"'&]*$",
         message = "Keine HTML-Sonderzeichen erlaubt")
private String pdescription;
```

> **Hinweis:** Thymeleaf `th:text` ist die Hauptverteidigung gegen XSS. Die `@Pattern`-Validierung ist eine zusätzliche Schicht (Defence in Depth).

---

## Fallstricke

- `th:utext` ist in manchen Projekten für Rich-Text nötig – in diesem Projekt VERBOTEN für User-Input
- `X-Frame-Options: DENY` kann Probleme machen wenn die App in iFrames eingebettet werden soll (hier kein Bedarf)
- `app_user` hat kein `ALTER`-Recht → `ddl-auto=update` schlägt fehl → auf `validate` oder `none` umstellen für Produktion
- Security Headers sind bereits in Phase 2 `SecurityConfig` integriert → hier nur verifizieren und dokumentieren

---

## Abnahme-Kriterium: Bewertungskriterium 6 – Injection-Schutz (Gewichtung 0.2)

**Nachweis:** Prepared Statements zeigen (Doku-Link), DB-User-Rechte demonstrieren

- [ ] Repository-Code zeigen: Spring Data JPA → automatisch Prepared Statements
- [ ] Nachweis-Link: Hibernate Doku (https://docs.jboss.org/hibernate/orm/...)
- [ ] DB-User `app_user` zeigen: `SHOW GRANTS FOR 'app_user'@'localhost'` – kein DROP/CREATE
- [ ] Kein `@Query` mit String-Concatenation vorhanden

## Abnahme-Kriterium: Bewertungskriterium 7 – XSS-Schutz (Gewichtung 0.2)

**Nachweis:** Output Escaping zeigen, XSS-Versuch live demonstrieren

- [ ] Thymeleaf `th:text` in Template zeigen + erklären was es macht
- [ ] Live-Demo: `<script>alert(1)</script>` als Produktname eingeben → wird escaped angezeigt, nicht ausgeführt
- [ ] Browser-DevTools → Network → Response Headers: `X-Content-Type-Options`, `X-Frame-Options` sichtbar
- [ ] HttpOnly-Flag auf Session-Cookie zeigen (aus Phase 3)
- [ ] Nachweis-Links: Thymeleaf Doku + OWASP XSS Prevention Cheatsheet
