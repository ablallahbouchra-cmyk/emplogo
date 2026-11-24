package com.transportpro.model.enums;

public enum StatutBus {
    DISPONIBLE("Disponible"),
    EN_MAINTENANCE("En maintenance"),
    HORS_SERVICE("Hors service");

    private final String libelle;

    StatutBus(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }
}
