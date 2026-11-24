package com.transportpro.model;

import java.util.Objects;

/** Un trajet est un itinéraire (sans horaire) : la planification est portée par {@link Affectation}. */
public class Trajet {

    private int idTrajet;
    private String nom;
    private String lieuDepart;
    private String lieuArrivee;

    public Trajet() {
    }

    public Trajet(int idTrajet, String nom, String lieuDepart, String lieuArrivee) {
        this.idTrajet = idTrajet;
        this.nom = nom;
        this.lieuDepart = lieuDepart;
        this.lieuArrivee = lieuArrivee;
    }

    public String getItineraire() {
        return lieuDepart + " → " + lieuArrivee;
    }

    // ---- Getters / Setters ----
    public int getIdTrajet() { return idTrajet; }
    public void setIdTrajet(int idTrajet) { this.idTrajet = idTrajet; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getLieuDepart() { return lieuDepart; }
    public void setLieuDepart(String lieuDepart) { this.lieuDepart = lieuDepart; }

    public String getLieuArrivee() { return lieuArrivee; }
    public void setLieuArrivee(String lieuArrivee) { this.lieuArrivee = lieuArrivee; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Trajet t && t.idTrajet == idTrajet;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTrajet);
    }

    @Override
    public String toString() {
        return nom;
    }
}
