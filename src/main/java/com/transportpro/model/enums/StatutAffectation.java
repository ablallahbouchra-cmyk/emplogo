package com.transportpro.model.enums;

public enum StatutAffectation {
    PLANIFIEE("Planifiée"),
    EN_COURS("En cours"),
    TERMINEE("Terminée"),
    ANNULEE("Annulée");

    private final String libelle;

    StatutAffectation(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }
}
