package com.aveplus.uemoa.qr.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour CRCCalculator (sans Spring)
 */
public class CRCCalculatorTest {
    
    private CRCCalculator crcCalculator;
    
    @BeforeEach
    public void setUp() {
        crcCalculator = new CRCCalculator();
    }
    
    @Test
    public void testCalculateCRC() {
        // Test avec une chaîne connue
        String data = "00020101021238";
        String crc = crcCalculator.calculate(data);
        
        assertNotNull(crc);
        assertEquals(4, crc.length()); // CRC doit faire 4 caractères
        assertTrue(crc.matches("[0-9A-F]{4}")); // Format hexadécimal
    }
    
    @Test
    public void testValidateCRC() {
        // Génère un QR avec CRC
        String dataWithoutCrc = "00020101021238" + "6304";
        String crc = crcCalculator.calculate(dataWithoutCrc);
        String fullData = dataWithoutCrc + crc;
        
        // Valide le CRC
        boolean isValid = crcCalculator.validate(fullData);
        assertTrue(isValid);
    }
    
    @Test
    public void testInvalidCRC() {
        // QR avec CRC invalide
        String invalidData = "00020101021238" + "6304" + "XXXX";
        boolean isValid = crcCalculator.validate(invalidData);
        assertFalse(isValid);
    }
    
    @Test
    public void testNullData() {
        assertThrows(IllegalArgumentException.class, () -> {
            crcCalculator.calculate(null);
        });
    }
    
    @Test
    public void testEmptyData() {
        assertThrows(IllegalArgumentException.class, () -> {
            crcCalculator.calculate("");
        });
    }
}
