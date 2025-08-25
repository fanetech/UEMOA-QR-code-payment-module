package com.aveplus.uemoa.qr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration automatique pour le module UEMOA QR Payment
 * Cette classe permet l'intégration automatique du module dans les projets Spring Boot
 */
@Configuration
@ComponentScan(basePackages = "com.aveplus.uemoa.qr")
public class UemoaQRAutoConfiguration {
    
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "uemoa.qr.payment")
    public UemoaQrProperties uemoaQrProperties() {
        return new UemoaQrProperties();
    }
}
