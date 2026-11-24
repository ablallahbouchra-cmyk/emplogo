package com.transportpro.model.enums;

public enum StatutChauffeur {
    ACTIF("Actif"),
    EN_CONGE("En congé"),
    SUSPENDU("Suspendu");

    private final String libelle;

    StatutChauffeur(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }
}
