# Phase 9 – DataLoader / Admin-Seeding

## Ziel
Beim ersten Start der Applikation automatisch einen initialen Admin-Account anlegen, damit das System sofort nutzbar ist – ohne manuellen SQL-Eingriff.

---

## IST-Zustand

| Problem | Auswirkung |
|---|---|
| Nach DB-Neuaufbau existieren keine Accounts | Niemand kann sich einloggen |
| Admin kann nur durch einen Admin erstellt werden | Henne-Ei-Problem |
| Kein Seeding-Mechanismus vorhanden | Manueller SQL-Insert nötig |

---

## Aufgaben

### 9a – DataLoader als CommandLineRunner

- [ ] Neue Datei: `src/main/java/com/business/security/DataLoader.java`
- [ ] Implementiert `CommandLineRunner` – läuft einmalig beim App-Start
- [ ] Prüft ob Admin-Tabelle leer ist
- [ ] Legt Default-Admin an mit gehashtem Passwort
- [ ] Gibt Hinweis in Konsole aus

```java
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PepperPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (((List<?>) adminRepository.findAll()).isEmpty()) {
            Admin admin = new Admin();
            admin.setAdminName("Admin");
            admin.setAdminEmail("admin@business.com");
            admin.setAdminPassword(passwordEncoder.encode("Admin123!"));
            admin.setAdminNumber("0000000000");
            adminRepository.save(admin);
            System.out.println("=== Initialer Admin angelegt: admin@business.com / Admin123! ===");
        }
    }
}
```

---

### 9b – Sicherheitshinweise

- [ ] Default-Passwort nach erstem Login ändern (Hinweis in Konsole)
- [ ] In Produktion: DataLoader deaktivieren oder Default-Credentials aus Env-Variable lesen

```java
// Produktionssichere Variante mit Env-Variablen:
@Value("${app.admin.email:admin@business.com}")
private String defaultAdminEmail;

@Value("${app.admin.password:Admin123!}")
private String defaultAdminPassword;
```

---

## Fallstricke

- DataLoader läuft bei JEDEM App-Start → darum Prüfung `if (adminRepository.findAll().isEmpty())`
- Passwort muss über `PepperPasswordEncoder.encode()` gehasht werden – KEIN Klartext in DB
- Default-Passwort `Admin123!` erfüllt die Komplexitätsanforderungen (Gross+Klein+Zahl+Sonderzeichen)
- In Produktion: Default-Credentials NICHT hardcoden → Umgebungsvariablen verwenden

---

## Abnahme-Kriterium

- [ ] App startet mit leerer DB → Admin wird automatisch angelegt
- [ ] Konsole zeigt Hinweis mit Default-Credentials
- [ ] Login mit `admin@business.com` / `Admin123!` funktioniert
- [ ] Bei erneutem Start wird KEIN zweiter Admin erstellt
- [ ] Passwort in DB ist BCrypt-Hash
