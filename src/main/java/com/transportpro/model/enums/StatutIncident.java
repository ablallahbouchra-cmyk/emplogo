package com.transportpro.model.enums;

public enum StatutIncident {
    OUVERT("Ouvert"),
    EN_COURS("En cours"),
    RESOLU("Résolu"),
    CLOTURE("Clôturé");

    private final String libelle;

    StatutIncident(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }
}
