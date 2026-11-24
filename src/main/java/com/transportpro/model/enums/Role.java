package com.transportpro.model.enums;

public enum Role {
    ADMINISTRATEUR("Administrateur"),
    RESPONSABLE("Responsable"),
    CHAUFFEUR("Chauffeur"),
    EMPLOYE("Employé");

    private final String libelle;

    Role(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() { return libelle; }

    /** Convertit le nom de la table `role` (ADMIN, GESTIONNAIRE, CHAUFFEUR, EMPLOYE). */
    public static Role fromDb(String nom) {
        if (nom == null) return EMPLOYE;
        return switch (nom.toUpperCase()) {
            case "ADMIN", "ADMINISTRATEUR" -> ADMINISTRATEUR;
            case "GESTIONNAIRE", "RESPONSABLE" -> RESPONSABLE;
            case "CHAUFFEUR" -> CHAUFFEUR;
            default -> EMPLOYE;
        };
    }
}
