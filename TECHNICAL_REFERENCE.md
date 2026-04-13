# Technische Referenz – Spring Boot Security Patterns

> Dieses Dokument enthält die wichtigsten Code-Patterns für die Sicherheitsimplementierung.
> Basierend auf: Java 25, Spring Boot (aktuell), Spring Security, Thymeleaf, Hibernate, MySQL

---

## Dependencies (pom.xml)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

## application.properties – Sicherheitsrelevante Einstellungen

```properties
# Session
server.servlet.session.timeout=30m
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=Lax

# Pepper (als externe Konfiguration / Env-Variable)
app.security.pepper=${PEPPER_SECRET:defaultPepperChangeMe}

# DB-User mit eingeschränkten Rechten (NICHT root!)
spring.datasource.username=app_user
spring.datasource.password=${DB_PASSWORD}
```

---

## SecurityConfig.java – Grundgerüst

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").hasRole("USER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .maximumSessions(1)
            )
            .headers(headers -> headers
                .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(frame -> frame.deny())
            );

        return http.build();
    }
}
```

---

## Pepper-Implementierung

```java
@Service
public class PepperPasswordEncoder {

    @Value("${app.security.pepper}")
    private String pepper;

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    public String encode(String rawPassword) {
        return bcrypt.encode(rawPassword + pepper);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return bcrypt.matches(rawPassword + pepper, encodedPassword);
    }
}
```

---

## Bean Validation auf Entitäten

```java
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Benutzername ist erforderlich")
    @Size(min = 2, max = 50, message = "Benutzername muss 2-50 Zeichen lang sein")
    @Column(length = 50, nullable = false)
    private String username;

    @NotBlank(message = "E-Mail ist erforderlich")
    @Email(message = "Ungültiges E-Mail-Format")
    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Passwort ist erforderlich")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Passwort: min 8 Zeichen, Gross+Kleinbuchstaben, Zahl, Sonderzeichen"
    )
    @Column(length = 255, nullable = false)
    private String password;

    @NotBlank(message = "Telefonnummer ist erforderlich")
    @Pattern(regexp = "^\\d{10,15}$", message = "Telefonnummer: 10-15 Ziffern")
    @Column(length = 20, nullable = false)
    private String phone;

    // Role field for Spring Security
    @Column(nullable = false)
    private String role; // "ROLE_USER" or "ROLE_ADMIN"
}
```

---

## Controller – Serverseitige Validierung

```java
@PostMapping("/register")
public String register(@Valid @ModelAttribute("user") User user,
                       BindingResult result, Model model) {
    if (result.hasErrors()) {
        return "register"; // Zurück zum Formular mit Fehlermeldungen
    }
    // ... User speichern
    return "redirect:/login";
}
```

---

## Thymeleaf – Sicherheits-Patterns

```html
<!-- Aktuell eingeloggten User anzeigen -->
<span sec:authentication="name">Username</span>

<!-- Nur für Admins sichtbar -->
<div sec:authorize="hasRole('ADMIN')">
    <a th:href="@{/admin/users}">User verwalten</a>
</div>

<!-- Nur für eingeloggte User -->
<div sec:authorize="isAuthenticated()">
    <a th:href="@{/logout}">Logout</a>
</div>

<!-- Output Escaping (Standard bei th:text) -->
<p th:text="${product.description}">Beschreibung</p>
<!-- NIEMALS th:utext für User-Input verwenden! -->

<!-- Login-Fehlermeldung (generisch!) -->
<div th:if="${param.error}" class="error">
    Benutzername oder Passwort falsch.
</div>
```

---

## SQL – Eingeschränkter DB-User

```sql
-- DB-User mit minimalen Rechten erstellen
CREATE USER 'app_user'@'localhost' IDENTIFIED BY 'sicheres_passwort';
GRANT SELECT, INSERT, UPDATE, DELETE ON business_management.* TO 'app_user'@'localhost';
FLUSH PRIVILEGES;

-- KEIN: DROP, CREATE, ALTER, GRANT, FILE, PROCESS, etc.
```

---

## Nachweis-Quellen (für Abnahme-Dokument)

| Thema | Quelle |
|-------|--------|
| Spring Security Architektur | https://docs.spring.io/spring-security/reference/servlet/architecture.html |
| BCrypt in Spring Security | https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html |
| Session Management | https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html |
| Thymeleaf Security Extras | https://github.com/thymeleaf/thymeleaf-extras-springsecurity |
| Thymeleaf Output Escaping | https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html#unescaped-text |
| Hibernate Prepared Statements | https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html |
| Bean Validation | https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html |
| OWASP SQL Injection Prevention | https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html |
| OWASP XSS Prevention | https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Scripting_Prevention_Cheat_Sheet.html |
| OWASP Session Management | https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html |
| OWASP Password Storage | https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html |
