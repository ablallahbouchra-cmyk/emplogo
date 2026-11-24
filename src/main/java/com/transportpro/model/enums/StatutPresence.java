package com.transportpro.model.enums;

/** PRESENT / ABSENT / RETARD = valeurs de la table `presence` ; NON_RENSEIGNE = aucune ligne. */
public enum StatutPresence {
    PRESENT("Présent"),
    ABSENT("Absent"),
    RETARD("Retard"),
    NON_RENSEIGNE("Non renseigné");

    private final String libelle;

    StatutPresence(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }
}
