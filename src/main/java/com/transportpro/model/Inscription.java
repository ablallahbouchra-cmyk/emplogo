package com.transportpro.model;

import com.transportpro.model.enums.StatutInscription;
import com.transportpro.model.enums.StatutPresence;

import java.time.LocalDateTime;

/** Table `inscription` : un employé inscrit à une affectation (+ présence éventuelle). */
public class Inscription {

    private int idInscription;
    private int idEmploye;
    private int idAffectation;
    private LocalDateTime dateInscription;
    private StatutInscription statut = StatutInscription.CONFIRMEE;

    // Relations (remplies par JOIN)
    private Employe employe;
    private Presence presence;

    public Inscription() {
    }

    public Inscription(int idInscription, int idEmploye, int idAffectation,
                       LocalDateTime dateInscription, StatutInscription statut) {
        this.idInscription = idInscription;
        this.idEmploye = idEmploye;
        this.idAffectation = idAffectation;
        this.dateInscription = dateInscription;
        this.statut = statut;
    }

    public StatutPresence getStatutPresence() {
        return presence == null || presence.getStatut() == null ? StatutPresence.NON_RENSEIGNE : presence.getStatut();
    }

    public void enregistrerPresence(StatutPresence statut) {
        if (statut == null) {
            throw new IllegalArgumentException("Le statut de présence ne peut pas être null.");
        }
        if (presence == null) {
            presence = new Presence();
            presence.setIdInscription(idInscription);
        }
        presence.setStatut(statut);
    }

    // ---- Getters / Setters ----
    public int getIdInscription() { return idInscription; }
    public void setIdInscription(int idInscription) { this.idInscription = idInscription; }

    public int getIdEmploye() { return idEmploye; }
    public void setIdEmploye(int idEmploye) { this.idEmploye = idEmploye; }

    public int getIdAffectation() { return idAffectation; }
    public void setIdAffectation(int idAffectation) { this.idAffectation = idAffectation; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public StatutInscription getStatut() { return statut; }
    public void setStatut(StatutInscription statut) { this.statut = statut; }

    public Employe getEmploye() { return employe; }
    public void setEmploye(Employe employe) { this.employe = employe; }

    public Presence getPresence() { return presence; }
    public void setPresence(Presence presence) { this.presence = presence; }
}
