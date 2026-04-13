# Bewertungskriterien & Sicherheitsanforderungen

> Startnote: 4 | Bewertungsschritt: 0.2 | Notenbereich: 2–6
>
> Bewertungsstufen: `-` = nicht erfüllt | `0` = teilweise erfüllt | `+` = erfüllt

## WICHTIG: Framework-Nachweis

Wenn Spring Security (oder ein anderes Framework) eine Sicherheitsmassnahme automatisch übernimmt, muss dies bei der Abnahme mit **Quellen belegt** werden:
- Angabe zum Thema
- Kurzer Kommentar
- Ausschnitt der eigenen Implementierung
- Verweis auf belegende Informationen (Links auf Originaldokumentation, Screenshots)

**Ohne Nachweis → "teilweise erfüllt"**

---

## Kriterium 1: Funktionsfähigkeit (Allgemein)

**Anforderung:** Die gemäss Projektantrag definierten Anforderungen sind funktionsfähig implementiert.

**Indikatoren:**
- Applikation stürzt nicht ab
- Funktionen funktionieren wie gewünscht gem. Projektantrag (ohne Sicherheitsbetrachtung)

---

## Kriterium 2: Authentifizierung & Autorisierung – Rollen/Rechte ⚠️ DOPPELT GEWICHTET (0.4)

**Anforderung:** Funktionen, welche nur bestimmte Benutzer/Benutzergruppen ausführen dürfen, sind sowohl im Frontend wie auch im Backend berücksichtigt und funktionieren.

**Indikatoren – wenn der Benutzer das Recht NICHT hat:**
- Funktion nicht sichtbar (Frontend)
- Funktion nicht manuell mit Tools auf dem Server aufrufbar (Backend)
- Allfällige Formulare werden gar nicht ausgeliefert

### Was zu tun ist:
- Spring Security konfigurieren mit Rollen (USER, ADMIN)
- `@PreAuthorize` / `@Secured` Annotationen auf Controller-Methoden
- Thymeleaf `sec:authorize` für bedingte UI-Anzeige
- Backend-Prüfung: Auch wenn jemand die URL direkt aufruft, wird geprüft
- Admin-Funktionen (User-CRUD, Admin-CRUD, Produkt-CRUD) nur für ADMIN
- User-Funktionen (Produkte kaufen, Bestellungen ansehen) nur für eingeloggte USER
- Keine reine Presentation Layer Security!

---

## Kriterium 3: Sicherer Login-Mechanismus ⚠️ DOPPELT GEWICHTET (0.4)

**Anforderung:** Der Login-Mechanismus ist sicher implementiert.

**Indikatoren:**
- Loginprozess wurde sicher implementiert (gem. erarbeitetem Ablauf)

### Was zu tun ist:
- Keine Informationspreisgabe bei fehlgeschlagenem Login ("Benutzername oder Passwort falsch" – nicht welches von beiden)
- Brute-Force-Schutz (Rate Limiting / Account Lockout nach X Versuchen)
- Sichere Weiterleitung nach Login
- HTTPS-Nutzung (zumindest Konzept/Konfiguration)
- Kein Passwort im URL / GET-Parameter
- Login-Formular per POST

---

## Kriterium 4: Sichere Passwortspeicherung

**Anforderung:** Passwörter sind sicher gespeichert.

**Indikatoren:**
- Passwörter mit **Salt** und **Hash** in der DB
- Aktuell als offiziell sicher erachteter Hash-Algorithmus (BCrypt, Argon2, SCrypt)
- **Pepper** wird verwendet

### Was zu tun ist:
- Spring Security `PasswordEncoder` verwenden (BCrypt empfohlen)
- Salt wird von BCrypt automatisch generiert
- Pepper: globales Secret als zusätzliche Absicherung (z.B. als Environment Variable)
- Passwörter NIE im Klartext speichern
- Passwörter NIE loggen

---

## Kriterium 5: Sicherer Umgang mit Authentifizierungselementen (Sessions/Tokens)

**Anforderung:** Sicherer Umgang mit Sessions oder Tokens.

**Indikatoren (Variante Session):**
- Sicherer Umgang mit Sessions
- Invalidierung zum geeigneten Zeitpunkt (Logout, Timeout)
- Allfällige Datenübernahme aus der alten Session (Session Fixation Protection)

**Indikatoren (Variante Token):**
- Sicherer Umgang mit Tokens
- Sichere Speicherung der Tokens auf dem Client

### Was zu tun ist (Session-Variante mit Spring Security):
- Session Fixation Protection aktivieren (`migrateSession` oder `newSession`)
- Session-Timeout konfigurieren (z.B. 30 Min)
- Session-Invalidierung bei Logout
- Sichere Session-Cookie-Konfiguration:
  - `HttpOnly = true`
  - `Secure = true` (HTTPS)
  - `SameSite = Lax` oder `Strict`
- Maximale gleichzeitige Sessions begrenzen

---

## Kriterium 6: Injection-Schutz

**Anforderung:** Injections werden durch entsprechende Gegenmassnahmen verhindert.

**Indikatoren:**
- Prepared Statements
- Named Queries mit Parametern
- Input-Validierung (Client + Server)
- Rechtebeschränkung DB-User

### Was zu tun ist:
- Hibernate/JPA verwenden (nutzt automatisch Prepared Statements)
- Keine nativen SQL-Queries mit String-Concatenation
- Falls native Queries: `@Query` mit benannten Parametern (`:param`)
- Serverseitige Validierung mit Bean Validation (`@Valid`, `@NotBlank`, `@Size`, etc.)
- Clientseitige Validierung mit HTML5-Attributen (`required`, `minlength`, `pattern`, etc.)
- DB-User mit minimalen Rechten (nicht root verwenden)
- Nachweis, dass Hibernate Prepared Statements nutzt (Doku-Link)

---

## Kriterium 7: XSS-Schutz

**Anforderung:** XSS-Angriffe werden durch entsprechende Gegenmassnahmen verhindert.

**Indikatoren:**
- Input-Validierung (Client + Server)
- Output Escaping
- Sichere Konfiguration der Session/Sessioncookie / Tokens
- (Content Security Policy → optional als Fokusthema)

### Was zu tun ist:
- Thymeleaf escaped standardmässig mit `th:text` (NICHT `th:utext` verwenden!)
- Nachweis, dass Thymeleaf Output Escaping macht (Doku-Link)
- Input-Validierung: Keine Script-Tags in Eingabefeldern erlauben
- HttpOnly-Flag auf Session-Cookies (verhindert JS-Zugriff)
- Optional: Content-Security-Policy Header setzen
- Optional: X-XSS-Protection Header

---

## Kriterium 8: Eigener Fokus – TryHackMe "Bank Rott"

**Anforderung:** Challenge erfolgreich abgeschlossen und dokumentiert.

**Indikatoren:**
- Alle Teammitglieder haben die Challenge bis **29.03.2026** erfolgreich gelöst
- Vorgehen zur Lösung der Aufgaben wurde nachvollziehbar dokumentiert

### Was zu tun ist:
- TryHackMe Account erstellen
- Challenge "Bank Rott" durcharbeiten
- Dokumentation erstellen mit:
  - Screenshots der gelösten Aufgaben
  - Beschreibung des Vorgehens pro Aufgabe
  - Erklärung der gefundenen Schwachstellen
  - Bezug zum eigenen Projekt herstellen
