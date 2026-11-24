package com.transportpro.model;

import com.transportpro.model.enums.StatutBus;

import java.time.LocalDate;
import java.time.LocalTime;

public class Bus {

    private int idBus;
    private String immatriculation;
    private String marque;
    private String modele;
    private int capacite;
    private StatutBus statut = StatutBus.DISPONIBLE;

    public Bus() {
    }

    public Bus(int idBus, String immatriculation, int capacite, StatutBus statut) {
        this(idBus, immatriculation, null, null, capacite, statut);
    }

    public Bus(int idBus, String immatriculation, String marque, String modele, int capacite, StatutBus statut) {
        this.idBus = idBus;
        this.immatriculation = immatriculation;
        this.marque = marque;
        this.modele = modele;
        this.capacite = capacite;
        this.statut = statut;
    }

    /**
     * Un bus est disponible s'il est DISPONIBLE (les chevauchements et maintenances
     * sont contrôlés par les triggers MySQL).
     */
    public boolean estDisponible(LocalDate date, LocalTime heure) {
        return statut == StatutBus.DISPONIBLE;
    }

    public boolean peutAccueillir(int nbInscrits) {
        return nbInscrits < capacite;
    }

    public String getMarqueModele() {
        String s = ((marque == null ? "" : marque) + " " + (modele == null ? "" : modele)).trim();
        return s.isEmpty() ? "—" : s;
    }

    // ---- Getters / Setters ----
    public int getIdBus() { return idBus; }
    public void setIdBus(int idBus) { this.idBus = idBus; }

    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }

    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }

    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public StatutBus getStatut() { return statut; }
    public void setStatut(StatutBus statut) { this.statut = statut; }
}
