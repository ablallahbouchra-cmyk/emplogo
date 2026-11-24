package com.transportpro.view;

import com.transportpro.Ctx;
import com.transportpro.controller.MainController;
import com.transportpro.model.Affectation;
import com.transportpro.model.Inscription;
import com.transportpro.model.enums.StatutInscription;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

/** Inscriptions des employés à une affectation (données de la table `inscription`). */
public final class RegistrationsPage {

    private static Integer derniereAffectation;

    private final Ctx ctx;
    private final MainController controller;
    private final VBox root = Ui.page();
    private final List<Affectation> affectations;

    public static Node build(Ctx ctx, MainController controller) {
        return new RegistrationsPage(ctx, controller).root;
    }

    private RegistrationsPage(Ctx ctx, MainController controller) {
        this.ctx = ctx;
        this.controller = controller;
        this.affectations = Fmt.actives(controller.chargerAffectations());
        render(Fmt.parDefaut(affectations, derniereAffectation));
    }

    private void render(Affectation a) {
        if (a == null) {
            root.getChildren().setAll(Ui.card(Ui.label("Aucune affectation programmée.", "muted")));
            return;
        }
        derniereAffectation = a.getIdAffectation();
        List<Inscription> inscriptions = controller.chargerInscriptions(a.getIdAffectation());

        ComboBox<Affectation> choix = Forms.combo(affectations, Fmt::affectation);
        choix.setValue(a);
        choix.setOnAction(e -> render(choix.getValue()));
        choix.setMaxWidth(420);

        // --- résumé de l'affectation sélectionnée
        VBox info = new VBox(4,
                Ui.label("TRAJET SÉLECTIONNÉ", "eyebrow"),
                Ui.label(a.getTrajet().getNom(), "summary-h2"),
                Ui.label(Fmt.date(a.getDateTrajet()) + " à " + Fmt.time(a.getHeureDepart())
                        + " • Bus " + a.getBus().getImmatriculation()
                        + " • " + a.getChauffeur().getNomComplet(), "muted"),
                choix);

        int capacite = a.getBus().getCapacite();
        int inscrits = a.getNbInscrits();
        double largeur = capacite == 0 ? 0 : 230.0 * Math.min(1.0, inscrits / (double) capacite);

        StackPane track = new StackPane();
        track.getStyleClass().add("cap-track");
        Ui.fixHeight(track, 8);
        track.setMinWidth(230);
        track.setPrefWidth(230);
        track.setMaxWidth(230);
        Region fill = new Region();
        fill.getStyleClass().add("cap-fill");
        fill.setMinSize(largeur, 8);
        fill.setPrefSize(largeur, 8);
        fill.setMaxSize(largeur, 8);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);
        track.getChildren().add(fill);

        VBox capacity = new VBox(6, Ui.label(inscrits + " / " + capacite + " places", "cap-text"), track,
                Ui.label(Math.max(0, capacite - inscrits) + " places restantes", "muted"));

        HBox summary = new HBox(30, info, Ui.spacer(), capacity,
                Ui.button("Inscrire un employé", "primary", "plus",
                        () -> Forms.inscription(ctx, controller, a, inscriptions)));
        summary.setAlignment(Pos.CENTER_LEFT);
        VBox summaryCard = Ui.card(summary);
        summaryCard.getStyleClass().add("trip-summary");

        // --- liste des inscrits
        String[][] rows = inscriptions.stream().map(i -> new String[]{
                i.getEmploye().getNomComplet(),
                Fmt.dateTime(i.getDateInscription()),
                Fmt.orDash(i.getEmploye().getAdresse()),
                i.getStatut().getLibelle(),
                i.getStatut() == StatutInscription.ANNULEE ? "—" : i.getStatutPresence().getLibelle()
        }).toArray(String[][]::new);
        HBox head = new HBox(Ui.label("Employés inscrits", "card-h3"), Ui.spacer(), Ui.search("Rechercher un employé", 210));
        head.setAlignment(Pos.CENTER_LEFT);
        VBox listCard = Ui.card(head,
                Ui.table(new String[]{"Employé", "Date d’inscription", "Point d’embarquement", "Inscription", "Présence"},
                        rows, 4, () -> ctx.openModal("Détail — Inscriptions")));
        listCard.getStyleClass().add("main-card");
        head.setSpacing(10);
        VBox.setMargin(head, new javafx.geometry.Insets(0, 0, 14, 0));

        root.getChildren().setAll(summaryCard, listCard);
    }
}
