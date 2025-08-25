package com.aveplus.uemoa.qr;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Configuration de test pour le module UEMOA QR
 * 
 * Cette classe est nécessaire pour les tests car le module n'a pas
 * de classe @SpringBootApplication (c'est une librairie, pas une app)
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.aveplus.uemoa.qr")
public class TestConfiguration {
    // Cette classe sert uniquement pour les tests
    // Elle simule une @SpringBootApplication pour que les tests fonctionnent
}
