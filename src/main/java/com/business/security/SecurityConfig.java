package com.business.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Autowired
	private PepperPasswordEncoder pepperEncoder;

	@Autowired
	private LoginAttemptService loginAttemptService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/", "/login", "/home", "/products", "/location", "/about",
						"/css/**", "/Images/**", "/JavaScript/**", "/Videos/**").permitAll()
				.requestMatchers("/admin/**", "/addAdmin", "/addingAdmin",
						"/updateAdmin/**", "/updatingAdmin/**", "/deleteAdmin/**",
						"/addProduct", "/addingProduct", "/updateProduct/**",
						"/updatingProduct/**", "/deleteProduct/**",
						"/addUser", "/addingUser", "/updateUser/**",
						"/updatingUser/**", "/deleteUser/**").hasRole("ADMIN")
				.requestMatchers("/product/**").hasRole("USER")
				.anyRequest().authenticated()
			)
			.formLogin(form -> form
				.loginPage("/login")
				.usernameParameter("email")
				.passwordParameter("password")
				.successHandler((request, response, authentication) -> {
					loginAttemptService.loginSucceeded(authentication.getName());
					boolean isAdmin = authentication.getAuthorities().stream()
							.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
					if (isAdmin) {
						response.sendRedirect("/admin/services");
					} else {
						response.sendRedirect("/product/back");
					}
				})
				.failureHandler((request, response, exception) -> {
					String email = request.getParameter("email");
					if (!(exception instanceof LockedException)) {
						loginAttemptService.loginFailed(email);
					}
					if (loginAttemptService.isBlocked(email)) {
						response.sendRedirect("/login?locked=true");
					} else {
						response.sendRedirect("/login?error=true");
					}
				})
				.permitAll()
			)
			.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/login?logout=true")
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID")
				.clearAuthentication(true)
				.permitAll()
			)
			.sessionManagement(session -> session
				.sessionFixation().migrateSession()
				.maximumSessions(1)
				.maxSessionsPreventsLogin(false)
			)
			.headers(headers -> headers
				.xssProtection(xss -> xss
						.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
				.contentTypeOptions(Customizer.withDefaults())
				.frameOptions(frame -> frame.deny())
			);
		return http.build();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(pepperEncoder);
		return provider;
	}

	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}
}
