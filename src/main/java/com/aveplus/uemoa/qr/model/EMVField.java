package com.aveplus.uemoa.qr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Représentation d'un champ EMV
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EMVField {
    /**
     * Identifiant du champ (2 caractères)
     */
    private String id;
    
    /**
     * Valeur du champ
     */
    private String value;
    
    /**
     * Sous-champs (pour les champs composites)
     */
    private List<EMVField> subFields = new ArrayList<>();
    
    /**
     * Constructeur simple sans sous-champs
     */
    public EMVField(String id, String value) {
        this.id = id;
        this.value = value;
        this.subFields = new ArrayList<>();
    }
    
    /**
     * Ajoute un sous-champ
     */
    public void addSubField(EMVField subField) {
        if (this.subFields == null) {
            this.subFields = new ArrayList<>();
        }
        this.subFields.add(subField);
    }
    
    /**
     * Recherche un sous-champ par son ID
     */
    public EMVField findSubFieldById(String id) {
        if (subFields == null) return null;
        return subFields.stream()
            .filter(field -> field.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
}
