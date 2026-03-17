# M183 – Business Management Secured

## Überblick

Modul 183: Applikationssicherheit implementieren (BBZ BL, Roger Zaugg)

**Team:** Alex Uscata & Furkan Güner

Wir verwenden ein **bestehendes Open-Source-Projekt** und sichern es ab:
- **Repo:** https://github.com/SuhasKamate/Business_Management_Project
- **Stack:** Java 25, Spring Boot, Thymeleaf, Hibernate, MySQL (XAMPP)
- **UI:** HTML & CSS
- **Sicherheitsframework:** Spring Security
- **IDE:** IntelliJ / VSCode
- **OS:** Windows / Linux

## Ausgangslage

Die bestehende Applikation ist eine Business-Management-Webanwendung mit User- und Admin-Bereich. Sie hat **keine oder mangelhafte Sicherheitsmassnahmen**. Unser Auftrag ist es, diese Applikation hinsichtlich der definierten Sicherheitsthemen zu analysieren und zu erweitern/optimieren.

## Produktfunktionen

### Standardbenutzer
- Login / Registrierung im User-Login Fenster
- Produkte kaufen, suchen
- Bestellung anschauen

### Admin
- Login im Admin-Login Fenster
- Managen der Userdaten (CRUD)
- Managen der Admin-Daten (CRUD)
- Managen der Produkte (CRUD)

## Entitäten / Datenmodell

### USER
| Feld | Validierung | DB-Typ |
|------|------------|--------|
| ID | automatisch generiert | Guid |
| Benutzername | Pflichtfeld, 2-50 Zeichen | varchar(50) |
| E-Mail | Pflichtfeld, gültiges E-Mail-Format | varchar(255) |
| Passwort | Pflichtfeld, min 8 Zeichen, Gross+Klein+Zahl+Sonderzeichen | varchar(255) |
| Telefonnummer | Pflichtfeld, 10-15 Ziffern, nur Zahlen | varchar(20) |

### ADMIN
| Feld | Validierung | DB-Typ |
|------|------------|--------|
| ID | automatisch generiert | Guid |
| Benutzername | Pflichtfeld, 2-50 Zeichen | varchar(50) |
| E-Mail | Pflichtfeld, gültiges E-Mail-Format | varchar(255) |
| Passwort | Pflichtfeld, min 8 Zeichen, Gross+Klein+Zahl+Sonderzeichen | varchar(255) |
| Telefonnummer | Pflichtfeld, 10-15 Ziffern, nur Zahlen | varchar(20) |

### ORDERS
| Feld | Validierung | DB-Typ |
|------|------------|--------|
| ID | automatisch generiert | Guid |
| Name | Pflichtfeld, 2-100 Zeichen | varchar(100) |
| Einzelpreis | Pflichtfeld, min 0.01, max 2 Nachkommastellen | decimal(11,2) |
| Anzahl | Pflichtfeld, min 1, max 999 | unsigned smallint |
| Bestelldatum | automatisch gesetzt | date |
| Totalpreis | automatisch berechnet (Einzelpreis × Anzahl) | decimal(11,2) |
| User-Referenz | Verknüpfung zum Benutzer | Guid |

### PRODUKT
| Feld | Validierung | DB-Typ |
|------|------------|--------|
| ID | automatisch generiert | Guid |
| Name | Pflichtfeld, 2-100 Zeichen | varchar(100) |
| Preis | Pflichtfeld, min 0.01, max 10 Stellen + 2 NK | decimal(11,2) |
| Beschreibung | Pflichtfeld, max 500 Zeichen | text(500) |

## Validierungsregeln

- Typenvalidierung
- Zeichenlänge-Validierung
- Einzigartige Zeichen im Passwort
- Gross-/Kleinschreibung im Passwort
- Zahlen im Passwort
- Client-seitige UND Server-seitige Validierung

## Eigener Fokus

**Fokusthema:** TryHackMe Challenge "Bank Rott"

**Erfolgs-Indikatoren:** Challenge erfolgreich abgeschlossen + Vorgehen nachvollziehbar dokumentiert gemäss Anforderungen Beurteilungsformular.

> Deadline: Alle Teammitglieder haben die Challenge bis **29.03.2026** erfolgreich gelöst.
