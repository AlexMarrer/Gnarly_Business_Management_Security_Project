# Phase 8 – User-Registrierung

## Ziel
Self-Service-Registrierung für User implementieren. Aktuell können User nur durch einen Admin manuell erstellt werden – es gibt kein öffentliches Registrierungsformular.

---

## IST-Zustand

| Problem | Fundstelle | Auswirkung |
|---|---|---|
| Kein Registrierungsformular | Templates-Ordner | User können sich nicht selbst registrieren |
| Toter Link "Register Here" | `Login.html` (alte Version) | Verwies auf `register.html`, die nie existierte |
| User-Erstellung nur via Admin-Panel | `AdminController.addUser()` → `Add_User.html` | Admin muss jeden User manuell anlegen |

---

## Aufgaben

### 8a – Registrierungsformular erstellen

- [ ] Neues Template: `src/main/resources/templates/Register.html`
- [ ] Felder: Name, E-Mail, Passwort, Passwort-Bestätigung, Telefonnummer
- [ ] Client-seitige Validierung (HTML5 `required`, `pattern`, `minlength`)
- [ ] Server-seitige Fehleranzeige mit `th:errors`
- [ ] Link zurück zur Login-Seite

```html
<!-- Register.html – Grundgerüst -->
<form th:action="@{/register}" th:object="${user}" method="post">
    Name:
    <input type="text" th:field="*{uname}" required minlength="2" maxlength="50"
           placeholder="Benutzername..." />
    <div th:if="${#fields.hasErrors('uname')}" th:errors="*{uname}" style="color:red;"></div>

    E-Mail:
    <input type="email" th:field="*{uemail}" required placeholder="E-Mail..." />
    <div th:if="${#fields.hasErrors('uemail')}" th:errors="*{uemail}" style="color:red;"></div>

    Passwort:
    <input type="password" th:field="*{upassword}" required minlength="8"
           pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).{8,}$"
           title="Min. 8 Zeichen: Gross, Klein, Zahl, Sonderzeichen"
           placeholder="Passwort..." />
    <div th:if="${#fields.hasErrors('upassword')}" th:errors="*{upassword}" style="color:red;"></div>

    Passwort bestätigen:
    <input type="password" name="passwordConfirm" required minlength="8"
           placeholder="Passwort wiederholen..." />
    <div th:if="${passwordMismatch}" th:text="${passwordMismatch}" style="color:red;"></div>

    Telefonnummer:
    <input type="tel" th:field="*{unumber}" required pattern="\d{10,15}"
           title="10-15 Ziffern" placeholder="Telefonnummer..." />
    <div th:if="${#fields.hasErrors('unumber')}" th:errors="*{unumber}" style="color:red;"></div>

    <button type="submit">Registrieren</button>
    <p>Bereits registriert? <a th:href="@{/login}">Hier einloggen</a></p>
</form>
```

---

### 8b – RegistrationController erstellen

- [ ] Neue Datei: `src/main/java/com/business/controllers/RegistrationController.java`
- [ ] `GET /register` → Formular anzeigen (mit leerem User-Objekt)
- [ ] `POST /register` → Validierung, Passwort-Bestätigung, Duplikat-Check, User anlegen

```java
@Controller
public class RegistrationController {

    @Autowired
    private UserServices userServices;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "Register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult result,
                               @RequestParam("passwordConfirm") String passwordConfirm,
                               Model model) {
        // Passwort-Bestätigung prüfen
        if (!user.getUpassword().equals(passwordConfirm)) {
            model.addAttribute("passwordMismatch", "Passwörter stimmen nicht überein");
            return "Register";
        }
        if (result.hasErrors()) {
            return "Register";
        }
        // Duplikat-E-Mail prüfen
        if (userServices.getUserByEmail(user.getUemail()) != null) {
            model.addAttribute("emailExists", "Diese E-Mail ist bereits registriert");
            return "Register";
        }
        userServices.addUser(user); // Passwort wird im Service gehasht (BCrypt+Pepper)
        return "redirect:/login?registered=true";
    }
}
```

> **Fallstrick:** `@Valid` prüft Bean Validation BEVOR das Passwort gehasht wird – Reihenfolge ist korrekt. Die Passwort-Bestätigung (`passwordConfirm`) ist KEIN Feld der User-Entity – sie kommt als separater `@RequestParam`.

---

### 8c – SecurityConfig anpassen

- [ ] `/register` zu den öffentlichen Endpunkten hinzufügen

```java
// SecurityConfig.java – permitAll ergänzen
.requestMatchers("/", "/login", "/register", "/home", ...)
    .permitAll()
```

---

### 8d – Login-Seite: Link zu Registrierung + Erfolgsmeldung

- [ ] In `Login.html` Link zu `/register` hinzufügen
- [ ] Erfolgsmeldung nach Registrierung anzeigen

```html
<!-- Nach dem Login-Formular -->
<p>Noch kein Konto? <a th:href="@{/register}" style="color: green;">Hier registrieren</a></p>

<div th:if="${param.registered}" style="color: green; margin-top: 5px;">
    Registrierung erfolgreich! Bitte einloggen.
</div>
```

---

### 8e – CSS (optional)

- [ ] `src/main/resources/static/css/Register.css` erstellen
- [ ] Kann `Login.css` oder `Add_User.css` als Vorlage nutzen

---

## Fallstricke

- Passwort-Bestätigung ist ein reines Formular-Feld – NICHT in der Entity speichern
- E-Mail-Duplikat-Check verhindert mehrfache Registrierung mit gleicher Adresse
- CSRF-Token wird durch `th:action` automatisch eingefügt
- Nach Registrierung wird der User NICHT automatisch eingeloggt → muss sich manuell anmelden
- Die Passwort-Validierung (`@Pattern` auf der Entity) greift VOR dem Hashing – das ist gewollt

---

## Abnahme-Kriterium

- [ ] Registrierungsformular erreichbar unter `/register`
- [ ] Validierung: Name, E-Mail, Passwort-Komplexität, Telefonnummer
- [ ] Passwort-Bestätigung wird geprüft
- [ ] Duplikat-E-Mail wird abgefangen
- [ ] Nach Registrierung: Redirect zu Login mit Erfolgsmeldung
- [ ] Neuer User kann sich einloggen
- [ ] Passwort in DB als BCrypt-Hash gespeichert (`$2a$12$...`)
