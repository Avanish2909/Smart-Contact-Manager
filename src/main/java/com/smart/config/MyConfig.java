package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import com.smart.entities.User;

@Configuration
@EnableWebSecurity
public class MyConfig {
	
	
	private User user;

	@Bean
	public UserDetailsService getUserDetailService() {
		return new UserDetailsServiceImpl();
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(this.getUserDetailService());
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		return daoAuthenticationProvider;
	}
	
	
	//configure method...
	/**
	//1st method
	@Bean
	protected void configure(AuthenticationManagerBuilder auth)throws Exception{
		auth.authenticationProvider(authenticationProvider());
	}
	*/
	
	//2nd method
	public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}
	
	
	/*
	@Override
	protected void configure(HttpSecurity http) throws Exception{
		http.authorizeRequests()
	}
	*/
	
	/*
	// something went wrong
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
		http
		.securityMatchers((matchers) -> matchers
                .requestMatchers("/user/**")
            )
            .authorizeHttpRequests((authorize) -> authorize
                .anyRequest().hasRole("USER")
            )
        .securityMatchers((matchers) -> matchers
                    .requestMatchers("/admin/**"))
                    .authorizeHttpRequests((authorize) -> authorize
                     .anyRequest().hasRole("ADMIN"))
         .securityMatchers((matchers) -> matchers
                     .requestMatchers("/**")
                     )
                     .authorizeHttpRequests((authorize) -> authorize
                     .anyRequest().permitAll()
                     );
		return http.build();
	}
	*/
	
	/**
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeHttpRequests((authorizeRequests) ->
                        authorizeRequests
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/user/**").hasAuthority("Role_User")
                                .requestMatchers("/**").authenticated()

                )
                .formLogin()
	                .loginPage("/signin")
					.loginProcessingUrl("/dologin")
					.defaultSuccessUrl("/user/index")
	                .failureUrl("/login-fail");;
                
		return http.build();
	}
	*/
	
	
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf().disable()
			.authorizeHttpRequests()
			.requestMatchers("/admin/**").hasRole("ADMIN")
			.requestMatchers("user/**").hasAuthority("Role_User")
			.requestMatchers("/**").permitAll()
			.and()
			.formLogin()
				.loginPage("/signin")
				.loginProcessingUrl("/dologin")
				.defaultSuccessUrl("/user/index")
				.usernameParameter("email")
				.passwordParameter("password");
				 	
			
			
		return http.build();
	}
	
	
	
	
}
