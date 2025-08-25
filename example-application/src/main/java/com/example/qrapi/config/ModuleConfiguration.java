package com.example.qrapi.config;

import com.aveplus.uemoa.qr.config.UemoaQRAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuration pour importer le module UEMOA QR
 * Le module s'auto-configure, on a juste besoin de l'importer
 */
@Configuration
@Import(UemoaQRAutoConfiguration.class)
public class ModuleConfiguration {
    // Les beans sont automatiquement créés par le module
    // Pas besoin de configuration manuelle
}
