# Phase 0 – Projekt-Setup & Lauffähigkeit

## Ziel
Das geklonte Open-Source-Projekt auf eine aktuelle Java- und Spring-Boot-Version bringen, die Datenbankverbindung konfigurieren und sicherstellen, dass alle Basisfunktionen stabil laufen – als sauberer Ausgangspunkt für alle Sicherheitsphasen.

---

## IST-Zustand (analysiert)

| Datei | Befund |
|---|---|
| `pom.xml` | Spring Boot `3.1.3`, Java `17`, keine Security-Dependency |
| `application.properties` | DB-User `root`/`root`, Port `2330`, `ddl-auto=update` |
| `HELP.md` | Dokumentiert JVM-Wechsel 11 → 17 (bereits vollzogen) |
| Ziel laut `PLAN.md` | Java 25, aktuelle Spring Boot Version |

---

## Aufgaben

### 1. Spring Boot Version updaten
- [ ] `pom.xml` → `spring-boot-starter-parent` Version auf `3.4.x` (aktuellste stabile Version prüfen: https://spring.io/projects/spring-boot)
- [ ] **Warum:** Spring Boot 3.1.3 ist nicht mehr im Support. Java 25 wird erst ab 3.3+ unterstützt.

```xml
<!-- pom.xml – Zeile 9 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.3</version> <!-- aktuelle Version prüfen! -->
    <relativePath />
</parent>
```

> **Fallstrick:** Nach Update `mvn dependency:resolve` laufen lassen. Hibernate-Version NICHT manuell überschreiben – wird durch Spring Boot Parent gesteuert.

---

### 2. Java-Version auf 25 anheben
- [ ] `pom.xml` → `<java.version>17</java.version>` auf `25` ändern
- [ ] Lokales JDK 25 prüfen: `java -version` → muss `25` zurückgeben
- [ ] Falls nicht installiert: https://jdk.java.net/25/

```xml
<properties>
    <java.version>25</java.version>
</properties>
```

> **Fallstrick:** Reihenfolge wichtig – erst Spring Boot updaten (Aufgabe 1), dann Java-Version setzen.

---

### 3. MySQL-Datenbank anlegen (XAMPP)
- [ ] XAMPP starten → MySQL starten
- [ ] phpMyAdmin öffnen oder MySQL-CLI nutzen
- [ ] Datenbank `businessproject` anlegen

```sql
CREATE DATABASE IF NOT EXISTS businessproject
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

> **Fallstrick:** `ddl-auto=update` legt Tabellen an, aber NICHT die Datenbank selbst. DB muss manuell erstellt werden.

---

### 4. `application.properties` prüfen

- [ ] Passwort anpassen falls lokales MySQL-root-Passwort abweicht
- [ ] Port 2330 muss frei sein (`ss -tlnp | grep 2330` auf Linux)

```properties
# Aktueller Ist-Zustand (src/main/resources/application.properties)
spring.datasource.name=businessproject
spring.datasource.url=jdbc:mysql://localhost:3306/businessproject
spring.datasource.username=root
spring.datasource.password=root         # ← anpassen falls nötig
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
server.port=2330
```

> **Hinweis:** `root` als DB-User bleibt vorerst so – wird in Phase 5 durch einen eingeschränkten User ersetzt. `ddl-auto=update` bleibt für Entwicklung, wird in Phase 7 auf `validate` gesetzt.

---

### 5. Projekt kompilieren
- [ ] `mvn clean compile` ausführen – muss fehlerfrei durchlaufen
- [ ] Bei Fehlern: Compile-Output vollständig lesen, gezielt beheben

```bash
mvn clean compile
# oder vollständiger Build:
mvn clean package -DskipTests
```

---

### 6. Applikation starten & erreichbar
- [ ] `mvn spring-boot:run` oder JAR starten
- [ ] `http://localhost:2330` im Browser öffnen
- [ ] Konsole zeigt: `Started BusinessProjectApplication` und `Tomcat started on port(s): 2330`

---

### 7. Alle Basisfunktionen testen (Checkliste)
- [ ] User-Registrierung funktioniert
- [ ] User-Login funktioniert
- [ ] Produktliste wird angezeigt
- [ ] Bestellung aufgeben funktioniert
- [ ] Admin-Login funktioniert (Admin muss manuell in DB angelegt werden!)
- [ ] Admin: User anlegen / bearbeiten / löschen
- [ ] Admin: Admin-Account verwalten
- [ ] Admin: Produkt anlegen / bearbeiten / löschen
- [ ] Logout beendet Session korrekt

> **Fallstrick:** Beim ersten Start legt Hibernate Tabellen an. Admin-Datensatz existiert noch nicht → muss manuell per SQL oder phpMyAdmin eingefügt werden (Klartext-Passwort, da noch kein Hashing).

---

### 8. IST-Zustand Entitäten dokumentieren (Vorbereitung Phase 1)
- [ ] Notieren was in Phase 1 geändert werden muss (nicht jetzt ändern!)

| Entität | Problem |
|---|---|
| `User` | `int u_id` statt UUID, keine Validierungsannotationen, `toString()` gibt Passwort aus |
| `Admin` | `int adminId` statt UUID, `@Value("1234")` falsch verwendet, `toString()` gibt Passwort aus |
| `Orders` | `double` für Preise (soll `BigDecimal`), Tippfehler `totalAmmout`, kein `@PrePersist` |
| `Product` | Keine einzige Validierungsannotation |

---

## Fallstricke (Zusammenfassung)

- `ddl-auto=update` löscht nie Felder → bei Schema-Konflikten DB löschen und neu anlegen
- `UserLogin.toString()` gibt Passwort in Logs aus → Logging in Produktion gefährlich (wird in Phase 1 behoben)
- Port 2330 muss auf allen Entwicklungsrechnern gleich sein

---

## Abnahme-Kriterium: Bewertungskriterium 1 – Funktionsfähigkeit (Gewichtung 0.2)

**Nachweis:** Live-Demo

Die Phase gilt als abgeschlossen wenn:
- [ ] Projekt kompiliert fehlerfrei mit aktuellem Java + Spring Boot
- [ ] Applikation startet ohne Fehler auf Port 2330
- [ ] Alle 9 Funktionen aus der Testcheckliste laufen ohne HTTP 500
- [ ] Kein unbehandelter Fehler im Browser-Netzwerk-Tab sichtbar
