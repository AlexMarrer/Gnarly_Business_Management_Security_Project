# Phase 5 – Checkliste

## 5a – SQL Injection: Nachweis
- [x] Nachweis: Spring Data JPA / Hibernate nutzt automatisch Prepared Statements
- [x] Kein nativer SQL mit String-Concatenation vorhanden → verifiziert (kein `@Query`, kein `nativeQuery`)

## 5b – Native Queries prüfen
- [x] Codebase nach `@Query` und `nativeQuery=true` durchsucht → kein Treffer

## 5c – Eingeschränkten DB-User erstellen
- [x] `src/main/resources/db/create_app_user.sql` erstellt
- [x] `application.properties`: username/password via Env-Variable (`${DB_USERNAME:root}`, `${DB_PASSWORD:}`)
- [ ] SQL-Skript manuell in MySQL ausführen (Passwort in Skript ersetzen)
- [ ] `DB_USERNAME` und `DB_PASSWORD` als Env-Variablen in Produktion setzen

## 5d – XSS: th:text vs th:utext verifizieren
- [x] Alle Templates geprüft: kein einziges `th:utext` gefunden
- [x] Alle Templates nutzen ausschliesslich `th:text` → automatisches Output Escaping aktiv

## 5e – Security Headers
- [x] `X-XSS-Protection: 1; mode=block` in `SecurityConfig` gesetzt
- [x] `X-Content-Type-Options: nosniff` in `SecurityConfig` gesetzt
- [x] `X-Frame-Options: DENY` in `SecurityConfig` gesetzt

## 5f – Input-Validierung gegen XSS
- [x] `Product.pname`: `@Pattern(regexp = "^[^<>\"'&]*$")` hinzugefügt
- [x] `Product.pdescription`: `@Pattern(regexp = "^[^<>\"'&]*$")` hinzugefügt
- [x] `User.uname`: `@Pattern(regexp = "^[^<>\"'&]*$")` hinzugefügt
- [x] `Admin.adminName`: `@Pattern(regexp = "^[^<>\"'&]*$")` hinzugefügt

## Abnahme-Tests (manuell durchzuführen)
- [ ] Repository-Code zeigen: Spring Data JPA → automatisch Prepared Statements
- [ ] DB-User `app_user` zeigen: `SHOW GRANTS FOR 'app_user'@'localhost'` – kein DROP/CREATE
- [ ] Thymeleaf `th:text` in Template zeigen + erklären
- [ ] Live-Demo: `<script>alert(1)</script>` als Produktname → wird escaped, nicht ausgeführt
- [ ] Browser-DevTools → Response Headers: `X-Content-Type-Options`, `X-Frame-Options` sichtbar
- [ ] HttpOnly-Flag auf Session-Cookie zeigen
