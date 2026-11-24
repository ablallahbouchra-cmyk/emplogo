package com.transportpro.model.enums;

public enum TypeMaintenance {
    PREVENTIVE("Préventive"),
    CORRECTIVE("Corrective");

    private final String libelle;

    TypeMaintenance(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }
}
