# Phase 3 – Checkliste

## Aufgaben

### 3a – Race Condition beheben: Controller-Instanzfelder entfernen
- [x] `private String email` aus `AdminController` entfernt
- [x] `private User user` aus `AdminController` entfernt
- [x] `searchHandler()`: nutzt `@AuthenticationPrincipal UserDetails` → `services.getUserByEmail(principal.getUsername())`
- [x] `orderHandler()`: nutzt `@AuthenticationPrincipal UserDetails` → setzt `order.setUser(currentUser)` thread-sicher
- [x] `back()`: nutzt `@AuthenticationPrincipal UserDetails` für Bestellhistorie + Benutzername
- **Bemerkung:** Der kritischste Bug der gesamten Applikation. Spring Controller sind Singletons – `private User user` wurde zwischen ALLEN parallelen HTTP-Requests geteilt. Bestellungen hätten dem falschen User zugeordnet werden können.

### 3b – Session-Konfiguration in application.properties
- [x] `server.servlet.session.timeout=30m`
- [x] `server.servlet.session.cookie.http-only=true`
- [x] `server.servlet.session.cookie.secure=false` (Entwicklung)
- [x] `server.servlet.session.cookie.same-site=Lax`
- **Bemerkung:** `secure=false` für localhost-Entwicklung (HTTP). Für Produktion auf `true` setzen (HTTPS-Only). In der Abnahme erklären, warum `secure=true` in Produktion wichtig ist.

### 3c – Session Management in SecurityConfig
- [x] `sessionFixation().migrateSession()` – neue Session-ID nach Login
- [x] `maximumSessions(1)` – nur 1 gleichzeitige Session pro User
- [x] `maxSessionsPreventsLogin(false)` – alte Session wird verdrängt (statt neuen Login zu blockieren)
- [x] `HttpSessionEventPublisher` Bean registriert (nötig für `maximumSessions`)
- **Bemerkung:** Session Fixation Protection ist der Schlüssel-Mechanismus: nach Login wird eine neue Session-ID vergeben, die alte wird ungültig. Angreifer können keine vordefinierten Session-IDs ausnutzen.

### 3d – Logout korrekt konfigurieren
- [x] Logout per POST (nicht GET) – CSRF-geschützt
- [x] `invalidateHttpSession(true)` – Session wird ungültig
- [x] `deleteCookies("JSESSIONID")` – Cookie wird gelöscht
- [x] `clearAuthentication(true)` – Authentication wird entfernt
- [x] `logoutSuccessUrl("/login?logout=true")` – Bestätigung auf Login-Seite
- [x] Logout-Formular in `Navigation.html` (als POST mit CSRF-Token via `th:action`)
- [x] Logout-Formular in `Admin_Page.html` (ersetzt alten "Back to Login"-Link)
- [x] Logout-Formular in `BuyProduct.html` (ersetzt alten "Back to Login"-Link)
- **Bemerkung:** `sec:authorize="isAuthenticated()"` steuert, ob Login-Link oder Logout-Button angezeigt wird. Benötigt `thymeleaf-extras-springsecurity6` Dependency.

### 3e – Session Fixation Protection
- [x] `migrateSession()` in SecurityConfig konfiguriert
- **Bemerkung:** Nach Login wird eine neue Session-ID vergeben. Alte ID ist ungültig. Nachweis: Session-ID vor Login notieren, nach Login vergleichen – muss sich geändert haben.

---

## Abnahme-Kriterium: Bewertungskriterium 5 – Session-Management (0.2)
- [x] `HttpOnly`-Flag auf `JSESSIONID` konfiguriert
- [x] Session wird bei Logout invalidiert
- [x] Session Fixation Protection: neue Session-ID nach Login
- [ ] `HttpOnly`-Flag in Browser-DevTools sichtbar *(manueller Test)*
- [ ] Back-Button nach Logout → Redirect zu Login *(manueller Test)*
- [ ] Session-ID-Wechsel nach Login verifizieren *(manueller Test)*
