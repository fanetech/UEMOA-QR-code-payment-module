package com.aveplus.uemoa.qr.utils;

import com.aveplus.uemoa.qr.model.EMVField;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Formateur pour les champs EMV selon la spécification EMVCo
 */
@Component
public class EMVFormatter {
    
    /**
     * Formate un champ EMV avec son ID, sa longueur et sa valeur
     * 
     * @param id L'identifiant du champ (2 caractères)
     * @param value La valeur du champ
     * @return Le champ formaté
     */
    public String formatField(String id, String value) {
        if (id == null || id.length() != 2) {
            throw new IllegalArgumentException("L'ID du champ doit faire exactement 2 caractères");
        }
        
        if (value == null || value.isEmpty()) {
            return "";
        }
        
        if (value.length() > 99) {
            throw new IllegalArgumentException("La valeur du champ ne peut pas dépasser 99 caractères");
        }
        
        String length = String.format("%02d", value.length());
        return id + length + value;
    }
    
    /**
     * Formate un sous-champ EMV
     * 
     * @param id L'identifiant du sous-champ (2 caractères)
     * @param value La valeur du sous-champ
     * @return Le sous-champ formaté
     */
    public String formatSubField(String id, String value) {
        return formatField(id, value);
    }
    
    /**
     * Parse un champ EMV à partir d'une position donnée
     * 
     * @param data Les données EMV complètes
     * @param offset La position de départ
     * @return Le champ EMV parsé ou null si impossible
     */
    public EMVField parseField(String data, int offset) {
        if (data == null || offset < 0 || offset + 4 > data.length()) {
            return null;
        }
        
        try {
            String id = data.substring(offset, offset + 2);
            int length = Integer.parseInt(data.substring(offset + 2, offset + 4));
            
            if (offset + 4 + length > data.length()) {
                return null;
            }
            
            String value = data.substring(offset + 4, offset + 4 + length);
            return new EMVField(id, value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Parse tous les champs EMV d'une chaîne de données
     * 
     * @param data Les données EMV complètes
     * @return La liste des champs EMV
     */
    public List<EMVField> parseAllFields(String data) {
        List<EMVField> fields = new ArrayList<>();
        
        if (data == null || data.isEmpty()) {
            return fields;
        }
        
        int offset = 0;
        while (offset < data.length() - 4) { // -4 pour le CRC
            EMVField field = parseField(data, offset);
            if (field == null) {
                break;
            }
            
            fields.add(field);
            offset += 4 + field.getValue().length();
            
            // Arrêt avant le CRC (champ 63)
            if ("63".equals(field.getId())) {
                break;
            }
        }
        
        return fields;
    }
    
    /**
     * Parse les sous-champs d'une valeur de champ composite
     * 
     * @param compositeValue La valeur contenant des sous-champs
     * @return La liste des sous-champs
     */
    public List<EMVField> parseSubFields(String compositeValue) {
        return parseAllFields(compositeValue);
    }
    
    /**
     * Construit une chaîne de données EMV à partir d'une map de champs
     * 
     * @param fields Map des champs (ID -> valeur)
     * @param excludeCRC Si true, n'inclut pas le champ CRC (63)
     * @return La chaîne EMV formatée
     */
    public String buildEMVString(Map<String, String> fields, boolean excludeCRC) {
        if (fields == null || fields.isEmpty()) {
            return "";
        }
        
        // Utilise TreeMap pour trier les champs par ID
        TreeMap<String, String> sortedFields = new TreeMap<>(fields);
        
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedFields.entrySet()) {
            String id = entry.getKey();
            
            // Exclut le CRC si demandé
            if (excludeCRC && "63".equals(id)) {
                continue;
            }
            
            String formatted = formatField(id, entry.getValue());
            result.append(formatted);
        }
        
        return result.toString();
    }
    
    /**
     * Convertit une liste de champs EMV en Map
     * 
     * @param fields Liste des champs EMV
     * @return Map des champs (ID -> valeur)
     */
    public Map<String, String> fieldsToMap(List<EMVField> fields) {
        Map<String, String> map = new TreeMap<>();
        
        if (fields != null) {
            for (EMVField field : fields) {
                map.put(field.getId(), field.getValue());
            }
        }
        
        return map;
    }
}
