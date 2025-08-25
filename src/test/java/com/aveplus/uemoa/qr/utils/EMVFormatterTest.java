package com.aveplus.uemoa.qr.utils;

import com.aveplus.uemoa.qr.model.EMVField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour EMVFormatter (sans Spring)
 */
public class EMVFormatterTest {
    
    private EMVFormatter formatter;
    
    @BeforeEach
    public void setUp() {
        formatter = new EMVFormatter();
    }
    
    @Test
    public void testFormatField() {
        // Test formatage simple
        String result = formatter.formatField("00", "01");
        assertEquals("000201", result); // ID(00) + Length(02) + Value(01)
    }
    
    @Test
    public void testFormatFieldWithLongerValue() {
        // Test avec valeur plus longue
        String result = formatter.formatField("36", "int.bceao.pi");
        assertEquals("3612int.bceao.pi", result); // ID(36) + Length(12) + Value
    }
    
    @Test
    public void testFormatFieldWithEmptyValue() {
        // Test avec valeur vide
        String result = formatter.formatField("99", "");
        assertEquals("", result); // Retourne chaîne vide
    }
    
    @Test
    public void testFormatFieldWithNullValue() {
        // Test avec valeur null
        String result = formatter.formatField("99", null);
        assertEquals("", result); // Retourne chaîne vide
    }
    
    @Test
    public void testParseField() {
        // Test parsing d'un champ
        String data = "000201520400005303952";
        EMVField field = formatter.parseField(data, 0);
        
        assertNotNull(field);
        assertEquals("00", field.getId());
        assertEquals("01", field.getValue());
    }
    
    @Test
    public void testParseAllFields() {
        // Test parsing de plusieurs champs
        String data = "000201520400005303952";
        List<EMVField> fields = formatter.parseAllFields(data);
        
        assertNotNull(fields);
        assertEquals(3, fields.size());
        
        // Vérifier le premier champ
        assertEquals("00", fields.get(0).getId());
        assertEquals("01", fields.get(0).getValue());
        
        // Vérifier le deuxième champ
        assertEquals("52", fields.get(1).getId());
        assertEquals("0000", fields.get(1).getValue());
        
        // Vérifier le troisième champ
        assertEquals("53", fields.get(2).getId());
        assertEquals("952", fields.get(2).getValue());
    }
    
    @Test
    public void testFieldsToMap() {
        // Créer des champs
        List<EMVField> fields = List.of(
            new EMVField("00", "01"),
            new EMVField("52", "0000"),
            new EMVField("53", "952")
        );
        
        // Convertir en Map
        Map<String, String> map = formatter.fieldsToMap(fields);
        
        assertNotNull(map);
        assertEquals(3, map.size());
        assertEquals("01", map.get("00"));
        assertEquals("0000", map.get("52"));
        assertEquals("952", map.get("53"));
    }
    
    @Test
    public void testInvalidFieldId() {
        // Test avec ID invalide (pas 2 caractères)
        assertThrows(IllegalArgumentException.class, () -> {
            formatter.formatField("1", "value");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            formatter.formatField("123", "value");
        });
    }
    
    @Test
    public void testValueTooLong() {
        // Test avec valeur dépassant 99 caractères
        String longValue = "a".repeat(100);
        assertThrows(IllegalArgumentException.class, () -> {
            formatter.formatField("00", longValue);
        });
    }
}
