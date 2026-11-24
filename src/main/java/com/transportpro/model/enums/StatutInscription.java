package com.transportpro.model.enums;

public enum StatutInscription {
    CONFIRMEE("Confirmée"),
    ANNULEE("Annulée");

    private final String libelle;

    StatutInscription(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }
}
