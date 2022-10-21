package fr.thomah.souvenirs.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;

@EnableWebSecurity
public class WebConfig extends WebSecurityConfigurerAdapter {

    @Value("${fr.thomah.souvenirs.api.cors.origins}")
    private String corsOrigins;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        corsConfiguration.setAllowedOrigins(List.of(corsOrigins.split(",")));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PUT", "OPTIONS", "PATCH", "DELETE"));
        
        // You can customize the following part based on your project, it's only a sample
        http.csrf().disable()
            .cors().configurationSource(request -> corsConfiguration);
    }
}
