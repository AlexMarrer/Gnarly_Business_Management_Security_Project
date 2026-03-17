# Phase 1 – Checkliste

## Aufgaben

### 1. UUID-IDs für alle Entitäten
- [x] `User.java`: `int u_id` → `UUID id` mit `GenerationType.UUID`
- [x] `Admin.java`: `int adminId` → `UUID id` mit `GenerationType.UUID`
- [x] `Orders.java`: `int oId` → `UUID id` mit `GenerationType.UUID`
- [x] `Product.java`: `int pid` → `UUID id` mit `GenerationType.UUID`
- [x] Alle Repositories: Typparameter `Integer` → `UUID`
- [x] Alle Services: Parameter `int id` → `UUID id`
- [x] Alle Controller: PathVariable `int id` → `UUID id`
- **Bemerkung:** DB muss nach diesem Umbau gelöscht und neu angelegt werden (`ddl-auto=update` kann int-Spalten nicht in UUID umwandeln).

### 2. Bean Validation auf User-Entität
- [x] `@NotBlank` auf `uname`, `uemail`, `upassword`, `unumber`
- [x] `@Size(min=2, max=50)` auf `uname`
- [x] `@Email` auf `uemail`
- [x] `@Pattern` (Komplexität) auf `upassword`
- [x] `@Pattern(regexp="^\\d{10,15}$")` auf `unumber`
- [x] `Long unumber` → `String unumber` (nötig für `@Pattern`)
- [x] `@Column`-Annotationen mit `nullable=false`, `length`, `unique`
- **Bemerkung:** Passwort-Pattern verlangt min. 8 Zeichen mit Gross+Klein+Zahl+Sonderzeichen.

### 3. Bean Validation auf Admin-Entität
- [x] Gleiche Annotationen wie User: `@NotBlank`, `@Email`, `@Pattern`, `@Size`
- [x] `@Value("1234")` entfernt (hatte keinen sinnvollen Effekt in JPA)
- **Bemerkung:** Admin und User haben identische Passwort-Komplexitätsregeln.

### 4. Bean Validation auf Orders-Entität
- [x] `double oPrice` → `BigDecimal oPrice` mit `@DecimalMin`, `@Digits`
- [x] `double totalAmmout` → `BigDecimal totalAmount` (Tippfehler korrigiert!)
- [x] `int oQuantity` → `Integer oQuantity` mit `@Min(1)`, `@Max(999)`
- [x] `@NotBlank`/`@Size` auf `oName`
- [x] `java.util.Date` → `java.time.LocalDate` für `orderDate`
- [x] `@PrePersist` für automatisches Setzen von `orderDate`
- [x] `@JoinColumn(name="user_id")` aktualisiert (war `user_u_id`)
- **Bemerkung:** `Logic.countTotal()` ebenfalls auf BigDecimal umgestellt. AdminController.orderHandler() setzt orderDate nicht mehr manuell – wird jetzt via @PrePersist gesetzt.

### 5. Bean Validation auf Product-Entität
- [x] `@NotBlank`/`@Size` auf `pname` und `pdescription`
- [x] `double pprice` → `BigDecimal pprice` mit `@DecimalMin`, `@Digits`
- **Bemerkung:** Keine Preisberechnungen in Product, daher reiner Typ-Wechsel.

### 6. toString() säubern (Logging-Schutz)
- [x] `User.toString()`: `upassword` und `orders` entfernt
- [x] `Admin.toString()`: `adminPassword` und `adminNumber` entfernt
- [x] `Orders.toString()`: `user`-Objekt entfernt (verhindert Rekursion)
- [x] `AdminLogin.toString()`: `password` entfernt
- [x] `UserLogin.toString()`: `userPassword` entfernt
- **Bemerkung:** Auch `System.out.println(user)` in UserController (Zeile 22) und `System.out.println(product)` in AdminController (Zeile 175) wurden entfernt – diese loggten Klartext-Passwörter bzw. Produktdaten.

### 7. @Valid in Controller-Methoden
- [x] `UserController.addUser()`: `@Valid` + `BindingResult`, bei Fehlern zurück zu `Add_User`
- [x] `ProductController.addProduct()`: `@Valid` + `BindingResult`, bei Fehlern zurück zu `Add_Product`
- [x] `ProductController.updateProduct()`: `@Valid` + `BindingResult`, bei Fehlern zurück zu `Update_Product`
- [x] `AdminController.addAdmin()`: `@Valid` + `BindingResult`, bei Fehlern zurück zu `Add_Admin`
- [x] Controller-Methoden für Formularseiten geben jetzt leere Model-Attribute mit (`new User()`, `new Admin()`, `new Product()`) – nötig für `th:object` in Templates
- **Bemerkung:** Update-Methoden für User und Admin haben KEIN `@Valid`, da die Update-Formulare kein Passwort-Feld enthalten (würde sonst `@NotBlank`-Validierung auslösen). Stattdessen nutzen die Services einen Merge-Ansatz: bestehende Entity laden, nur geänderte Felder überschreiben.

### 8. HTML5-Validierung in Thymeleaf-Templates
- [x] `Login.html`: `maxlength="8"` entfernt, `minlength="8"` gesetzt
- [x] `Add_User.html`: Hidden-Passwort `value="2330"` entfernt, echtes Passwort-Feld mit Pattern-Validierung, `th:field`-Bindings, Fehleranzeige mit `th:errors`
- [x] `Add_Admin.html`: Hidden-Passwort `value="1234"` entfernt, echtes Passwort-Feld, Validierung, Fehleranzeige
- [x] `Add_Product.html`: Preis auf `type="number"` mit `min="0.01"`, `step="0.01"`, `required`
- [x] `Update_User.html`: ID-Referenzen aktualisiert (`u_id` → `id`), `method="post"` hinzugefügt
- [x] `Update_Admin.html`: ID-Referenzen aktualisiert (`adminId` → `id`), `method="post"` hinzugefügt
- [x] `Update_Product.html`: ID-Referenzen aktualisiert (`pid` → `id`), `method="post"` hinzugefügt
- [x] `Admin_Page.html`: Alle ID-Referenzen aktualisiert, `totalAmmout` → `totalAmount`
- [x] `BuyProduct.html`: `totalAmmout` → `totalAmount`, Thymeleaf-Namespace korrigiert (`.com` → `.org`)
- **Bemerkung:** Update-Formulare (User/Admin/Product) von GET auf POST umgestellt – State-Changes per GET sind ein Sicherheitsrisiko. Thymeleaf-Namespace war in 3 Templates falsch (`thymeleaf.com` statt `thymeleaf.org`).

---

## Abnahme-Kriterium
- [x] Alle 4 Entitäten haben vollständige Bean Validation Annotationen
- [x] Alle relevanten Controller-Methoden haben `@Valid` und `BindingResult`
- [x] Kein Passwort taucht in Logs auf (`toString()` bereinigt, `println` entfernt)
- [x] Client- UND serverseitige Validierung vorhanden
- [ ] Validierung testbar via Browser *(manueller Test nach DB-Setup)*
