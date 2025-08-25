package com.aveplus.uemoa.qr.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Informations du marchand ou du particulier pour le QR code
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantInfo {
    
    /**
     * Alias/Proxy du compte (UUID ou identifiant unique)
     */
    @NotBlank(message = "L'alias est obligatoire")
    private String alias;
    
    /**
     * Nom du marchand ou du particulier
     */
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 25, message = "Le nom ne doit pas dépasser 25 caractères")
    private String name;
    
    /**
     * Ville du marchand ou du particulier
     */
    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 15, message = "La ville ne doit pas dépasser 15 caractères")
    private String city;
    
    /**
     * Code pays ISO (BF, CI, TG, SN, ML, BJ, GW, NE)
     */
    @NotBlank(message = "Le code pays est obligatoire")
    @Size(min = 2, max = 2, message = "Le code pays doit faire exactement 2 caractères")
    @Pattern(regexp = "^(BF|CI|TG|SN|ML|BJ|GW|NE)$", 
             message = "Code pays invalide. Doit être un pays de l'UEMOA")
    private String countryCode;
    
    /**
     * Code catégorie marchand (MCC) - optionnel
     */
    private String categoryCode;
}
