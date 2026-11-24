package com.transportpro.model.enums;

public enum ModeDeclaration {
    APPLICATION("Application"),
    TELEPHONE("Téléphone"),
    RAPPORT_PAPIER("Rapport papier"),
    SIGNALEMENT_CHAUFFEUR("Signalement chauffeur");

    private final String libelle;

    ModeDeclaration(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }
}
