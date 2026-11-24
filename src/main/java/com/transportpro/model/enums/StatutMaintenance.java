package com.transportpro.model.enums;

public enum StatutMaintenance {
    PLANIFIEE("Planifiée"),
    EN_COURS("En cours"),
    TERMINEE("Terminée"),
    ANNULEE("Annulée");

    private final String libelle;

    StatutMaintenance(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }
}
