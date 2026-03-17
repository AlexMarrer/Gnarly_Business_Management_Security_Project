# Phase 0 – Checkliste

## Aufgaben

### 1. Spring Boot Version updaten
- [x] `pom.xml` → `spring-boot-starter-parent` auf `3.4.3` gesetzt
- **Bemerkung:** Update von 3.1.3 auf 3.4.3 verlief problemlos, keine Breaking Changes.

### 2. Java-Version anheben
- [x] `pom.xml` → `<java.version>` angepasst
- **Bemerkung:** Auf `21` gesetzt (installiertes JDK: OpenJDK 21.0.10). Phase-Doku verlangt Java 25 – sobald JDK 25 installiert ist, in `pom.xml` auf `25` ändern.

### 3. MySQL-Datenbank anlegen
- [ ] Datenbank `businessproject` anlegen (XAMPP/MySQL)
- **Bemerkung:** Muss manuell erledigt werden. SQL: `CREATE DATABASE IF NOT EXISTS businessproject CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`

### 4. `application.properties` prüfen
- [x] Passwort/Port/DB-URL stimmen (root/root, Port 2330)
- **Bemerkung:** Keine Änderungen nötig, Konfiguration war bereits korrekt.

### 5. Projekt kompilieren
- [x] `./mvnw clean compile` läuft fehlerfrei durch
- **Bemerkung:** Maven-Wrapper-JAR war korrupt und musste neu heruntergeladen werden. Danach: BUILD SUCCESS, alle 22 Quelldateien kompilieren sauber.

### 6. Applikation starten & erreichbar
- [ ] `./mvnw spring-boot:run` → `http://localhost:2330` erreichbar
- **Bemerkung:** Setzt laufende MySQL-Instanz mit DB `businessproject` voraus. Nach UUID-Umstieg (Phase 1) muss die DB gelöscht und neu angelegt werden!

### 7. Alle Basisfunktionen testen
- [ ] User-Registrierung funktioniert
- [ ] User-Login funktioniert
- [ ] Produktliste wird angezeigt
- [ ] Bestellung aufgeben funktioniert
- [ ] Admin-Login funktioniert
- [ ] Admin: User anlegen / bearbeiten / löschen
- [ ] Admin: Admin-Account verwalten
- [ ] Admin: Produkt anlegen / bearbeiten / löschen
- [ ] Logout beendet Session korrekt
- **Bemerkung:** Manueller Test nach DB-Setup nötig. Admin muss initial per SQL angelegt werden.

### 8. IST-Zustand Entitäten dokumentiert
- [x] Befunde dokumentiert in `PHASE_0_SETUP.md`
- **Bemerkung:** Alle Probleme (int-IDs, fehlende Validierung, toString-Leaks, double statt BigDecimal) wurden in Phase 1 behoben.

---

## Abnahme-Kriterium
- [x] Projekt kompiliert fehlerfrei mit aktuellem Java + Spring Boot
- [ ] Applikation startet ohne Fehler auf Port 2330 *(DB-Setup nötig)*
- [ ] Alle 9 Funktionen aus der Testcheckliste laufen ohne HTTP 500 *(manueller Test)*
- [ ] Kein unbehandelter Fehler im Browser-Netzwerk-Tab sichtbar *(manueller Test)*
