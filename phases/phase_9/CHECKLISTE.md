# Phase 9 – Checkliste

## Aufgaben

### 9a – DataLoader erstellen
- [x] `DataLoader.java` als `CommandLineRunner`
- [x] Prüft ob Admin-Tabelle leer (`adminRepository.count() == 0`)
- [x] Legt Default-Admin mit gehashtem Passwort an (BCrypt+Pepper)
- [x] Konsolenausgabe mit Credentials

### 9b – Sicherheit
- [x] Konsolenhinweis: Default-Passwort nach Login ändern
- [x] Für Produktion: Env-Variablen (`ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_NUMBER`) in `application.properties`

## Abnahme-Tests (manuell durchzuführen)
- [ ] App startet mit leerer DB → Admin wird automatisch angelegt
- [ ] Konsole zeigt Hinweis mit Default-Credentials
- [ ] Login mit `admin@business.com` / `Admin123!` funktioniert
- [ ] Bei erneutem Start wird KEIN zweiter Admin erstellt
- [ ] Passwort in DB ist BCrypt-Hash
