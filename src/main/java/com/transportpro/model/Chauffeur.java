package com.transportpro.model;

import com.transportpro.model.enums.Role;
import com.transportpro.model.enums.StatutChauffeur;
import com.transportpro.model.enums.TypeIncident;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Table `chauffeur` ; nom, prénom et téléphone proviennent de la table `employe` (JOIN). */
public class Chauffeur extends Utilisateur {
    private int idChauffeur;
    private int idEmploye;
    private String nom;
    private String prenom;
    private String telephone;
    private String numeroPermis;
    private LocalDate validitePermis;
    private StatutChauffeur statut = StatutChauffeur.ACTIF;
    private List<Affectation> affectations = new ArrayList<>();

    public Chauffeur() {
        super();
        setRole(Role.CHAUFFEUR);
    }

    public Chauffeur(int idChauffeur, int idEmploye, String nom, String prenom, String telephone,
                     String numeroPermis, LocalDate validitePermis, StatutChauffeur statut) {
        this();
        this.idChauffeur = idChauffeur;
        this.idEmploye = idEmploye;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.numeroPermis = numeroPermis;
        this.validitePermis = validitePermis;
        this.statut = statut;
    }

    public String getNomComplet() {
        return prenom == null ? nom : prenom + " " + nom;
    }

    public boolean isDisponible() {
        return statut == StatutChauffeur.ACTIF;
    }

    public boolean permisValide(LocalDate date) {
        return validitePermis != null && date != null && !validitePermis.isBefore(date);
    }

    public List<Affectation> consulterPlanning() {
        return new ArrayList<>(affectations);
    }

    public Incident signalerIncident(TypeIncident type, String description) {
        Incident i = new Incident();
        i.setType(type);
        i.setDescription(description);
        i.setDateHeure(LocalDateTime.now());
        i.creer();
        return i;
    }

    // ---- Getters / Setters ----
    public int getIdChauffeur() { return idChauffeur; }
    public void setIdChauffeur(int idChauffeur) { this.idChauffeur = idChauffeur; }

    public int getIdEmploye() { return idEmploye; }
    public void setIdEmploye(int idEmploye) { this.idEmploye = idEmploye; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getNumeroPermis() { return numeroPermis; }
    public void setNumeroPermis(String numeroPermis) { this.numeroPermis = numeroPermis; }

    public LocalDate getValiditePermis() { return validitePermis; }
    public void setValiditePermis(LocalDate validitePermis) { this.validitePermis = validitePermis; }

    public StatutChauffeur getStatut() { return statut; }
    public void setStatut(StatutChauffeur statut) { this.statut = statut; }

    public List<Affectation> getAffectations() { return affectations; }
    public void setAffectations(List<Affectation> affectations) { this.affectations = affectations; }
}
