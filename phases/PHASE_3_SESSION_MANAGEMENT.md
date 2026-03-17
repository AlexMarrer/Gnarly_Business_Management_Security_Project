# Phase 3 – Session-Management

## Ziel
Sicheres Session-Management implementieren: Session Fixation Protection, Timeout, Cookie-Flags und den kritischen Race-Condition-Bug (User als Controller-Instanzfeld) beheben.

---

## IST-Zustand (analysiert) – KRITISCHE LÜCKEN

### Race Condition in AdminController.java
```java
// AdminController.java – Zeilen 43–44 – GEFÄHRLICH!
private String email;   // Singleton-Feld – geteilt zwischen ALLEN Requests!
private User user;      // Race Condition: User A sieht Daten von User B!
```
Spring Controller sind **Singletons** – `private User user` wird zwischen allen parallelen HTTP-Requests geteilt. Wenn User A einloggt und gleichzeitig User B eine Bestellung aufgibt, landet User B's Bestellung unter User A's Account.

**Betroffene Methoden die `this.user` referenzieren:**
- `seachHandler()` (Zeile 93–101)
- `orderHandler()` (Zeile 197–207)
- `back()` (Zeile 209–214)

### application.properties
Keine einzige Session/Cookie-Konfiguration vorhanden. Aktueller Stand nur:
```properties
# Nur diese 7 Zeilen existieren:
spring.datasource.*=...
spring.jpa.hibernate.ddl-auto=update
server.port=2330
```

---

## Aufgaben

### 3a – Race Condition beheben: Controller-Instanzfeld entfernen

- [ ] `private String email` und `private User user` aus `AdminController.java` entfernen
- [ ] Alle Methoden die `this.user` nutzen auf `Authentication` / `Principal` umstellen

```java
// Vorher – GEFÄHRLICH (AdminController.java)
private User user; // Singleton-Feld!

@PostMapping("/product/order")
public String orderHandler(@ModelAttribute Orders order, Model model) {
    order.setUser(user); // Welcher User ist das gerade?!
    ...
}

// Nachher – SICHER: User aus Spring Security Principal holen
@PostMapping("/product/order")
public String orderHandler(@ModelAttribute Orders order,
                           @AuthenticationPrincipal UserDetails principal,
                           Model model) {
    User currentUser = userServices.getUserByEmail(principal.getUsername());
    order.setUser(currentUser);
    ...
}
```

```java
// Alternative: Principal-Parameter nutzen
@PostMapping("/product/search")
public String searchHandler(@RequestParam String productName,
                            Principal principal, Model model) {
    User currentUser = userServices.getUserByEmail(principal.getName());
    List<Orders> orders = orderServices.getOrdersForUser(currentUser);
    model.addAttribute("orders", orders);
    ...
}
```

---

### 3b – Session-Konfiguration in application.properties

- [ ] Session-Timeout, HttpOnly, Secure und SameSite konfigurieren

```properties
# application.properties – ergänzen
# Session
server.servlet.session.timeout=30m
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=Lax
```

> **Fallstrick:** `secure=true` bedeutet Cookie wird NUR über HTTPS gesendet. Bei lokalem HTTP-Entwicklungsserver (localhost) kann der Cookie dann nicht gesetzt werden → für lokale Entwicklung auf `false` setzen, für Produktion auf `true`.

---

### 3c – Session Management in SecurityConfig

- [ ] Session Fixation Protection aktivieren
- [ ] Maximale gleichzeitige Sessions begrenzen
- [ ] `HttpSessionEventPublisher` Bean für `maximumSessions(1)` registrieren

```java
// SecurityConfig.java – sessionManagement Block
.sessionManagement(session -> session
    .sessionFixation().migrateSession()  // Neue Session-ID nach Login
    .maximumSessions(1)                  // Nur 1 gleichzeitige Session pro User
    .maxSessionsPreventsLogin(false)     // false = alte Session wird verdrängt
)
```

```java
// SecurityConfig.java – zusätzliche Bean für maximumSessions
@Bean
public HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
}
```

> **Fallstrick:** `maximumSessions(1)` funktioniert nur wenn `HttpSessionEventPublisher` als Bean registriert ist. Sonst werden Sessions nicht korrekt gezählt.

---

### 3d – Logout korrekt konfigurieren

- [ ] In `SecurityConfig.filterChain()` Logout konfigurieren (bereits in Phase 2 angelegt)
- [ ] Thymeleaf-Template: Logout per POST-Formular (kein GET-Link!)

```java
// SecurityConfig.java – logout Block
.logout(logout -> logout
    .logoutUrl("/logout")
    .logoutSuccessUrl("/login?logout=true")
    .invalidateHttpSession(true)       // Session wird ungültig
    .deleteCookies("JSESSIONID")       // Cookie wird gelöscht
    .clearAuthentication(true)
    .permitAll()
)
```

```html
<!-- Navigation.html – Logout als POST-Formular, NICHT als <a href> Link! -->
<form th:action="@{/logout}" method="post" sec:authorize="isAuthenticated()">
    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
    <button type="submit">Logout</button>
</form>
```

> **Fallstrick:** Spring Security blockiert GET-Requests auf `/logout` standardmässig. Logout MUSS per POST erfolgen (CSRF-Schutz). Kein einfacher `<a href="/logout">` verwenden!

---

### 3e – Session Fixation Protection verstehen (für Nachweis)

Session Fixation = Angreifer setzt eine Session-ID vor dem Login. Nach `migrateSession()` wird bei erfolgreichen Login eine **neue Session-ID** vergeben, die alte wird ungültig.

```
Angriff ohne Schutz:
1. Angreifer kennt Session-ID "ABC123"
2. Opfer loggt sich mit "ABC123" ein
3. Angreifer ist auch eingeloggt mit "ABC123"

Mit migrateSession():
1. Angreifer kennt Session-ID "ABC123"
2. Opfer loggt sich ein → neue ID "XYZ789" wird vergeben
3. Angreifer kann "ABC123" nicht mehr nutzen ✓
```

---

## Test-Szenarien für Abnahme

| Test | Erwartetes Ergebnis |
|---|---|
| Login → Browser DevTools → Application → Cookies | `JSESSIONID` mit `HttpOnly`-Flag sichtbar |
| 30 Min warten (oder Timeout kürzer setzen zum Testen) | Session abgelaufen, Redirect zu Login |
| 2x einloggen (2 Browser) | Erste Session wird verdrängt |
| Logout klicken → Back-Button im Browser | Weiterleitung zu Login-Seite (Session ungültig) |
| Session-ID vor Login notieren, nach Login vergleichen | Neue ID nach Login (Session Fixation Protection) |

> **Hinweis für Abnahme:** `secure=true` auf localhost zeigen kann schwierig sein (HTTP). Entweder HTTPS lokal mit self-signed Cert konfigurieren oder in `application.properties` erklären und Screenshot der Konfiguration zeigen.

---

## Fallstricke (Zusammenfassung)

- Race Condition durch `private User user` in Singleton-Controller ist der kritischste Bug dieser Phase
- `secure=true` bricht Cookie bei HTTP → lokal auf `false`, in Doku erklären warum es `true` sein soll
- Logout MUSS POST sein (nicht GET)
- `HttpSessionEventPublisher` für `maximumSessions(1)` nicht vergessen

---

## Abnahme-Kriterium: Bewertungskriterium 5 – Session-Management (Gewichtung 0.2)

**Nachweis:** Cookie-Flags im Browser zeigen, Logout testen, Timeout testen

- [ ] `HttpOnly`-Flag auf `JSESSIONID` im Browser-DevTools sichtbar
- [ ] Session wird bei Logout invalidiert (Back-Button funktioniert nicht mehr)
- [ ] Session Fixation Protection: neue Session-ID nach Login
- [ ] Nachweis: https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html
