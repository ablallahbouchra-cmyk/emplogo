package com.transportpro.model;

import com.transportpro.model.enums.StatutPresence;

import java.time.LocalTime;

/** Table `presence` : au plus une ligne par inscription. */
public class Presence {

    private int idPresence;
    private int idInscription;
    private StatutPresence statut;
    private LocalTime heureMontee;
    private String remarque;

    public Presence() {
    }

    public Presence(int idPresence, int idInscription, StatutPresence statut,
                    LocalTime heureMontee, String remarque) {
        this.idPresence = idPresence;
        this.idInscription = idInscription;
        this.statut = statut;
        this.heureMontee = heureMontee;
        this.remarque = remarque;
    }

    public int getIdPresence() { return idPresence; }
    public void setIdPresence(int idPresence) { this.idPresence = idPresence; }

    public int getIdInscription() { return idInscription; }
    public void setIdInscription(int idInscription) { this.idInscription = idInscription; }

    public StatutPresence getStatut() { return statut; }
    public void setStatut(StatutPresence statut) { this.statut = statut; }

    public LocalTime getHeureMontee() { return heureMontee; }
    public void setHeureMontee(LocalTime heureMontee) { this.heureMontee = heureMontee; }

    public String getRemarque() { return remarque; }
    public void setRemarque(String remarque) { this.remarque = remarque; }
}
