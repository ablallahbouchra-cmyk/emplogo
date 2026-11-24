package com.transportpro.model;

import com.transportpro.model.enums.GraviteIncident;
import com.transportpro.model.enums.ModeDeclaration;
import com.transportpro.model.enums.StatutIncident;
import com.transportpro.model.enums.TypeIncident;

import java.time.LocalDateTime;

/** Table `incident` (+ libellés d'affichage remplis par IncidentDAO via JOIN). */
public class Incident {

    private int idIncident;
    private int idAffectation;
    private Integer idCompte;
    private TypeIncident type;
    private GraviteIncident gravite = GraviteIncident.MINEURE;
    private String description;
    private LocalDateTime dateHeure;
    private StatutIncident statut = StatutIncident.OUVERT;
    private ModeDeclaration modeDeclaration = ModeDeclaration.APPLICATION;

    // Affichage
    private String nomTrajet;
    private String immatriculationBus;
    private String nomChauffeur;
    private String loginDeclarant;

    public Incident() {
    }

    public Incident(int idIncident, TypeIncident type, String description, LocalDateTime dateHeure) {
        this.idIncident = idIncident;
        this.type = type;
        this.description = description;
        this.dateHeure = dateHeure;
    }

    public void creer() {
        if (this.dateHeure == null) {
            this.dateHeure = LocalDateTime.now();
        }
    }

    // ---- Getters / Setters ----
    public int getIdIncident() { return idIncident; }
    public void setIdIncident(int idIncident) { this.idIncident = idIncident; }

    public int getIdAffectation() { return idAffectation; }
    public void setIdAffectation(int idAffectation) { this.idAffectation = idAffectation; }

    public Integer getIdCompte() { return idCompte; }
    public void setIdCompte(Integer idCompte) { this.idCompte = idCompte; }

    public TypeIncident getType() { return type; }
    public void setType(TypeIncident type) { this.type = type; }

    public GraviteIncident getGravite() { return gravite; }
    public void setGravite(GraviteIncident gravite) { this.gravite = gravite; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }

    public StatutIncident getStatut() { return statut; }
    public void setStatut(StatutIncident statut) { this.statut = statut; }

    public ModeDeclaration getModeDeclaration() { return modeDeclaration; }
    public void setModeDeclaration(ModeDeclaration modeDeclaration) { this.modeDeclaration = modeDeclaration; }

    public String getNomTrajet() { return nomTrajet; }
    public void setNomTrajet(String nomTrajet) { this.nomTrajet = nomTrajet; }

    public String getImmatriculationBus() { return immatriculationBus; }
    public void setImmatriculationBus(String immatriculationBus) { this.immatriculationBus = immatriculationBus; }

    public String getNomChauffeur() { return nomChauffeur; }
    public void setNomChauffeur(String nomChauffeur) { this.nomChauffeur = nomChauffeur; }

    public String getLoginDeclarant() { return loginDeclarant; }
    public void setLoginDeclarant(String loginDeclarant) { this.loginDeclarant = loginDeclarant; }
}
