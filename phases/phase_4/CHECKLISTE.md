# Phase 4 – Checkliste

## 4a – URL-Absicherung in SecurityConfig
- [x] `SecurityConfig.java` um vollständige URL-Regeln erweitern (`.authorizeHttpRequests` Block mit allen Endpunkten)

## 4b – @PreAuthorize auf AdminController
- [x] `@EnableMethodSecurity` in `SecurityConfig` aktivieren
- [x] Alle Admin-Methoden mit `@PreAuthorize("hasRole('ADMIN')")` absichern
- [x] User-Methoden (`/product/**`) mit `@PreAuthorize("hasRole('USER')")` absichern

## 4c – @PreAuthorize auf UserController & ProductController
- [x] `UserController.java`: alle Methoden mit `@PreAuthorize("hasRole('ADMIN')")`
- [x] `ProductController.java`: Add/Update/Delete mit `@PreAuthorize("hasRole('ADMIN')")`

## 4d – Thymeleaf Frontend-Absicherung
- [x] `xmlns:sec` Namespace zu relevanten Templates hinzufügen (`Admin_Page.html`, `Navigation.html`)
- [x] `sec:authorize="hasRole('ADMIN')"` um gesamten Admin-Page-Inhalt
- [x] `sec:authorize="isAuthenticated()"` für Logout-Button in Navigation
- [x] `sec:authorize="!isAuthenticated()"` für Login-Link in Navigation

## 4e – Abnahme-Tests (manuell durchzuführen)
- [ ] Nicht eingeloggt → `/admin/services` → Redirect zu `/login`
- [ ] Eingeloggt als USER → `/admin/services` → `403 Forbidden`
- [ ] Eingeloggt als USER → Admin-CRUD-Buttons im Frontend NICHT sichtbar
- [ ] Eingeloggt als ADMIN → alle Admin-Funktionen sichtbar und nutzbar
- [ ] Eingeloggt als USER → `/addAdmin` direkt aufrufen → Formular wird NICHT ausgeliefert
