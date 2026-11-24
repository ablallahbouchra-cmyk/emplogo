package com.transportpro.view;

import com.transportpro.Ctx;
import com.transportpro.Page;
import com.transportpro.controller.MainController;
import com.transportpro.model.Affectation;
import com.transportpro.model.Inscription;
import com.transportpro.model.Presence;
import com.transportpro.model.enums.StatutInscription;
import com.transportpro.model.enums.StatutPresence;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Saisie des présences d'une affectation (table `presence`, une ligne par inscription). */
public final class AttendancePage {

    private static final StatutPresence[] CHOIX = {StatutPresence.PRESENT, StatutPresence.RETARD, StatutPresence.ABSENT};
    private static Integer derniereAffectation;

    private final Ctx ctx;
    private final MainController controller;
    private final VBox root = Ui.page();
    private final List<Affectation> affectations;

    public static Node build(Ctx ctx, MainController controller) {
        return new AttendancePage(ctx, controller).root;
    }

    private AttendancePage(Ctx ctx, MainController controller) {
        this.ctx = ctx;
        this.controller = controller;
        this.affectations = Fmt.actives(controller.chargerAffectations());
        Affectation a = Fmt.parDefaut(affectations, derniereAffectation);
        render(a == null ? LocalDate.now() : a.getDateTrajet(), a);
    }

    private List<Affectation> duJour(LocalDate date) {
        return affectations.stream().filter(a -> Objects.equals(a.getDateTrajet(), date)).toList();
    }

    private void render(LocalDate date, Affectation a) {
        List<Affectation> jour = duJour(date);
        if (a == null || !jour.contains(a)) a = jour.isEmpty() ? null : jour.get(0);
        final Affectation affectation = a;
        if (a != null) derniereAffectation = a.getIdAffectation();

        DatePicker datePicker = new DatePicker(date);
        datePicker.setOnAction(e -> render(datePicker.getValue(), null));
        ComboBox<Affectation> choix = Forms.combo(jour,
                x -> Fmt.time(x.getHeureDepart()) + " • " + x.getTrajet().getNom() + " • " + x.getBus().getImmatriculation());
        choix.setValue(a);
        choix.setOnAction(e -> render(date, choix.getValue()));
        choix.setPrefWidth(380);

        List<Inscription> inscriptions = a == null ? List.of()
                : controller.chargerInscriptions(a.getIdAffectation()).stream()
                        .filter(i -> i.getStatut() == StatutInscription.CONFIRMEE).toList();

        List<ToggleGroup> groups = new ArrayList<>();
        List<ToggleButton> presentButtons = new ArrayList<>();
        Label counter = Ui.label("", "muted");
        int total = inscriptions.size();
        Runnable update = () -> {
            long n = groups.stream().filter(g -> g.getSelectedToggle() != null).count();
            counter.setText(n + " sur " + total + " présences renseignées");
        };

        // --- barre du haut
        HBox filters = new HBox(10, Ui.field("Date", datePicker), Ui.field("Trajet", choix));
        filters.setAlignment(Pos.BOTTOM_LEFT);
        HBox top = new HBox(filters, Ui.spacer(),
                Ui.button("Tout marquer présent", "secondary", null, () -> presentButtons.forEach(b -> b.setSelected(true))));
        top.setAlignment(Pos.BOTTOM_CENTER);
        top.setPadding(new Insets(0, 0, 15, 0));

        // --- liste
        VBox list = new VBox();
        list.getChildren().add(row(Ui.label("EMPLOYÉ", "th"), Ui.label("POINT D’EMBARQUEMENT", "th"), Ui.label("STATUT DE PRÉSENCE", "th"), "presence-head"));
        if (a == null) {
            list.getChildren().add(Ui.label("Aucun trajet programmé à cette date.", "muted"));
        } else if (inscriptions.isEmpty()) {
            list.getChildren().add(Ui.label("Aucun employé inscrit à ce trajet.", "muted"));
        }
        for (Inscription i : inscriptions) {
            String nom = i.getEmploye().getNomComplet();
            HBox person = new HBox(10, Ui.avatar(Fmt.initials(nom)), Ui.label(nom, "person-name"));
            person.setAlignment(Pos.CENTER_LEFT);

            ToggleGroup g = new ToggleGroup();
            g.setUserData(i);
            groups.add(g);
            HBox seg = new HBox(-1);
            for (StatutPresence s : CHOIX) {
                ToggleButton tb = new ToggleButton(s.getLibelle());
                tb.setUserData(s);
                tb.setToggleGroup(g);
                tb.getStyleClass().add("seg");
                if (s == StatutPresence.PRESENT) presentButtons.add(tb);
                if (s == i.getStatutPresence()) tb.setSelected(true);
                seg.getChildren().add(tb);
            }
            g.selectedToggleProperty().addListener((obs, o, n) -> update.run());
            list.getChildren().add(row(person, Ui.label(Fmt.orDash(i.getEmploye().getAdresse()), "cell-text"), seg, "presence-row"));
        }
        update.run();

        // --- barre d'enregistrement
        HBox save = new HBox(counter, Ui.spacer(),
                Ui.button("Enregistrer les présences", "primary", null, () -> {
                    if (affectation == null) return;
                    if (controller.enregistrerPresences(presences(groups, affectation))) {
                        ctx.navigate(Page.ATTENDANCE);
                    }
                }));
        save.setAlignment(Pos.CENTER_LEFT);
        save.getStyleClass().add("save-bar");

        VBox card = Ui.card(top, list, save);
        card.getStyleClass().add("main-card");
        root.getChildren().setAll(card);
    }

    private static List<Presence> presences(List<ToggleGroup> groups, Affectation a) {
        List<Presence> result = new ArrayList<>();
        for (ToggleGroup g : groups) {
            if (g.getSelectedToggle() == null) continue;
            Inscription i = (Inscription) g.getUserData();
            StatutPresence statut = (StatutPresence) g.getSelectedToggle().getUserData();
            Presence ancienne = i.getPresence();
            LocalTime heure = switch (statut) {
                case ABSENT -> null;
                case PRESENT -> ancienne != null && ancienne.getHeureMontee() != null ? ancienne.getHeureMontee() : a.getHeureDepart();
                default -> ancienne == null ? null : ancienne.getHeureMontee();
            };
            result.add(new Presence(0, i.getIdInscription(), statut, heure, ancienne == null ? null : ancienne.getRemarque()));
        }
        return result;
    }

    private static GridPane row(Node a, Node b, Node c, String styleClass) {
        GridPane g = new GridPane();
        g.getStyleClass().add(styleClass);
        double[] w = {40, 28, 32};
        for (double p : w) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(p);
            g.getColumnConstraints().add(cc);
        }
        g.add(a, 0, 0);
        g.add(b, 1, 0);
        g.add(c, 2, 0);
        GridPane.setValignment(a, javafx.geometry.VPos.CENTER);
        return g;
    }
}
