package com.example.demo.interfaces;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class WebConfig {

    @Bean
    @Profile("cors")//only enabled when requires cors.
    CorsFilter corsFilter() {
        var config = new CorsConfiguration().applyPermitDefaultValues();
        //config.setAllowedOrigins(List.of("http://localhost:4200"));
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

}
