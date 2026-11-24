package com.transportpro.model.enums;

public enum GraviteIncident {
    MINEURE("Mineure"),
    MODEREE("Modérée"),
    GRAVE("Grave"),
    CRITIQUE("Critique");

    private final String libelle;

    GraviteIncident(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }
}
