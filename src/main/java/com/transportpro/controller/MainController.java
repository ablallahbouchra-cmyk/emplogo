package com.transportpro.controller;

import com.transportpro.Ctx;
import com.transportpro.dao.AffectationDAO;
import com.transportpro.dao.BusDAO;
import com.transportpro.dao.ChauffeurDAO;
import com.transportpro.dao.CompteUtilisateurDAO;
import com.transportpro.dao.EmployeDAO;
import com.transportpro.dao.IncidentDAO;
import com.transportpro.dao.InscriptionDAO;
import com.transportpro.dao.MaintenanceDAO;
import com.transportpro.dao.PresenceDAO;
import com.transportpro.dao.TrajetDAO;
import com.transportpro.model.Affectation;
import com.transportpro.model.Bus;
import com.transportpro.model.Chauffeur;
import com.transportpro.model.CompteUtilisateur;
import com.transportpro.model.Employe;
import com.transportpro.model.Incident;
import com.transportpro.model.Inscription;
import com.transportpro.model.Maintenance;
import com.transportpro.model.Presence;
import com.transportpro.model.Trajet;
import com.transportpro.model.enums.ModeDeclaration;
import com.transportpro.model.enums.Role;
import com.transportpro.model.enums.StatutAffectation;
import com.transportpro.model.enums.StatutIncident;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;

/**
 * Controller applicatif principal.
 * Fait le lien entre les pages (View) et les DAO.
 * Toute exception SQL (dont les messages SIGNAL des triggers) est transformée en toast lisible.
 */
public class MainController {

    public enum StatutAuth { OK, REFUSE, BDD_INDISPONIBLE }

    public record ResultatAuth(StatutAuth statut, CompteUtilisateur compte, String message) {}

    private static final String ACCES_REFUSE = "Identifiant ou mot de passe incorrect. Accès refusé.";

    private final Ctx ctx;
    private final EmployeDAO employeDAO = new EmployeDAO();
    private final BusDAO busDAO = new BusDAO();
    private final ChauffeurDAO chauffeurDAO = new ChauffeurDAO();
    private final TrajetDAO trajetDAO = new TrajetDAO();
    private final AffectationDAO affectationDAO = new AffectationDAO();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();
    private final PresenceDAO presenceDAO = new PresenceDAO();
    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final MaintenanceDAO maintenanceDAO = new MaintenanceDAO();
    private final CompteUtilisateurDAO compteDAO = new CompteUtilisateurDAO();

    private CompteUtilisateur compteConnecte;

    public MainController(Ctx ctx) {
        this.ctx = ctx;
    }

    private void erreur(String contexte, SQLException e) {
        ctx.toastErreur(contexte + e.getMessage());
    }

    // ---------------- Authentification ----------------

    /**
     * Vérifie login / mot de passe contre `compte_utilisateur` (hash BCrypt).
     * Les comptes de test ont un hash PLACEHOLDER : la première connexion initialise
     * alors le mot de passe avec le hash BCrypt de celui saisi.
     */
    public ResultatAuth authentifier(String login, String mdp) {
        if (login == null || login.isBlank() || mdp == null || mdp.isEmpty()) {
            return new ResultatAuth(StatutAuth.REFUSE, null, ACCES_REFUSE);
        }
        try {
            CompteUtilisateur compte = compteDAO.findByLogin(login.trim());
            if (compte == null || !compte.isActif()) {
                return new ResultatAuth(StatutAuth.REFUSE, null, ACCES_REFUSE);
            }
            String hash = compte.getMotDePasseHache();
            if (hash == null || hash.contains("PLACEHOLDER")) {
                String nouveau = BCrypt.hashpw(mdp, BCrypt.gensalt());
                compteDAO.updateMotDePasse(compte.getIdCompte(), nouveau);
                compte.setMotDePasseHache(nouveau);
            } else if (!verifierHash(mdp, hash)) {
                return new ResultatAuth(StatutAuth.REFUSE, null, ACCES_REFUSE);
            }
            if (compte.getRole() == Role.EMPLOYE) {
                return new ResultatAuth(StatutAuth.REFUSE, null,
                        "Le profil Employé n’a pas accès à l’application de gestion.");
            }
            compteDAO.majDerniereConnexion(compte.getIdCompte());
            compteConnecte = compte;
            return new ResultatAuth(StatutAuth.OK, compte, null);
        } catch (SQLException e) {
            return new ResultatAuth(StatutAuth.BDD_INDISPONIBLE, null,
                    "Base de données indisponible : " + e.getMessage());
        }
    }

    private static boolean verifierHash(String mdp, String hash) {
        try {
            return BCrypt.checkpw(mdp, hash);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public CompteUtilisateur getCompteConnecte() { return compteConnecte; }
    public void setCompteConnecte(CompteUtilisateur compte) { this.compteConnecte = compte; }

    // ---------------- Employés ----------------

    public List<Employe> chargerEmployes() {
        try {
            return employeDAO.findAll();
        } catch (SQLException e) {
            erreur("Erreur employés : ", e);
            return List.of();
        }
    }

    public void ajouterEmploye(Employe e) {
        try {
            employeDAO.insert(e);
            ctx.toast("Employé ajouté : " + e.getNomComplet());
        } catch (SQLException ex) {
            erreur("Échec ajout : ", ex);
        }
    }

    public void supprimerEmploye(int id) {
        try {
            employeDAO.delete(id);
            ctx.toast("Employé supprimé");
        } catch (SQLException ex) {
            erreur("Échec suppression : ", ex);
        }
    }

    // ---------------- Bus ----------------

    public List<Bus> chargerBus() {
        try {
            return busDAO.findAll();
        } catch (SQLException e) {
            erreur("Erreur bus : ", e);
            return List.of();
        }
    }

    public boolean ajouterBus(Bus b) {
        try {
            busDAO.insert(b);
            ctx.toast("Bus ajouté : " + b.getImmatriculation());
            return true;
        } catch (SQLException e) {
            erreur("Échec ajout du bus : ", e);
            return false;
        }
    }

    public boolean modifierBus(Bus b) {
        try {
            busDAO.update(b);
            ctx.toast("Bus modifié : " + b.getImmatriculation());
            return true;
        } catch (SQLException e) {
            erreur("Échec modification du bus : ", e);
            return false;
        }
    }

    public boolean supprimerBus(int idBus) {
        try {
            busDAO.delete(idBus);
            ctx.toast("Bus supprimé");
            return true;
        } catch (SQLException e) {
            erreur("Échec suppression du bus : ", e);
            return false;
        }
    }

    public List<Maintenance> chargerMaintenances() {
        try {
            return maintenanceDAO.findAll();
        } catch (SQLException e) {
            erreur("Erreur maintenances : ", e);
            return List.of();
        }
    }

    public boolean ajouterMaintenance(Maintenance m) {
        try {
            maintenanceDAO.insert(m);
            ctx.toast("Maintenance enregistrée");
            return true;
        } catch (SQLException e) {
            erreur("", e);
            return false;
        }
    }

    // ---------------- Chauffeurs ----------------

    public List<Chauffeur> chargerChauffeurs() {
        try {
            return chauffeurDAO.findAll();
        } catch (SQLException e) {
            erreur("Erreur chauffeurs : ", e);
            return List.of();
        }
    }

    /** Chauffeur lié au compte connecté (null si le compte n'est pas celui d'un chauffeur). */
    public Chauffeur chauffeurConnecte() {
        if (compteConnecte == null || compteConnecte.getIdEmploye() == null) return null;
        try {
            return chauffeurDAO.findByEmploye(compteConnecte.getIdEmploye());
        } catch (SQLException e) {
            erreur("Erreur chauffeur : ", e);
            return null;
        }
    }

    // ---------------- Trajets / affectations ----------------

    public List<Trajet> chargerTrajets() {
        try {
            return trajetDAO.findAll();
        } catch (SQLException e) {
            erreur("Erreur trajets : ", e);
            return List.of();
        }
    }

    public List<Affectation> chargerAffectations() {
        try {
            return affectationDAO.findAll();
        } catch (SQLException e) {
            erreur("Erreur affectations : ", e);
            return List.of();
        }
    }

    public List<Affectation> chargerAffectationsChauffeur(int idChauffeur) {
        try {
            return affectationDAO.findByChauffeur(idChauffeur);
        } catch (SQLException e) {
            erreur("Erreur planning : ", e);
            return List.of();
        }
    }

    /** Les contrôles (bus disponible, chauffeur actif, chevauchements...) sont faits par les triggers. */
    public boolean creerAffectation(Affectation a) {
        try {
            affectationDAO.insert(a);
            ctx.toast("Affectation confirmée : " + a.getTrajet().getNom());
            return true;
        } catch (SQLException e) {
            erreur("", e);
            return false;
        }
    }

    public boolean changerStatutAffectation(int idAffectation, StatutAffectation statut) {
        try {
            affectationDAO.updateStatut(idAffectation, statut);
            ctx.toast("Affectation " + statut.getLibelle().toLowerCase());
            return true;
        } catch (SQLException e) {
            erreur("", e);
            return false;
        }
    }

    // ---------------- Inscriptions / présences ----------------

    public List<Inscription> chargerInscriptions(int idAffectation) {
        try {
            return inscriptionDAO.findByAffectation(idAffectation);
        } catch (SQLException e) {
            erreur("Erreur inscriptions : ", e);
            return List.of();
        }
    }

    public boolean inscrireEmploye(int idEmploye, int idAffectation) {
        Inscription i = new Inscription();
        i.setIdEmploye(idEmploye);
        i.setIdAffectation(idAffectation);
        try {
            inscriptionDAO.insert(i);
            ctx.toast("Employé inscrit");
            return true;
        } catch (SQLException e) {
            erreur("", e);
            return false;
        }
    }

    public boolean annulerInscription(int idInscription) {
        try {
            inscriptionDAO.annuler(idInscription);
            ctx.toast("Inscription annulée");
            return true;
        } catch (SQLException e) {
            erreur("", e);
            return false;
        }
    }

    public boolean enregistrerPresences(List<Presence> presences) {
        try {
            presenceDAO.upsertAll(presences);
            ctx.toast("Présences enregistrées");
            return true;
        } catch (SQLException e) {
            erreur("Échec enregistrement des présences : ", e);
            return false;
        }
    }

    // ---------------- Incidents ----------------

    public List<Incident> chargerIncidents() {
        try {
            return incidentDAO.findAll();
        } catch (SQLException e) {
            erreur("Erreur incidents : ", e);
            return List.of();
        }
    }

    public boolean declarerIncident(Incident i) {
        if (i.getIdCompte() == null && compteConnecte != null) {
            i.setIdCompte(compteConnecte.getIdCompte());
        }
        if (i.getModeDeclaration() == null) {
            i.setModeDeclaration(ModeDeclaration.APPLICATION);
        }
        i.creer();
        try {
            incidentDAO.insert(i);
            ctx.toast("Incident enregistré");
            return true;
        } catch (SQLException e) {
            erreur("Échec déclaration : ", e);
            return false;
        }
    }

    public boolean changerStatutIncident(int idIncident, StatutIncident statut) {
        try {
            incidentDAO.updateStatut(idIncident, statut);
            ctx.toast("Incident " + statut.getLibelle().toLowerCase());
            return true;
        } catch (SQLException e) {
            erreur("", e);
            return false;
        }
    }

    // ---------------- Comptes ----------------

    public List<CompteUtilisateur> chargerComptes() {
        try {
            return compteDAO.findAll();
        } catch (SQLException e) {
            erreur("Erreur comptes : ", e);
            return List.of();
        }
    }
}
