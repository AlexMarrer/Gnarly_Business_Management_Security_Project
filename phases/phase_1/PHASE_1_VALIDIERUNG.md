# Phase 1 – Datenmodell & Validierung

## Ziel
Alle Entitäten mit korrekten Typen (UUID statt int), Bean Validation Annotationen und sicheren toString()-Methoden ausstatten. Controller um `@Valid` erweitern. Client- und serverseitige Validierung lückenlos sicherstellen.

---

## IST-Zustand (analysiert)

### User.java – kritische Befunde
- `int u_id` mit `GenerationType.IDENTITY` → soll UUID werden
- Kein einziges `@NotBlank`, `@Size`, `@Email`, `@Pattern` auf irgendeinem Feld
- `Long unumber` → blockiert `@Pattern`-Regex, soll `String` werden
- `toString()` Zeile 71–72: gibt `upassword` im Klartext aus → Logging-Gefahr!
- Ungenutzter Import: `org.springframework.beans.factory.annotation.Value`

### Admin.java – kritische Befunde
- `int adminId`, keine Validierungsannotationen
- `@Value("1234")` als Feld-Annotation (falsche Annotation, hat keinen Effekt in JPA)
- Validation-Imports (`@NotNull`, `@Size`, `@Email`) vorhanden aber nicht verwendet
- `toString()` gibt `adminPassword` im Klartext aus

### Orders.java – Befunde
- `double` für `oPrice` und `totalAmmout` → soll `BigDecimal` (Präzision bei Währungen!)
- Tippfehler: `totalAmmout` → soll `totalAmount`
- `orderDate` wird manuell im Controller gesetzt → soll `@PrePersist` nutzen
- `toString()` würde rekursiv über `user` iterieren

### Product.java – Befunde
- Keine einzige Validierungsannotation

### UserController.java – Befunde
- `addUser(@ModelAttribute User user)` → kein `@Valid`, kein `BindingResult`
- `System.out.println(user)` in Zeile 22 → loggt Klartext-Passwort!
- `updatingUser` als `@GetMapping` → State-Change per GET (falsch)

### AdminController.java – Befunde
- `@Valid` und `BindingResult` sind importiert (Zeile 11, 30) aber nirgends verwendet
- `addAdmin`, `orderHandler` → keine Validierung
- `System.out.println(product)` in Zeile 175 → loggt Produktdaten

### Doppelte Mappings (Konflikt-Risiko!)
- `@PostMapping("/addingUser")` existiert in **beiden** `UserController.java` und wird von `AdminController` Formular aufgerufen
- `@PostMapping("/addingProduct")` existiert in **beiden** `ProductController.java` und wird von `AdminController` Formular aufgerufen
- `@GetMapping("/updatingUser/{id}")`, `/deleteUser/{id}`, `/updatingProduct/{id}`, `/deleteProduct/{id}` → ebenfalls doppelt
- **Lösung:** CRUD-Logik nur in `UserController`/`ProductController` belassen, aus `AdminController` die Duplikate entfernen (AdminController hat die Formular-Seiten-Aufrufe, die eigentliche Logik ist in den spezifischen Controllern)

### HTML-Templates – Befunde
- `Add_User.html`: Passwort hardcoded als `value="2330"` in hidden field
- `Add_Admin.html`: Passwort hardcoded als `value="1234"` in hidden field
- `Login.html`: `maxlength="8"` auf Passwort-Feld → blockiert starke Passwörter!
- `Add_Product.html`: Preis als `type="text"`, kein `required`

---

## Aufgaben

### 1. UUID-IDs für alle Entitäten

- [ ] `User.java`: `int u_id` → `UUID id`
- [ ] `Admin.java`: `int adminId` → `UUID id`
- [ ] `Orders.java`: `int oId` → `UUID id`
- [ ] `Product.java`: `int pid` → `UUID id`

```java
// Vorher (User.java)
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private int u_id;

// Nachher
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

> **Fallstrick:** Alle Controller und Services die `int id` als Parameter nehmen müssen auf `UUID id` umgestellt werden. Path-Variablen in URLs bleiben als String, werden per `UUID.fromString(id)` konvertiert.

---

### 2. Bean Validation auf User-Entität

- [ ] `spring-boot-starter-validation` Dependency in `pom.xml` prüfen (bereits vorhanden laut pom.xml)
- [ ] `User.java` vollständig mit Annotationen ausstatten

```java
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Benutzername ist erforderlich")
    @Size(min = 2, max = 50, message = "Benutzername: 2–50 Zeichen")
    @Column(length = 50, nullable = false)
    private String uname;

    @NotBlank(message = "E-Mail ist erforderlich")
    @Email(message = "Ungültiges E-Mail-Format")
    @Column(length = 255, nullable = false, unique = true)
    private String uemail;

    @NotBlank(message = "Passwort ist erforderlich")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Passwort: min. 8 Zeichen, Groß+Klein+Zahl+Sonderzeichen"
    )
    @Column(length = 255, nullable = false)
    private String upassword;

    @NotBlank(message = "Telefonnummer ist erforderlich")
    @Pattern(regexp = "^\\d{10,15}$", message = "Telefonnummer: 10–15 Ziffern")
    @Column(length = 20, nullable = false)
    private String unumber; // Typ: String (nicht mehr Long!)
}
```

---

### 3. Bean Validation auf Admin-Entität

- [ ] Analog zu User – gleiche Felder, gleiche Annotationen
- [ ] `@Value("1234")` entfernen (falsche Annotation!)
- [ ] Felder auf korrekte Typen anpassen

---

### 4. Bean Validation auf Orders-Entität

- [ ] `double` → `BigDecimal` für `oPrice` und `totalAmount`
- [ ] Tippfehler `totalAmmout` → `totalAmount` korrigieren
- [ ] `@Min`, `@Max`, `@DecimalMin` hinzufügen
- [ ] `@PrePersist` für `orderDate` nutzen statt manuell im Controller setzen

```java
@NotBlank(message = "Produktname ist erforderlich")
@Size(min = 2, max = 100)
private String oName;

@NotNull
@DecimalMin(value = "0.01", message = "Preis muss mindestens 0.01 sein")
@Digits(integer = 9, fraction = 2)
private BigDecimal oPrice;

@NotNull
@Min(value = 1, message = "Mindestmenge: 1")
@Max(value = 999, message = "Maximalmenge: 999")
private Integer oQuantity;

@PrePersist
protected void onCreate() {
    this.orderDate = LocalDate.now();
}
```

---

### 5. Bean Validation auf Product-Entität

- [ ] `@NotBlank`, `@Size` für Name und Beschreibung
- [ ] `BigDecimal` für Preis, `@DecimalMin`

```java
@NotBlank(message = "Produktname ist erforderlich")
@Size(min = 2, max = 100)
private String pname;

@NotBlank(message = "Beschreibung ist erforderlich")
@Size(max = 500)
private String pdescription;  // Achtung: aktueller Feldname ist pdescription, NICHT pdesc

@NotNull
@DecimalMin("0.01")
@Digits(integer = 9, fraction = 2)
private BigDecimal pprice;
```

---

### 6. toString() säubern (Logging-Schutz)

- [ ] `User.toString()`: `upassword` aus der Ausgabe entfernen
- [ ] `Admin.toString()`: `adminPassword` aus der Ausgabe entfernen
- [ ] `Orders.toString()`: User-Objekt nicht ausgeben (verhindert Rekursion)

```java
// Vorher – GEFÄHRLICH (User.java Zeile 71)
return "User [u_id=" + u_id + ", uname=" + uname + ", uemail=" + uemail
        + ", upassword=" + upassword + ...]

// Nachher – SICHER
return "User [id=" + id + ", uname=" + uname + ", uemail=" + uemail + "]";
// Passwort NIEMALS in toString()!
```

---

### 7. @Valid in Controller-Methoden

- [ ] `UserController.addUser()`: `@Valid` hinzufügen, `BindingResult` Parameter ergänzen
- [ ] `UserController.updateUser()`: auf `@PostMapping` umstellen + `@Valid`
- [ ] `AdminController.addAdmin()`: `@Valid` + `BindingResult`
- [ ] `AdminController.orderHandler()`: `@Valid` + `BindingResult`
- [ ] `System.out.println(user)` in `UserController.java:22` → entfernen!

```java
// Vorher
@PostMapping("/addingUser")
public String addUser(@ModelAttribute User user) {
    System.out.println(user); // KLARTEXT-PASSWORT IN LOGS!
    this.services.addUser(user);
    return "redirect:/admin/services";
}

// Nachher
@PostMapping("/addingUser")
public String addUser(@Valid @ModelAttribute("user") User user,
                      BindingResult result, Model model) {
    if (result.hasErrors()) {
        return "Add_User"; // Zurück zum Formular mit Fehlermeldungen
    }
    this.services.addUser(user);
    return "redirect:/admin/services";
}
```

---

### 8. HTML5-Validierung in Thymeleaf-Templates

- [ ] `Login.html`: `maxlength="8"` auf Passwort-Feld entfernen → auf `minlength="8"` ändern
- [ ] `Add_User.html`: hardcoded `value="2330"` entfernen, `required`, `minlength`, `pattern` hinzufügen
- [ ] `Add_Admin.html`: hardcoded `value="1234"` entfernen
- [ ] `Add_Product.html`: Preis auf `type="number"`, `min="0.01"`, `step="0.01"`, `required`
- [ ] Alle Eingabefelder: `required` Attribut wo Pflichtfeld

```html
<!-- Passwort-Feld – Vorher -->
<input type="password" name="upassword" maxlength="8">

<!-- Passwort-Feld – Nachher -->
<input type="password" name="upassword"
       minlength="8"
       pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).{8,}$"
       title="Mindestens 8 Zeichen: Groß, Klein, Zahl, Sonderzeichen"
       required>
```

---

## Fallstricke

- `@Pattern` funktioniert nur auf `String` – deshalb `unumber` von `Long` auf `String` ändern
- `BigDecimal` für Geldbeträge ist Pflicht – `double`/`float` haben Rundungsfehler
- `@PrePersist` wird nur beim ersten Speichern ausgelöst – für Updates `@PreUpdate`
- UUID als Path-Variable: Immer mit try-catch um `UUID.fromString()` wrappen
- Nach UUID-Umstieg müssen alle existierenden DB-Einträge gelöscht werden (Schema-Änderung)

---

## Abnahme-Kriterium: Bewertungskriterium 1 & 6 (Injection-Schutz via Validierung)

- [ ] Alle 4 Entitäten haben vollständige Bean Validation Annotationen
- [ ] Alle Controller-Methoden haben `@Valid` und `BindingResult`
- [ ] Kein Passwort taucht in Logs auf (`toString()` bereinigt, `println` entfernt)
- [ ] Client- UND Serverseitige Validierung vorhanden und testbar
