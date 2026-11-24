package com.transportpro.model;

import com.transportpro.model.enums.StatutAffectation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Un trajet programmé à une date, avec un bus et un chauffeur (table `affectation`). */
public class Affectation {

    private int idAffectation;
    private Trajet trajet;
    private Bus bus;
    private Chauffeur chauffeur;
    private LocalDate dateTrajet;
    private LocalTime heureDepart;
    private LocalTime heureArrivee;
    private StatutAffectation statut = StatutAffectation.PLANIFIEE;

    /** Nombre d'inscriptions CONFIRMEE (calculé par le DAO). */
    private int nbInscrits;
    private List<Inscription> inscriptions = new ArrayList<>();

    public Affectation() {
    }

    public Affectation(int idAffectation, Trajet trajet, Bus bus, Chauffeur chauffeur,
                       LocalDate dateTrajet, LocalTime heureDepart, LocalTime heureArrivee,
                       StatutAffectation statut) {
        this.idAffectation = idAffectation;
        this.trajet = trajet;
        this.bus = bus;
        this.chauffeur = chauffeur;
        this.dateTrajet = dateTrajet;
        this.heureDepart = heureDepart;
        this.heureArrivee = heureArrivee;
        this.statut = statut;
    }

    public boolean estComplet() {
        return bus != null && nbInscrits >= bus.getCapacite();
    }

    /** Même règle que le trigger : début1 < fin2 ET début2 < fin1, même date. */
    public boolean chevauche(Affectation autre) {
        if (autre == null || dateTrajet == null || !dateTrajet.equals(autre.dateTrajet)) return false;
        return heureDepart.isBefore(autre.heureArrivee) && autre.heureDepart.isBefore(heureArrivee);
    }

    // ---- Getters / Setters ----
    public int getIdAffectation() { return idAffectation; }
    public void setIdAffectation(int idAffectation) { this.idAffectation = idAffectation; }

    public Trajet getTrajet() { return trajet; }
    public void setTrajet(Trajet trajet) { this.trajet = trajet; }

    public Bus getBus() { return bus; }
    public void setBus(Bus bus) { this.bus = bus; }

    public Chauffeur getChauffeur() { return chauffeur; }
    public void setChauffeur(Chauffeur chauffeur) { this.chauffeur = chauffeur; }

    public LocalDate getDateTrajet() { return dateTrajet; }
    public void setDateTrajet(LocalDate dateTrajet) { this.dateTrajet = dateTrajet; }

    public LocalTime getHeureDepart() { return heureDepart; }
    public void setHeureDepart(LocalTime heureDepart) { this.heureDepart = heureDepart; }

    public LocalTime getHeureArrivee() { return heureArrivee; }
    public void setHeureArrivee(LocalTime heureArrivee) { this.heureArrivee = heureArrivee; }

    public StatutAffectation getStatut() { return statut; }
    public void setStatut(StatutAffectation statut) { this.statut = statut; }

    public int getNbInscrits() { return nbInscrits; }
    public void setNbInscrits(int nbInscrits) { this.nbInscrits = nbInscrits; }

    public List<Inscription> getInscriptions() { return inscriptions; }
    public void setInscriptions(List<Inscription> inscriptions) { this.inscriptions = inscriptions; }
}
