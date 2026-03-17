# Phase 4 – Autorisierung (Frontend + Backend)

## Ziel
Alle Endpunkte rollenbasiert absichern – sowohl auf Backend-Ebene (`@PreAuthorize`, URL-Regeln) als auch im Frontend (Thymeleaf `sec:authorize`). ⚠️ **DOPPELT GEWICHTET**

---

## IST-Zustand (analysiert) – KRITISCHE LÜCKEN

| Befund | Detail |
|---|---|
| Keine Spring Security Dependency | `pom.xml` – wird in Phase 2 hinzugefügt |
| Kein `SecurityConfig.java` | Existiert nicht – alle Endpunkte öffentlich |
| `@GetMapping("/admin/services")` | Vollständig offen – kein Login nötig |
| Keine `@PreAuthorize`-Annotationen | Auf keinem einzigen Controller |
| Navigation.html | Kein `xmlns:sec`, kein `sec:authorize` |
| Alle 24 Endpunkte | Ohne Rollenschutz |

---

## Alle Endpunkte und ihre Ziel-Rollen

| Endpunkt | Methode | Aktuelle Rolle | Ziel-Rolle |
|---|---|---|---|
| `/home` | GET | OFFEN | `permitAll` |
| `/login` | GET/POST | OFFEN | `permitAll` |
| `/register` | GET/POST | OFFEN | `permitAll` |
| `/adminLogin` | GET | OFFEN | **ENTFERNEN** (Phase 2) |
| `/userlogin` | GET | OFFEN | **ENTFERNEN** (Phase 2) |
| `/admin/services` | GET | OFFEN | `ROLE_ADMIN` |
| `/addAdmin` | GET | OFFEN | `ROLE_ADMIN` |
| `/addingAdmin` | POST | OFFEN | `ROLE_ADMIN` |
| `/updateAdmin/{id}` | GET | OFFEN | `ROLE_ADMIN` |
| `/updatingAdmin/{id}` | GET | OFFEN | `ROLE_ADMIN` |
| `/deleteAdmin/{id}` | GET | OFFEN | `ROLE_ADMIN` |
| `/addProduct` | GET | OFFEN | `ROLE_ADMIN` |
| `/updateProduct/{id}` | GET | OFFEN | `ROLE_ADMIN` |
| `/addUser` | GET | OFFEN | `ROLE_ADMIN` |
| `/addingUser` | POST | OFFEN | `ROLE_ADMIN` |
| `/updatingUser/{id}` | GET | OFFEN | `ROLE_ADMIN` |
| `/deleteUser/{id}` | GET | OFFEN | `ROLE_ADMIN` |
| `/updateUser/{id}` | GET | OFFEN | `ROLE_ADMIN` |
| `/product/search` | POST | OFFEN | `ROLE_USER` |
| `/product/order` | POST | OFFEN | `ROLE_USER` |
| `/product/back` | GET | OFFEN | `ROLE_USER` |
| `/addingProduct` | POST | OFFEN | `ROLE_ADMIN` |
| `/updatingProduct/{id}` | GET | OFFEN | `ROLE_ADMIN` |
| `/deleteProduct/{id}` | GET | OFFEN | `ROLE_ADMIN` |

---

## Aufgaben

### 4a – URL-Absicherung in SecurityConfig

- [ ] `SecurityConfig.java` (aus Phase 2) um vollständige URL-Regeln erweitern

```java
// SecurityConfig.java – authorizeHttpRequests Block
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/login", "/register", "/home",
                     "/css/**", "/js/**", "/images/**").permitAll()
    .requestMatchers("/admin/**", "/addAdmin", "/addingAdmin",
                     "/updateAdmin/**", "/updatingAdmin/**", "/deleteAdmin/**",
                     "/addProduct", "/addingProduct", "/updateProduct/**",
                     "/updatingProduct/**", "/deleteProduct/**",
                     "/addUser", "/addingUser", "/updateUser/**",
                     "/updatingUser/**", "/deleteUser/**").hasRole("ADMIN")
    .requestMatchers("/product/**").hasRole("USER")
    .anyRequest().authenticated()
)
```

---

### 4b – @PreAuthorize auf AdminController

- [ ] `@EnableMethodSecurity` in `SecurityConfig` aktivieren (bereits im Template)
- [ ] Alle Admin-Methoden mit `@PreAuthorize` absichern

```java
// AdminController.java
@Controller
public class AdminController {

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/services")
    public String returnBack(Model model) { ... }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/addingAdmin")
    public String addAdmin(@Valid @ModelAttribute Admin admin, BindingResult result) { ... }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/deleteAdmin/{id}")
    public String deleteAdmin(@PathVariable UUID id) { ... }

    // Alle weiteren Admin-Methoden analog annotieren
}
```

---

### 4c – @PreAuthorize auf UserController & ProductController

- [ ] `UserController.java`: alle Methoden mit `@PreAuthorize("hasRole('ADMIN')")`
- [ ] `ProductController.java`: Add/Update/Delete → `ADMIN`, Search → `USER`

```java
// UserController.java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/addingUser")
public String addUser(@Valid @ModelAttribute User user, BindingResult result) { ... }

// ProductController.java oder AdminController
@PreAuthorize("hasRole('USER')")
@PostMapping("/product/search")
public String searchHandler(...) { ... }

@PreAuthorize("hasRole('USER')")
@PostMapping("/product/order")
public String orderHandler(...) { ... }
```

---

### 4d – Thymeleaf Frontend-Absicherung

- [ ] `xmlns:sec` Namespace zu allen relevanten Templates hinzufügen
- [ ] `sec:authorize` für bedingte Sichtbarkeit nutzen

```html
<!-- Alle Templates die sec:authorize nutzen – Namespace hinzufügen -->
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">

<!-- Navigation.html – Rollenbasierte Links -->
<nav>
    <!-- Nur für Admins sichtbar -->
    <a th:href="@{/admin/services}" sec:authorize="hasRole('ADMIN')">
        Admin-Panel
    </a>

    <!-- Nur für eingeloggte User -->
    <a th:href="@{/product/search}" sec:authorize="hasRole('USER')">
        Produkte kaufen
    </a>

    <!-- Aktuell eingeloggter User anzeigen -->
    <span sec:authorize="isAuthenticated()">
        Eingeloggt als: <b sec:authentication="name"></b>
    </span>

    <!-- Logout (nur wenn eingeloggt) -->
    <form th:action="@{/logout}" method="post" sec:authorize="isAuthenticated()">
        <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
        <button type="submit">Logout</button>
    </form>
</nav>
```

```html
<!-- Admin_Page.html – Admin-Formulare nur für Admins ausliefern -->
<div sec:authorize="hasRole('ADMIN')">
    <h2>Benutzerverwaltung</h2>
    <!-- CRUD-Tabellen und Buttons -->
</div>

<!-- Wichtig: sec:authorize verhindert nur Anzeige – Backend-Schutz (4b) ist Pflicht! -->
```

---

### 4e – Formulare werden nicht ausgeliefert (Abnahme-Anforderung!)

Die Abnahme prüft explizit: **"Allfällige Formulare werden gar nicht ausgeliefert"**

- [ ] Testen: Als User eingeloggt → URL `/addAdmin` direkt aufrufen → muss `403 Forbidden` zurückgeben, NICHT das Formular
- [ ] Testen: Ohne Login → URL `/admin/services` → Redirect zu `/login`, nicht die Seite

```java
// Das passiert durch die Kombination aus:
// 1. URL-Regel in SecurityConfig (.hasRole("ADMIN"))
// 2. @PreAuthorize("hasRole('ADMIN')") auf der Controller-Methode
// → Doppelte Absicherung!
```

---

## Fallstricke

- Frontend-Absicherung alleine reicht NICHT – `sec:authorize` ist nur für die Anzeige, nicht für den Zugriff
- URL-Regeln in SecurityConfig und `@PreAuthorize` zusammen = doppelte Sicherheit (beide implementieren!)
- `thymeleaf-extras-springsecurity6` Dependency muss in `pom.xml` vorhanden sein (Phase 2)
- Beim Umbau der Controller-Endpunkte: delete/update von GET auf POST umstellen (State-Changes per GET sind unsicher)
- `@EnableMethodSecurity` in `SecurityConfig` nicht vergessen – sonst werden `@PreAuthorize`-Annotationen ignoriert!
- Alte `AdminController.adminLogin()` und `userLogin()` Methoden nach Phase 2 entfernen – sonst konflikten die GET-Login-Endpunkte mit Spring Security

---

## Abnahme-Kriterium: Bewertungskriterium 2 – Rollen/Rechte ⚠️ DOPPELT GEWICHTET (0.4)

**Nachweis:** URL ohne Rechte direkt aufrufen, UI als verschiedene Rollen zeigen

- [ ] Als nicht eingeloggter User: `/admin/services` aufrufen → Redirect zu `/login`
- [ ] Als eingeloggter USER (nicht Admin): `/admin/services` aufrufen → `403 Forbidden`
- [ ] Als USER: Admin-CRUD-Buttons sind im Frontend NICHT sichtbar (`sec:authorize`)
- [ ] Als ADMIN: Alle Admin-Funktionen sichtbar und nutzbar
- [ ] Formular `/addAdmin` als USER direkt aufrufen → Formular wird NICHT ausgeliefert
- [ ] Nachweis: https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html
