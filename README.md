# Gnarly Business Management – Security Project

Webapplikation zur Geschäftsverwaltung (Kunden, Produkte, Bestellungen) mit umfassender Sicherheitsimplementierung im Rahmen des Moduls M183 (Applikationssicherheit) an der BBZ BL.

**Team:** Alex Uscata & Furkan Güner

## Voraussetzungen

| Software | Version      | Hinweis |
|----------|--------------|---------|
| Java JDK | 25   | LTS-Version |
| MySQL / MariaDB | 5.7+ / 10.5+ | Lokal oder via XAMPP |
| Git | beliebig     | Zum Klonen des Repos |

> Maven muss **nicht** separat installiert werden – der mitgelieferte Maven Wrapper (`mvnw` / `mvnw.cmd`) erledigt das automatisch.

---

## Schnellstart

### 1. Repository klonen

```bash
git clone https://github.com/AlexMarrer/Gnarly_Business_Management_Security_Project.git
cd Gnarly_Business_Management_Security_Project
```

### 2. Datenbank einrichten

Starte MySQL/MariaDB (z.B. über XAMPP, systemd oder den Installer) und erstelle die Datenbank:

```sql
CREATE DATABASE IF NOT EXISTS businessproject;
```

> Die Tabellen werden beim ersten Start automatisch von Hibernate erstellt (`ddl-auto=update`).

### 3. Applikation starten

**Linux (Manjaro / Ubuntu):**
```bash
chmod +x mvnw
./mvnw spring-boot:run
```

**Windows (CMD / PowerShell):**
```cmd
mvnw.cmd spring-boot:run
```

Beim ersten Start wird der Maven Wrapper alle nötigen Dependencies herunterladen – das kann 1-2 Minuten dauern.

> **Stoppen:** Mit `Ctrl+C` im Terminal wird die Applikation beendet (Graceful Shutdown).

### 4. Im Browser oeffnen

```
http://localhost:2330
```

### 5. Einloggen

Beim ersten Start wird automatisch ein Admin-Account erstellt:

| Feld | Wert |
|------|------|
| E-Mail | `admin@business.com` |
| Passwort | `Admin123!` |

Über das Admin-Panel können weitere Admins, User und Produkte verwaltet werden. User können sich auch selbst über `/register` registrieren.

---

## Plattform-spezifische Hinweise

### Windows

**Java installieren:**
1. [Adoptium JDK 25](https://adoptium.net/) herunterladen und installieren
2. Installer wählt automatisch `JAVA_HOME` – bei manuellem Setup:
   - Systemumgebungsvariable `JAVA_HOME` auf den JDK-Ordner setzen (z.B. `C:\Program Files\Eclipse Adoptium\jdk-25`)
   - `%JAVA_HOME%\bin` zu `Path` hinzufügen
3. Prüfen: `java --version`

**MySQL via XAMPP:**
1. [XAMPP](https://www.apachefriends.org/) installieren
2. XAMPP Control Panel starten → **MySQL** starten
3. phpMyAdmin öffnen (`http://localhost/phpmyadmin`) → neue Datenbank `businessproject` anlegen

### Linux – Manjaro

```bash
# Java 21
sudo pacman -S jdk25-openjdk
sudo archlinux-java set java-25-openjdk

# MariaDB
sudo pacman -S mariadb
sudo mariadb-install-db --user=mysql --basedir=/usr --datadir=/var/lib/mysql
sudo systemctl enable --now mariadb
sudo mysql_secure_installation

# Datenbank anlegen
sudo mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS businessproject;"
```

### Linux – Ubuntu

```bash
# Java 25
sudo apt update
sudo apt install openjdk-25-jdk

# MySQL
sudo apt install mysql-server
sudo systemctl enable --now mysql
sudo mysql_secure_installation

# Datenbank anlegen
sudo mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS businessproject;"
```

---

## Konfiguration

Die Standartkonfiguration in `src/main/resources/application.properties` funktioniert out-of-the-box mit:
- DB-User: `root` / Passwort: `root`
- Port: `2330`

Falls dein MySQL-Setup ein anderes Passwort hat, kannst du es per Umgebungsvariable überschreiben:

**Linux:**
```bash
DB_PASSWORD=meinpasswort ./mvnw spring-boot:run
```

**Windows (CMD):**
```cmd
set DB_PASSWORD=meinpasswort
mvnw.cmd spring-boot:run
```

Alle konfigurierbaren Umgebungsvariablen:

| Variable | Default | Beschreibung                  |
|----------|---------|-------------------------------|
| `DB_USERNAME` | `root` | Datenbank-Benutzername        |
| `DB_PASSWORD` | `root` | Datenbank-Passwort            |
| `PEPPER_SECRET` | (interner Default) | Pepper für Passwort-Hashing   |
| `ADMIN_EMAIL` | `admin@business.com` | E-Mail des initialen Admins   |
| `ADMIN_PASSWORD` | `Admin123!` | Passwort des initialen Admins |

---

## Weitere Befehle

```bash
# Nur kompilieren (ohne Start)
./mvnw clean compile

# JAR bauen
./mvnw clean package -DskipTests

# Tests ausfuehren
./mvnw test
```

---

## Sicherheitsfeatures

- **Passwort-Hashing:** BCrypt (Cost 12) + Pepper
- **Brute-Force-Schutz:** Account-Sperre nach 5 Fehlversuchen
- **Session-Management:** HttpOnly Cookies, SameSite=Lax, Session-Fixation-Schutz
- **Autorisierung:** Rollenbasiert (ADMIN / USER) mit Spring Security
- **Input-Validierung:** Bean Validation + HTML5 Client-side Validation
- **XSS-Schutz:** Thymeleaf Output-Escaping, Security Headers, Pattern-Validierung
- **SQL-Injection-Schutz:** Spring Data JPA mit Prepared Statements

## Technologie-Stack

- Spring Boot 3.4.3
- Java 25
- Thymeleaf + Spring Security 6
- MySQL / MariaDB
- Hibernate (JPA)
