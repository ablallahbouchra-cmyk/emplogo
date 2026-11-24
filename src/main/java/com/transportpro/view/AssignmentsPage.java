package com.transportpro.view;

import com.transportpro.Ctx;
import com.transportpro.Page;
import com.transportpro.controller.MainController;
import com.transportpro.model.Affectation;
import com.transportpro.model.Bus;
import com.transportpro.model.Chauffeur;
import com.transportpro.model.Trajet;
import com.transportpro.model.enums.StatutAffectation;
import com.transportpro.model.enums.StatutBus;
import com.transportpro.model.enums.StatutChauffeur;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Affectation d'un bus et d'un chauffeur à un trajet ; les triggers MySQL font les contrôles finaux. */
public final class AssignmentsPage {

    private final VBox root = Ui.page();
    private final Ctx ctx;
    private final MainController controller;

    private final List<Bus> buses;
    private final List<Chauffeur> chauffeurs;
    private final ComboBox<Trajet> trajet;
    private final DatePicker date = new DatePicker(LocalDate.now().plusDays(1));
    private final TextField depart = new TextField("07:00");
    private final TextField arrivee = new TextField("08:00");
    private final FlowPane checks = new FlowPane(24, 8);
    private final HBox banner = new HBox(12);
    private final Button confirm;

    private Bus bus;
    private Chauffeur chauffeur;

    public static Node build(Ctx ctx, MainController controller) {
        return new AssignmentsPage(ctx, controller).root;
    }

    private AssignmentsPage(Ctx ctx, MainController controller) {
        this.ctx = ctx;
        this.controller = controller;
        this.buses = controller.chargerBus();
        this.chauffeurs = controller.chargerChauffeurs();
        this.trajet = Forms.combo(controller.chargerTrajets(), t -> t.getNom());
        this.confirm = Ui.button("Confirmer l’affectation", "primary", null, this::confirmer);
        bus = buses.stream().filter(b -> b.getStatut() == StatutBus.DISPONIBLE).findFirst().orElse(null);
        chauffeur = chauffeurs.stream().filter(this::chauffeurUtilisable).findFirst().orElse(null);
        date.setOnAction(e -> render());
        depart.textProperty().addListener((o, a, b) -> updateChecks());
        arrivee.textProperty().addListener((o, a, b) -> updateChecks());
        banner.setAlignment(Pos.CENTER_LEFT);
        render();
    }

    private static LocalTime heure(TextField f) {
        try {
            return LocalTime.parse(f.getText().trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private boolean chauffeurUtilisable(Chauffeur c) {
        return c.getStatut() == StatutChauffeur.ACTIF && c.permisValide(date.getValue());
    }

    private void render() {
        if (chauffeur != null && !chauffeurUtilisable(chauffeur)) chauffeur = null;

        // --- 1) trajet, date, horaires
        HBox heures = new HBox(10, Ui.field("Départ (HH:mm)", depart), Ui.field("Arrivée (HH:mm)", arrivee));
        VBox step1 = stepCard("1", "Programmer le trajet", "Itinéraire, date et horaires",
                Ui.field("Trajet", trajet), Ui.field("Date", date), heures);

        // --- 2) bus
        VBox step2 = stepCard("2", "Choisir un bus", "Capacité et disponibilité");
        for (Bus b : buses) {
            step2.getChildren().add(option(b.getImmatriculation(), b.getCapacite() + " places • " + b.getMarqueModele(),
                    b.getStatut().getLibelle(), b.equals(bus), b.getStatut() != StatutBus.DISPONIBLE, () -> {
                        bus = b;
                        render();
                    }));
        }

        // --- 3) chauffeur
        VBox step3 = stepCard("3", "Choisir un chauffeur", "Actifs avec un permis valide à la date");
        for (Chauffeur c : chauffeurs) {
            boolean utilisable = chauffeurUtilisable(c);
            String badge = c.getStatut() != StatutChauffeur.ACTIF ? c.getStatut().getLibelle()
                    : utilisable ? "Disponible" : "Permis expiré";
            step3.getChildren().add(option(c.getNomComplet(),
                    "Permis " + c.getNumeroPermis() + " • valide jusqu’au " + Fmt.date(c.getValiditePermis()),
                    badge, c.equals(chauffeur), !utilisable, () -> {
                        chauffeur = c;
                        render();
                    }));
        }

        GridPane steps = new GridPane();
        steps.setHgap(12);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(33.3);
            steps.getColumnConstraints().add(cc);
        }
        steps.add(step1, 0, 0);
        steps.add(step2, 1, 0);
        steps.add(step3, 2, 0);

        VBox checksCard = Ui.card(Ui.label("Vérifications automatiques", "card-h3"), checks,
                Ui.label("Les chevauchements d’horaires et les maintenances sont contrôlés par la base de données.", "muted"),
                confirm);
        checksCard.setSpacing(12);
        checksCard.setPadding(new Insets(16));

        updateChecks();
        root.getChildren().setAll(banner, steps, checksCard);
    }

    private void updateChecks() {
        LocalTime hd = heure(depart);
        LocalTime ha = heure(arrivee);
        Object[][] rules = {
            {"Trajet sélectionné", trajet.getValue() != null},
            {"Bus disponible", bus != null && bus.getStatut() == StatutBus.DISPONIBLE},
            {"Chauffeur actif", chauffeur != null && chauffeur.getStatut() == StatutChauffeur.ACTIF},
            {"Permis valide à la date du trajet", chauffeur != null && chauffeur.permisValide(date.getValue())},
            {"Heure d’arrivée après l’heure de départ", hd != null && ha != null && ha.isAfter(hd)},
        };
        boolean ok = true;
        checks.getChildren().clear();
        for (Object[] r : rules) {
            boolean pass = (Boolean) r[1];
            ok &= pass;
            HBox line = new HBox(6, Ui.label(pass ? "✓" : "×", pass ? "check-pass" : "check-fail"), Ui.label((String) r[0], "check-text"));
            line.setAlignment(Pos.CENTER_LEFT);
            checks.getChildren().add(line);
        }
        confirm.setDisable(!ok);

        banner.getChildren().setAll(
                Ui.label(ok ? "Prêt à affecter" : "Affectation incomplète", "banner-title"),
                Ui.label(ok ? "Les vérifications locales sont validées. Vous pouvez confirmer."
                            : "Complétez les éléments signalés ci-dessous.", "banner-text"));
        banner.getStyleClass().setAll("assign-banner", ok ? "ok" : "error");
    }

    private void confirmer() {
        Affectation a = new Affectation(0, trajet.getValue(), bus, chauffeur, date.getValue(),
                heure(depart), heure(arrivee), StatutAffectation.PLANIFIEE);
        if (controller.creerAffectation(a)) {
            ctx.navigate(Page.TRIPS);
        }
    }

    private VBox stepCard(String number, String title, String subtitle, Node... extra) {
        HBox head = new HBox(10, Ui.label(number, "step"), Ui.cardTitle(title, subtitle, null, null));
        head.setAlignment(Pos.TOP_LEFT);
        VBox card = Ui.card(head);
        card.setSpacing(8);
        card.setPadding(new Insets(16));
        card.getChildren().addAll(extra);
        return card;
    }

    private Button option(String title, String sub, String badgeText, boolean selected, boolean disabled, Runnable onClick) {
        Circle radio = new Circle(7);
        radio.getStyleClass().add(selected ? "radio-on" : "radio-off");
        HBox row = new HBox(10, radio, new VBox(2, Ui.label(title, "opt-title"), Ui.label(sub, "opt-sub")),
                Ui.spacer(), Ui.badge(badgeText));
        row.setAlignment(Pos.CENTER_LEFT);

        Button b = new Button();
        b.setGraphic(row);
        b.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        b.setMaxWidth(Double.MAX_VALUE);
        b.getStyleClass().add("option-row");
        if (selected) b.getStyleClass().add("selected");
        row.prefWidthProperty().bind(b.widthProperty().subtract(30));
        b.setDisable(disabled);
        if (onClick != null) b.setOnAction(e -> onClick.run());
        return b;
    }
}
