# Phase 8 – Checkliste

## Aufgaben

### 8a – Register.html Template
- [x] Template erstellt mit allen Feldern
- [x] HTML5-Validierung (`required`, `pattern`, `minlength`)
- [x] `th:errors` Fehleranzeige
- [x] Link zurück zu Login

### 8b – RegistrationController
- [x] `GET /register` → Formular anzeigen
- [x] `POST /register` → User anlegen
- [x] Passwort-Bestätigung prüfen
- [x] Duplikat-E-Mail prüfen
- [x] Redirect zu `/login?registered=true`

### 8c – SecurityConfig
- [x] `/register` in `permitAll()` aufgenommen

### 8d – Login-Seite
- [x] Link "Hier registrieren" eingefügt
- [x] Meldung nach erfolgreicher Registrierung

### 8e – CSS (optional)
- [x] `Register.css` erstellt

---

## Abnahme-Tests (manuell durchzuführen)
- [ ] Registrierungsformular erreichbar unter `/register`
- [ ] Validierung: Name, E-Mail, Passwort-Komplexität, Telefonnummer
- [ ] Passwort-Bestätigung wird geprüft
- [ ] Duplikat-E-Mail wird abgefangen
- [ ] Nach Registrierung: Redirect zu Login mit Erfolgsmeldung
- [ ] Neuer User kann sich einloggen
- [ ] Passwort in DB als BCrypt-Hash gespeichert (`$2a$12$...`)
