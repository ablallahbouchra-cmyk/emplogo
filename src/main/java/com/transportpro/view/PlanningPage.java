package com.transportpro.view;

import com.transportpro.Ctx;
import com.transportpro.controller.MainController;
import com.transportpro.model.Affectation;
import com.transportpro.model.Chauffeur;
import com.transportpro.model.Inscription;
import com.transportpro.model.enums.StatutInscription;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.time.LocalDate;
import java.util.List;

/** « Mon planning » : affectations du chauffeur connecté. */
public final class PlanningPage {
    private PlanningPage() {}

    public static Node build(Ctx ctx, MainController controller) {
        Chauffeur chauffeur = controller.chauffeurConnecte();
        boolean demo = chauffeur == null;
        if (demo) {
            List<Chauffeur> tous = controller.chargerChauffeurs();
            chauffeur = tous.isEmpty() ? null : tous.get(0);
        }
        if (chauffeur == null) {
            return Ui.page(Ui.card(Ui.label("Aucun chauffeur trouvé dans la base de données.", "muted")));
        }

        List<Affectation> affectations = Fmt.actives(controller.chargerAffectationsChauffeur(chauffeur.getIdChauffeur()));
        LocalDate jour = Fmt.jourDeReference(affectations);
        List<Affectation> duJour = affectations.stream().filter(a -> jour.equals(a.getDateTrajet())).toList();
        String bus = duJour.stream().map(a -> a.getBus().getImmatriculation()).distinct()
                .reduce((x, y) -> x + ", " + y).orElse("—");

        HBox hero = new HBox(
                new VBox(4,
                        Ui.label("Bonjour " + chauffeur.getPrenom() + (demo ? " (démo)" : "") + ",", "hero-small"),
                        Ui.label("Votre planning du " + Fmt.longDate(jour), "hero-h2"),
                        Ui.label(duJour.size() + " trajet(s) prévu(s) • Bus " + bus, "hero-small")),
                Ui.spacer(),
                Ui.button("Signaler un incident", "soft", null, () -> ctx.openModal("Signaler un incident")));
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.getStyleClass().add("driver-hero");

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(50);
            grid.getColumnConstraints().add(cc);
        }
        for (int i = 0; i < duJour.size(); i++) {
            grid.add(routeCard(ctx, controller, duJour.get(i)), i % 2, i / 2);
        }

        String[][] rows = affectations.stream().map(a -> new String[]{
                Fmt.date(a.getDateTrajet()),
                Fmt.time(a.getHeureDepart()) + " → " + Fmt.time(a.getHeureArrivee()),
                a.getTrajet().getNom(),
                a.getBus().getImmatriculation(),
                a.getNbInscrits() + " / " + a.getBus().getCapacite(),
                a.getStatut().getLibelle()
        }).toArray(String[][]::new);
        VBox all = Ui.card(Ui.cardTitle("Tous mes trajets", affectations.size() + " affectation(s)", null, null),
                Ui.table(new String[]{"Date", "Horaire", "Trajet", "Bus", "Inscrits", "Statut"}, rows, 5, null));
        all.getStyleClass().add("main-card");

        return Ui.page(hero, grid, all);
    }

    private static VBox routeCard(Ctx ctx, MainController controller, Affectation a) {
        HBox top = new HBox(
                new VBox(3, Ui.label(Fmt.date(a.getDateTrajet()), "eyebrow"), Ui.label(a.getTrajet().getNom(), "card-h3")),
                Ui.spacer(), Ui.badge(a.getStatut().getLibelle()));
        top.setAlignment(Pos.TOP_LEFT);

        VBox meta = new VBox(3,
                Ui.label(Fmt.time(a.getHeureDepart()) + " → " + Fmt.time(a.getHeureArrivee()), "route-time"),
                Ui.label("Bus " + a.getBus().getImmatriculation() + " • " + a.getBus().getMarqueModele(), "muted"),
                Ui.label(a.getNbInscrits() + " employés inscrits", "muted"));

        VBox stops = new VBox(10);
        String[][] etapes = {
            {Fmt.time(a.getHeureDepart()), a.getTrajet().getLieuDepart()},
            {Fmt.time(a.getHeureArrivee()), a.getTrajet().getLieuArrivee()},
        };
        for (String[] e : etapes) {
            Circle dot = new Circle(4.5);
            dot.getStyleClass().add("stop-dot");
            HBox s = new HBox(10, dot, Ui.label(e[0], "stop-time"), Ui.label(e[1], "cell-text"));
            s.setAlignment(Pos.CENTER_LEFT);
            stops.getChildren().add(s);
        }

        VBox card = Ui.card(top, meta, stops, Ui.button("Voir la liste des passagers", "secondary", null,
                () -> passagers(ctx, controller, a)));
        card.setSpacing(14);
        card.getStyleClass().add("route-card");
        return card;
    }

    private static void passagers(Ctx ctx, MainController controller, Affectation a) {
        List<Inscription> inscrits = controller.chargerInscriptions(a.getIdAffectation()).stream()
                .filter(i -> i.getStatut() == StatutInscription.CONFIRMEE).toList();
        String[][] rows = inscrits.stream().map(i -> new String[]{
                i.getEmploye().getNomComplet(),
                Fmt.orDash(i.getEmploye().getAdresse()),
                i.getStatutPresence().getLibelle()
        }).toArray(String[][]::new);
        ctx.openModal("Passagers — " + a.getTrajet().getNom(),
                Ui.table(new String[]{"Employé", "Point d’embarquement", "Présence"}, rows, 2, null),
                () -> true);
    }
}
