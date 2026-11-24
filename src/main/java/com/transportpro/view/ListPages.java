package com.transportpro.view;

import com.transportpro.controller.MainController;
import com.transportpro.model.Affectation;
import com.transportpro.model.Bus;
import com.transportpro.model.Chauffeur;
import com.transportpro.model.CompteUtilisateur;
import com.transportpro.model.Employe;
import com.transportpro.model.Incident;
import com.transportpro.model.enums.StatutAffectation;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import com.transportpro.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
/** Écrans « liste » : Employés, Bus, Chauffeurs, Trajets, Incidents, Comptes. */
public final class ListPages {
    private ListPages() {}

    private record Cfg(String action, String[] headers, String[][] rows,
                       int badgeCol, String note, int total) {}

    public static Node build(Page p, Ctx ctx, MainController controller) {
        Cfg c = switch (p) {

            // Employés : données chargées depuis MySQL via le controller
            case EMPLOYEES -> {
                String[][] rows = controller.chargerEmployes().stream()
                        .map(ListPages::employeToRow)
                        .toArray(String[][]::new);
                yield new Cfg("Ajouter un employé",
                        new String[]{"Matricule", "Nom", "Email", "Téléphone",
                                "Point d’embarquement", "Statut"},
                        rows, 5, null, rows.length);
            }

            case BUSES -> {
                List<Affectation> affectations = controller.chargerAffectations();
                String[][] rows = controller.chargerBus().stream()
                        .map(b -> busToRow(b, affectations))
                        .toArray(String[][]::new);
                yield new Cfg("Ajouter un bus",
                        new String[]{"Immatriculation", "Marque / modèle", "Capacité",
                                "Prochain trajet", "Statut"},
                        rows, 4,
                        "Un bus en maintenance ou hors service ne peut pas être affecté.",
                        rows.length);
            }

            case DRIVERS -> {
                String[][] rows = controller.chargerChauffeurs().stream()
                        .map(ListPages::chauffeurToRow)
                        .toArray(String[][]::new);
                yield new Cfg("Ajouter un chauffeur",
                        new String[]{"Nom", "Téléphone", "N° de permis",
                                "Validité du permis", "Statut"},
                        rows, 4, null, rows.length);
            }

            case TRIPS -> {
                String[][] rows = controller.chargerAffectations().stream()
                        .map(ListPages::affectationToRow)
                        .toArray(String[][]::new);
                yield new Cfg("Créer un trajet",
                        new String[]{"ID", "Date", "Départ", "Trajet",
                                "Bus", "Chauffeur", "Inscrits", "Statut"},
                        rows, 7, null, rows.length);
            }

            case INCIDENTS -> {
                String[][] rows = controller.chargerIncidents().stream()
                        .map(ListPages::incidentToRow)
                        .toArray(String[][]::new);
                yield new Cfg("Enregistrer un incident",
                        new String[]{"Date / heure", "Type", "Gravité", "Trajet", "Bus",
                                "Chauffeur", "Statut", "Description"},
                        rows, 1, null, rows.length);
            }

            case ACCOUNTS -> {
                String[][] rows = controller.chargerComptes().stream()
                        .map(ListPages::compteToRow)
                        .toArray(String[][]::new);
                yield new Cfg("Créer un compte",
                        new String[]{"Identifiant", "Employé", "Rôle",
                                "Dernière connexion", "Statut"},
                        rows, 4, null, rows.length);
            }

            default -> throw new IllegalArgumentException(
                    "Page non gérée : " + p);
        };

        VBox card = Ui.card();
        card.getStyleClass().add("main-card");

        if (c.note() != null) {
            HBox banner = new HBox(
                    8,
                    Ui.icon("incidents", 17, "#8A5A00"),
                    Ui.label(c.note(), "info-text")
            );
            banner.getStyleClass().add("info-banner");
            banner.setAlignment(Pos.CENTER_LEFT);
            card.getChildren().add(banner);
        }

        HBox filters = new HBox(
                8,
                Ui.search("Rechercher...", 210),
                Ui.combo("Tous les statuts", "Actif")
        );
        filters.setAlignment(Pos.CENTER_LEFT);

        if (p == Page.TRIPS) {
            DatePicker d = new DatePicker();
            d.setPromptText("jj/mm/aaaa");
            filters.getChildren().add(d);
        }

        HBox toolbar = new HBox(
                filters,
                Ui.spacer(),
                Ui.button(c.action(), "primary", "plus",
                        () -> ctx.openModal(c.action()))
        );
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(0, 0, 15, 0));

        card.getChildren().addAll(
                toolbar,
                Ui.table(c.headers(), c.rows(), c.badgeCol(),
                        () -> ctx.openModal("Détail — " + p.title)),
                pagination(c.rows().length, c.total())
        );

        return Ui.page(card);
    }

    
    // ----- Mappers Model → String[] pour l'affichage -----
    private static String[] employeToRow(Employe e) {
        return new String[]{
                e.getMatricule(),
                e.getNomComplet(),
                Fmt.orDash(e.getEmail()),
                Fmt.orDash(e.getTelephone()),
                Fmt.orDash(e.getAdresse()),
                e.isActif() ? "Actif" : "Inactif"
        };
    }

    private static String[] busToRow(Bus b, List<Affectation> affectations) {
        LocalDateTime now = LocalDateTime.now();
        String prochain = affectations.stream()
                .filter(a -> a.getBus().getIdBus() == b.getIdBus())
                .filter(a -> a.getStatut() == StatutAffectation.PLANIFIEE || a.getStatut() == StatutAffectation.EN_COURS)
                .filter(a -> !LocalDateTime.of(a.getDateTrajet(), a.getHeureDepart()).isBefore(now))
                .findFirst()
                .map(a -> Fmt.date(a.getDateTrajet()) + " • " + Fmt.time(a.getHeureDepart()))
                .orElse("—");
        return new String[]{
                b.getImmatriculation(),
                b.getMarqueModele(),
                b.getCapacite() + " places",
                prochain,
                b.getStatut().getLibelle()
        };
    }

    private static String[] chauffeurToRow(Chauffeur c) {
        boolean expire = c.getValiditePermis() != null && c.getValiditePermis().isBefore(LocalDate.now());
        return new String[]{
                c.getNomComplet(),
                Fmt.orDash(c.getTelephone()),
                c.getNumeroPermis(),
                Fmt.date(c.getValiditePermis()) + (expire ? " (expiré)" : ""),
                c.getStatut().getLibelle()
        };
    }

    private static String[] affectationToRow(Affectation a) {
        return new String[]{
                String.valueOf(a.getIdAffectation()),
                Fmt.date(a.getDateTrajet()),
                Fmt.time(a.getHeureDepart()),
                a.getTrajet().getNom(),
                a.getBus().getImmatriculation(),
                a.getChauffeur().getNomComplet(),
                a.getNbInscrits() + " / " + a.getBus().getCapacite(),
                a.getStatut().getLibelle()
        };
    }

    private static String[] incidentToRow(Incident i) {
        return new String[]{
                Fmt.dateTime(i.getDateHeure()),
                i.getType().getLibelle(),
                i.getGravite().getLibelle(),
                Fmt.orDash(i.getNomTrajet()),
                Fmt.orDash(i.getImmatriculationBus()),
                Fmt.orDash(i.getNomChauffeur()),
                i.getStatut().getLibelle(),
                Fmt.orDash(i.getDescription())
        };
    }

    private static String[] compteToRow(CompteUtilisateur c) {
        return new String[]{
                c.getLogin(),
                c.getNomEmploye() == null ? "—" : c.getNomAffiche(),
                c.getRole().getLibelle(),
                Fmt.dateTime(c.getDerniereConnexion()),
                c.isActif() ? "Actif" : "Inactif"
        };
    }

    private static HBox pagination(int shown, int total) {
        HBox pages = new HBox(4);

        for (String t : new String[]{"‹", "1", "2", "3", "›"}) {
            Button b = new Button(t);
            b.getStyleClass().add("page-btn");

            if (t.equals("1")) {
                b.getStyleClass().add("active");
            }

            pages.getChildren().add(b);
        }

        HBox h = new HBox(
                Ui.label("Affichage de 1 à " + shown + " sur " + total,
                        "muted"),
                Ui.spacer(),
                pages
        );

        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(14, 0, 0, 0));

        return h;
    }
}

