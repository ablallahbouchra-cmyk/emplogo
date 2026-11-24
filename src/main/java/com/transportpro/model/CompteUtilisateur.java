package com.transportpro.model;

import com.transportpro.model.enums.Role;

import java.time.LocalDateTime;

/** Table `compte_utilisateur` (+ nom du rôle et de l'employé lié, via JOIN). */
public class CompteUtilisateur extends Utilisateur {

    private LocalDateTime derniereConnexion;
    private int idRole;
    private String nomRole;
    private Integer idEmploye;
    private String nomEmploye;
    private String prenomEmploye;

    public CompteUtilisateur() {
    }

    public CompteUtilisateur(int idCompte, String login, String motDePasseHash, boolean actif,
                             LocalDateTime derniereConnexion, int idRole, String nomRole, Integer idEmploye) {
        super(idCompte, login, motDePasseHash, actif, Role.fromDb(nomRole));
        this.derniereConnexion = derniereConnexion;
        this.idRole = idRole;
        this.nomRole = nomRole;
        this.idEmploye = idEmploye;
    }

    public int getIdCompte() { return getIdUtilisateur(); }

    /** Nom affiché dans l'application : « Prénom Nom » de l'employé lié, sinon le login. */
    public String getNomAffiche() {
        if (nomEmploye == null) return getLogin();
        return prenomEmploye == null ? nomEmploye : prenomEmploye + " " + nomEmploye;
    }

    public LocalDateTime getDerniereConnexion() { return derniereConnexion; }
    public void setDerniereConnexion(LocalDateTime derniereConnexion) { this.derniereConnexion = derniereConnexion; }

    public int getIdRole() { return idRole; }
    public void setIdRole(int idRole) { this.idRole = idRole; }

    public String getNomRole() { return nomRole; }
    public void setNomRole(String nomRole) {
        this.nomRole = nomRole;
        setRole(Role.fromDb(nomRole));
    }

    public Integer getIdEmploye() { return idEmploye; }
    public void setIdEmploye(Integer idEmploye) { this.idEmploye = idEmploye; }

    public String getNomEmploye() { return nomEmploye; }
    public void setNomEmploye(String nomEmploye) { this.nomEmploye = nomEmploye; }

    public String getPrenomEmploye() { return prenomEmploye; }
    public void setPrenomEmploye(String prenomEmploye) { this.prenomEmploye = prenomEmploye; }
}
