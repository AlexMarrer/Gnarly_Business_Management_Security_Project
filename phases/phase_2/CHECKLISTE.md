# Phase 2 – Checkliste

## Aufgaben

### 2a – Dependencies hinzufügen
- [x] `pom.xml`: `spring-boot-starter-security` eingetragen
- [x] `pom.xml`: `thymeleaf-extras-springsecurity6` eingetragen
- [x] `spring-boot-starter-validation` war bereits vorhanden
- **Bemerkung:** Beide Dependencies werden durch Spring Boot Parent versioniert – keine explizite Version nötig.

### 2b – PepperPasswordEncoder Service erstellen
- [x] `src/main/java/com/business/security/PepperPasswordEncoder.java` erstellt
- [x] Implementiert `PasswordEncoder`-Interface direkt (sauberer als Wrapper)
- [x] BCrypt-Stärke: Cost Factor 12
- [x] Pepper-Secret in `application.properties` konfiguriert via `${PEPPER_SECRET:bitte-aendern-in-produktion}`
- **Bemerkung:** PepperPasswordEncoder implementiert `PasswordEncoder` direkt, damit es im `DaoAuthenticationProvider` ohne anonyme Wrapper-Klasse verwendet werden kann.

### 2c – UserDetailsService implementieren
- [x] `src/main/java/com/business/security/CustomUserDetailsService.java` erstellt
- [x] Prüft User-Tabelle zuerst, dann Admin-Tabelle
- [x] User → `ROLE_USER`, Admin → `ROLE_ADMIN`
- [x] Integration mit `LoginAttemptService` für Account-Locking (`accountLocked`-Flag im `UserDetails`)
- **Bemerkung:** Bestehende `findUserByUemail()` in UserRepository beibehalten (funktioniert, Spring Data leitet Query korrekt ab).

### 2d – SecurityConfig erstellen
- [x] `src/main/java/com/business/security/SecurityConfig.java` erstellt
- [x] `@EnableWebSecurity` + `@EnableMethodSecurity` (für Phase 4 vorbereitet)
- [x] Öffentliche Endpunkte: `/`, `/login`, `/home`, `/products`, `/location`, `/about`, statische Ressourcen
- [x] Admin-Endpunkte: `hasRole("ADMIN")` – CRUD für Users, Admins, Products
- [x] User-Endpunkte: `hasRole("USER")` – `/product/**`
- [x] Custom `successHandler`: ADMIN → `/admin/services`, USER → `/product/back`
- [x] Custom `failureHandler`: unterscheidet zwischen falschen Credentials und gesperrtem Account
- [x] Security Headers: X-XSS-Protection, X-Content-Type-Options, X-Frame-Options: DENY
- **Bemerkung:** `usernameParameter("email")` und `passwordParameter("password")` müssen exakt mit den `name`-Attributen im Login-Formular übereinstimmen.

### 2e – Login-Seite anpassen (GET → POST)
- [x] Zwei separate Formulare (Admin + User, beide GET) → ein einheitliches POST-Formular
- [x] Login-Logik (`adminLogin`, `userlogin`) aus `AdminController` entfernt – Spring Security übernimmt
- [x] `AdminLogin`/`UserLogin`-Referenzen aus `HomeController` entfernt
- [x] CSRF-Token wird durch `th:action` automatisch eingefügt
- [x] Generische Fehlermeldung: "Benutzername oder Passwort falsch" (nicht welches Feld)
- [x] Separate Meldung für gesperrte Accounts
- [x] Logout-Bestätigung angezeigt nach erfolgreichem Logout
- **Bemerkung:** `AdminLogin.java` und `UserLogin.java` werden nirgends mehr referenziert, bleiben aber als Referenz im Projekt.

### 2f – Passwörter migrieren (Klartext → BCrypt+Pepper)
- [x] `UserServices.addUser()`: Passwort wird vor dem Speichern gehasht
- [x] `AdminServices.addAdmin()`: Passwort wird vor dem Speichern gehasht
- [x] `validateLoginCredentials()` aus `UserServices` entfernt
- [x] `validateAdminCredentials()` aus `AdminServices` entfernt
- **Bemerkung:** Alle bestehenden Testdaten in der DB müssen gelöscht werden – Klartext-Passwörter sind nach BCrypt-Migration ungültig. Neue Benutzer über das Admin-Panel anlegen.

### 2g – Brute-Force-Schutz (Account Lockout)
- [x] `src/main/java/com/business/security/LoginAttemptService.java` erstellt
- [x] In-Memory `ConcurrentHashMap` für Fehlversuch-Zählung
- [x] Max 5 Fehlversuche → Account gesperrt
- [x] Erfolgreicher Login setzt Zähler zurück
- [x] Integration in `CustomUserDetailsService` (setzt `accountLocked`-Flag)
- [x] Failure-Handler zählt nur bei echten Fehlversuchen (nicht bei bereits gesperrtem Account)
- **Bemerkung:** Lock ist in-memory und wird bei App-Neustart zurückgesetzt. Für Produktion sollte dies DB-basiert mit Zeitlimit implementiert werden.

---

## Abnahme-Kriterium: Bewertungskriterium 3 – Sicherer Login (DOPPELT GEWICHTET 0.4)
- [x] Login erfolgt per POST (kein GET) – im Browser-Netzwerk-Tab prüfbar
- [x] Credentials tauchen NICHT in der URL auf
- [x] Fehlermeldung ist generisch: "Benutzername oder Passwort falsch"
- [x] Brute-Force-Schutz: 5x falsch → gesperrt
- [ ] Live-Demo mit Browser-DevTools *(manueller Test)*

## Abnahme-Kriterium: Bewertungskriterium 4 – Passwort-Speicherung (0.2)
- [x] Passwörter werden mit BCrypt (Cost 12) + Pepper gehasht
- [x] `PepperPasswordEncoder.java` implementiert
- [ ] DB-Eintrag zeigen: Passwort-Feld enthält `$2a$12$...` *(manueller Test)*
