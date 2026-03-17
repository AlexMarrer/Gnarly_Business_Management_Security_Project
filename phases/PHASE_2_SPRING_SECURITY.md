# Phase 2 – Spring Security & Passwort-Sicherheit

## Ziel
Spring Security einrichten, alle Login-Prozesse auf sichere POST-Requests umstellen, Passwörter mit BCrypt + Pepper hashen und Brute-Force-Schutz implementieren.

---

## IST-Zustand (analysiert) – KRITISCHE LÜCKEN

| Problem | Fundstelle | Risiko |
|---|---|---|
| Login via `@GetMapping` – Credentials in der URL | `AdminController.java:47` & `:63` | Credentials in Server-Logs, Browser-History, Referer-Header |
| Passwörter im Klartext in der DB | `UserServices.validateLoginCredentials()` | Datenleck = alle Passwörter sofort lesbar |
| Keine Spring Security Dependency | `pom.xml` | Keinerlei Framework-Schutz |
| Kein `SecurityConfig` | gesamte Codebase | Alle Endpunkte öffentlich erreichbar |
| Login-Fehler verrät Details | `AdminController.java:57` – `model.addAttribute("error2", "Invalid email or password")` | Akzeptabel, aber ohne Brute-Force-Schutz nutzlos |

---

## Aufgaben

### 2a – Dependencies hinzufügen

- [ ] `pom.xml`: `spring-boot-starter-security` eintragen
- [ ] `pom.xml`: `thymeleaf-extras-springsecurity6` eintragen (für Phase 4)
- [ ] Sicherstellen: `spring-boot-starter-validation` bereits vorhanden (ja, laut pom.xml)

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

> **Achtung:** Nach dem Hinzufügen von Spring Security sperrt es ALLE Endpunkte sofort. App startet, aber kein Request geht mehr durch ohne Konfiguration. Sofort `SecurityConfig` anlegen!

---

### 2b – PepperPasswordEncoder Service erstellen

- [ ] Neue Datei: `src/main/java/com/business/security/PepperPasswordEncoder.java`
- [ ] Pepper-Secret in `application.properties` konfigurieren (via Env-Variable)

```java
// src/main/java/com/business/security/PepperPasswordEncoder.java
@Service
public class PepperPasswordEncoder {

    @Value("${app.security.pepper}")
    private String pepper;

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(12);

    public String encode(String rawPassword) {
        return bcrypt.encode(rawPassword + pepper);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return bcrypt.matches(rawPassword + pepper, encodedPassword);
    }
}
```

```properties
# application.properties – ergänzen
app.security.pepper=${PEPPER_SECRET:bitte-aendern-in-produktion}
```

> **Fallstrick:** Der Pepper darf NIE in den Git-Commit! Über Umgebungsvariable (`PEPPER_SECRET=...`) setzen oder in eine lokale `.env`-Datei (in `.gitignore` eintragen).

---

### 2c – UserDetailsService implementieren

- [ ] Neue Datei: `src/main/java/com/business/security/CustomUserDetailsService.java`
- [ ] Sowohl User als auch Admin müssen sich einloggen können

```java
// src/main/java/com/business/security/CustomUserDetailsService.java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AdminRepository adminRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Zuerst User-Tabelle prüfen
        User user = userRepo.findByUemail(email);
        if (user != null) {
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUemail())
                    .password(user.getUpassword())
                    .roles("USER")
                    .build();
        }
        // Dann Admin-Tabelle prüfen
        Admin admin = adminRepo.findByAdminEmail(email);
        if (admin != null) {
            return org.springframework.security.core.userdetails.User.builder()
                    .username(admin.getAdminEmail())
                    .password(admin.getAdminPassword())
                    .roles("ADMIN")
                    .build();
        }
        throw new UsernameNotFoundException("User nicht gefunden: " + email);
    }
}
```

> **Fallstrick:** `UserRepository` und `AdminRepository` müssen eine `findByUemail(String email)` bzw. `findByAdminEmail(String email)` Methode haben. Diese ggf. in den Repositories ergänzen.

---

### 2d – SecurityConfig erstellen

- [ ] Neue Datei: `src/main/java/com/business/security/SecurityConfig.java`

```java
// src/main/java/com/business/security/SecurityConfig.java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private PepperPasswordEncoder pepperEncoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/home",
                                 "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**", "/product/**").hasRole("USER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .maximumSessions(1)
            )
            .headers(headers -> headers
                .xssProtection(xss -> xss.headerValue(
                    XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(frame -> frame.deny())
            );
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(new PasswordEncoder() {
            public String encode(CharSequence raw) {
                return pepperEncoder.encode(raw.toString());
            }
            public boolean matches(CharSequence raw, String encoded) {
                return pepperEncoder.matches(raw.toString(), encoded);
            }
        });
        return provider;
    }
}
```

---

### 2e – Login-Seite anpassen (GET → POST, Credentials aus URL entfernen)

- [ ] Bestehende Login-Logik in `AdminController.java` (Zeilen 47–83) entfernen – Spring Security übernimmt das!
- [ ] `AdminLogin.java` und `UserLogin.java` können ggf. entfernt werden
- [ ] Thymeleaf Login-Template anpassen: `action="/login"`, `method="post"`, CSRF-Token

```html
<!-- Login.html – Vorher: separate Admin/User Formulare mit GET -->
<!-- Login.html – Nachher: ein einheitliches Spring Security Login-Formular -->
<form th:action="@{/login}" method="post">
    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
    <input type="email" name="email" required placeholder="E-Mail"/>
    <input type="password" name="password" required placeholder="Passwort"/>
    <button type="submit">Login</button>
    <!-- GENERISCHE Fehlermeldung – NICHT welches Feld falsch ist! -->
    <div th:if="${param.error}" class="alert alert-danger">
        Benutzername oder Passwort falsch.
    </div>
</form>
```

---

### 2f – Passwörter migrieren (Klartext → BCrypt+Pepper)

- [ ] Alle bestehenden Testdaten in der DB löschen (Klartext-Passwörter ungültig nach Migration)
- [ ] `UserServices.addUser()` anpassen: Passwort vor dem Speichern hashen
- [ ] `AdminServices.addAdmin()` anpassen: Passwort vor dem Speichern hashen

```java
// UserServices.java – addUser anpassen
@Autowired
private PepperPasswordEncoder passwordEncoder;

public User addUser(User user) {
    user.setUpassword(passwordEncoder.encode(user.getUpassword()));
    return userRepo.save(user);
}
```

> **Fallstrick:** Bestehende `validateLoginCredentials()` Methode kann entfernt werden – Spring Security übernimmt die Authentifizierung vollständig.

---

### 2g – Brute-Force-Schutz (Account Lockout)

- [ ] Login-Fehlversuche zählen (z.B. in-memory oder DB-Feld `failedLoginAttempts`)
- [ ] Nach 5 Fehlversuchen: Account sperren (`accountLocked = true`)
- [ ] Oder: Rate-Limiting via Spring Boot Resilience4j / Bucket4j

**Einfache Variante (in-memory, für Abgabe ausreichend):**

```java
// LoginAttemptService.java
@Service
public class LoginAttemptService {
    private final int MAX_ATTEMPTS = 5;
    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();

    public void loginFailed(String email) {
        attemptsCache.merge(email, 1, Integer::sum);
    }

    public boolean isBlocked(String email) {
        return attemptsCache.getOrDefault(email, 0) >= MAX_ATTEMPTS;
    }

    public void loginSucceeded(String email) {
        attemptsCache.remove(email);
    }
}
```

---

## Fallstricke

- Nach Spring Security Dependency → App ist sofort gesperrt → SecurityConfig muss gleichzeitig erstellt werden
- CSRF-Token in allen POST-Formularen obligatorisch (Thymeleaf macht das mit `th:action="@{/url}"` automatisch)
- Pepper NIEMALS in Git committen → `.gitignore` und Env-Variable
- `loadUserByUsername()` darf nicht verraten ob User existiert oder nicht (Exception werfen, nicht null returnen)
- BCrypt-Stärke (cost factor): `12` ist guter Default – testet die Abnahme auf Performance

---

## Abnahme-Kriterium: Bewertungskriterium 3 – Sicherer Login ⚠️ DOPPELT GEWICHTET (0.4)

**Nachweis:** Fehlermeldungen zeigen (POST), Browser-DevTools Netzwerk-Tab

- [ ] Login erfolgt per POST (kein GET) – im Browser-Netzwerk-Tab zeigen
- [ ] Credentials tauchen NICHT in der URL auf
- [ ] Fehlermeldung ist generisch: "Benutzername oder Passwort falsch" (nicht welches)
- [ ] Brute-Force-Schutz demonstrieren (5x falsch eingeben → gesperrt)
- [ ] DB-Eintrag zeigen: Passwort ist BCrypt-Hash (nicht Klartext)

## Abnahme-Kriterium: Bewertungskriterium 4 – Passwort-Speicherung (Gewichtung 0.2)

- [ ] DB-Eintrag live zeigen: Passwort-Feld enthält `$2a$12$...` (BCrypt-Hash)
- [ ] Code zeigen: `PepperPasswordEncoder.java`
- [ ] Nachweis: https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html
