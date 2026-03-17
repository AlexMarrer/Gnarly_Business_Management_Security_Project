# Implementierungsplan – Business Management Secured

## Ausgangspunkt

Wir forken/klonen das bestehende Projekt:
**https://github.com/SuhasKamate/Business_Management_Project**

Das Projekt ist eine Spring Boot + Thymeleaf + Hibernate Applikation mit MySQL.
Wir müssen es auf **Java 25** und aktuelle Spring Boot Version bringen und die Sicherheit implementieren.

---

## Phase 0: Projekt Setup & Lauffähigkeit

- [ ] Repo klonen / forken
- [ ] Auf Java 25 + aktuelle Spring Boot Version upgraden
- [ ] MySQL-DB via XAMPP einrichten
- [ ] `application.properties` konfigurieren (DB-Connection)
- [ ] Projekt kompilieren und starten
- [ ] Alle bestehenden Funktionen testen (User-Login, Admin-Login, CRUD-Operationen)
- [ ] Sicherstellen: Applikation läuft stabil und stürzt nicht ab

> **Bewertungskriterium 1:** Funktionsfähigkeit sicherstellen

---

## Phase 1: Datenmodell & Validierung anpassen

- [ ] Entitäten prüfen/anpassen gemäss Projektantrag:
  - USER: ID (Guid), Benutzername, E-Mail, Passwort, Telefonnummer
  - ADMIN: ID (Guid), Benutzername, E-Mail, Passwort, Telefonnummer
  - ORDERS: ID, Name, Einzelpreis, Anzahl, Bestelldatum, Totalpreis, User-Referenz
  - PRODUKT: ID, Name, Preis, Beschreibung
- [ ] Bean Validation Annotationen auf allen Entitäten:
  - `@NotBlank`, `@Size`, `@Email`, `@Pattern`, `@Min`, `@Max`, `@DecimalMin`
- [ ] Passwort-Validierung: min 8 Zeichen, Gross+Klein+Zahl+Sonderzeichen (`@Pattern`)
- [ ] Telefonnummer: nur Ziffern, 10-15 Stellen (`@Pattern`)
- [ ] Clientseitige Validierung in Thymeleaf-Templates (HTML5 `required`, `minlength`, `pattern`)
- [ ] `@Valid` in allen Controller-Methoden die Daten entgegennehmen

---

## Phase 2: Spring Security einrichten

### 2a: Dependency & Grundkonfiguration
- [ ] `spring-boot-starter-security` Dependency hinzufügen
- [ ] `SecurityConfig` Klasse erstellen (`@Configuration`, `@EnableWebSecurity`)
- [ ] `SecurityFilterChain` Bean definieren
- [ ] Rollen definieren: `ROLE_USER`, `ROLE_ADMIN`
- [ ] Custom `UserDetailsService` implementieren (gegen DB authentifizieren)

### 2b: Passwort-Sicherheit
- [ ] `BCryptPasswordEncoder` als Bean registrieren
- [ ] Pepper implementieren (globales Secret, z.B. via `application.properties` oder Env-Variable)
  - Passwort vor dem Hashen mit Pepper konkatenieren
- [ ] Bestehende Klartext-Passwörter in DB migrieren (Migrationsskript oder beim ersten Login)
- [ ] Sicherstellen: Passwörter werden NIRGENDS geloggt

### 2c: Login-Prozess absichern
- [ ] Login-Formular per POST
- [ ] Generische Fehlermeldung: "Benutzername oder Passwort falsch"
- [ ] Kein Unterschied in Fehlermeldung ob User existiert oder nicht
- [ ] Optional: Brute-Force-Schutz (Account Lockout / Rate Limiting)
- [ ] Redirect nach erfolgreichem Login

### 2d: Session-Management
- [ ] Session Fixation Protection: `sessionManagement().sessionFixation().migrateSession()`
- [ ] Session-Timeout: `server.servlet.session.timeout=30m`
- [ ] Max Sessions: `maximumSessions(1)`
- [ ] Logout: Session invalidieren, Cookies löschen
- [ ] Cookie-Konfiguration:
  - `server.servlet.session.cookie.http-only=true`
  - `server.servlet.session.cookie.secure=true` (für HTTPS)
  - `server.servlet.session.cookie.same-site=Lax`

---

## Phase 3: Autorisierung (Frontend + Backend)

### 3a: Backend-Absicherung
- [ ] `@EnableMethodSecurity` aktivieren
- [ ] URL-basierte Absicherung in `SecurityFilterChain`:
  - `/admin/**` → nur `ROLE_ADMIN`
  - `/user/**` → nur `ROLE_USER`
  - `/login`, `/register`, `/css/**`, `/js/**` → permitAll
- [ ] `@PreAuthorize("hasRole('ADMIN')")` auf Admin-Controller-Methoden
- [ ] `@PreAuthorize("hasRole('USER')")` auf User-Controller-Methoden

### 3b: Frontend-Absicherung (Thymeleaf)
- [ ] Spring Security Thymeleaf Extras Dependency hinzufügen
- [ ] `sec:authorize="hasRole('ADMIN')"` für Admin-Navigationslinks
- [ ] `sec:authorize="isAuthenticated()"` für geschützte Bereiche
- [ ] Aktuell eingeloggten Benutzer anzeigen (`sec:authentication="name"`)
- [ ] Admin-Formulare werden nicht ausgeliefert wenn kein Admin-Recht

---

## Phase 4: Injection-Schutz

- [ ] Prüfen: Alle DB-Queries gehen über Hibernate/JPA (keine String-Concatenation)
- [ ] Falls native Queries vorhanden: auf Named Parameters umstellen (`:paramName`)
- [ ] Serverseitige Validierung auf allen Eingaben (bereits in Phase 1)
- [ ] DB-User mit eingeschränkten Rechten erstellen (nicht root):
  - Nur SELECT, INSERT, UPDATE, DELETE auf die App-Datenbank
  - Kein DROP, CREATE, ALTER, GRANT
- [ ] SQL-Skript für eingeschränkten DB-User erstellen

---

## Phase 5: XSS-Schutz

- [ ] Sicherstellen: Alle Thymeleaf-Templates verwenden `th:text` (NICHT `th:utext`)
- [ ] Input-Validierung: Sonderzeichen in Freitextfeldern prüfen
- [ ] Session-Cookie: HttpOnly-Flag (bereits in Phase 2d)
- [ ] Security-Headers setzen (in `SecurityFilterChain` oder per Filter):
  - `X-Content-Type-Options: nosniff`
  - `X-Frame-Options: DENY`
  - `X-XSS-Protection: 1; mode=block`
  - Optional: `Content-Security-Policy`

---

## Phase 6: Eigener Fokus – TryHackMe "Bank Rott"

- [ ] TryHackMe Account erstellen (beide Teammitglieder)
- [ ] Challenge "Bank Rott" starten und durcharbeiten
- [ ] Dokumentation erstellen:
  - Pro Aufgabe: Screenshot + Vorgehensbeschreibung
  - Gefundene Schwachstellen erklären
  - Bezug zum eigenen Projekt
- [ ] Deadline: **29.03.2026**

---

## Phase 7: Dokumentation & Abgabe

- [ ] **Sicherheits-Nachweisdokument** erstellen (für die Abnahme):
  - Pro Kriterium aus dem Beurteilungsraster:
    - Thema
    - Kurzer Kommentar was gemacht wurde
    - Code-Ausschnitt der eigenen Implementierung
    - Links auf Spring Security / Thymeleaf / Hibernate Dokumentation als Beleg
- [ ] DB-Skript mit Beispieldaten erstellen
- [ ] Abgabe-ZIP erstellen: `M183_Abgabe_Uscata_Guener.zip`
  - Projektordner (IntelliJ)
  - DB-Skript
  - Nachweisdokument
  - TryHackMe-Dokumentation

---

## Checkliste für die Abnahme

Bei der Abnahme muss zu jedem Kriterium **konkret bewiesen** werden, warum die Implementierung sicher ist:

| # | Kriterium | Gewichtung | Nachweis |
|---|-----------|-----------|----------|
| 1 | Funktionsfähigkeit | Normal (0.2) | Live-Demo |
| 2 | Rollen/Rechte (FE+BE) | **Doppelt (0.4)** | URL-Aufruf ohne Rechte zeigen, UI zeigen |
| 3 | Sicherer Login | **Doppelt (0.4)** | Fehlermeldungen zeigen, POST zeigen |
| 4 | Passwort-Speicherung | Normal (0.2) | DB-Eintrag zeigen (Hash), Code zeigen |
| 5 | Session-Management | Normal (0.2) | Cookie-Flags zeigen, Timeout testen |
| 6 | Injection-Schutz | Normal (0.2) | Prepared Statements zeigen, DB-User-Rechte |
| 7 | XSS-Schutz | Normal (0.2) | Output Escaping zeigen, XSS-Versuch Demo |
| 8 | TryHackMe Bank Rott | Normal (0.2) | Profil + Dokumentation zeigen |
