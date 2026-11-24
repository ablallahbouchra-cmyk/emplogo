package com.transportpro.model.enums;

public enum TypeIncident {
    PANNE("Panne"),
    ACCIDENT("Accident"),
    RETARD("Retard"),
    COMPORTEMENT("Comportement"),
    AUTRE("Autre");

    private final String libelle;

    TypeIncident(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }
}
