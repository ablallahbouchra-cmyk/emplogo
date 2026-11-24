package com.transportpro.view;

import com.transportpro.*;
import com.transportpro.controller.MainController;
import com.transportpro.model.Affectation;
import com.transportpro.model.Bus;
import com.transportpro.model.Incident;
import com.transportpro.model.enums.StatutAffectation;
import com.transportpro.model.enums.StatutBus;
import com.transportpro.model.enums.StatutIncident;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class DashboardPage {
    private DashboardPage() {}

    public static Node build(Ctx ctx, MainController controller) {
        List<Bus> buses = controller.chargerBus();
        List<Affectation> affectations = controller.chargerAffectations();
        List<Affectation> actives = Fmt.actives(affectations);
        List<Incident> incidents = controller.chargerIncidents();

        LocalDate jour = Fmt.jourDeReference(affectations);
        boolean aujourdhui = jour.equals(LocalDate.now());
        List<Affectation> duJour = actives.stream()
                .filter(a -> jour.equals(a.getDateTrajet()))
                .sorted(Comparator.comparing(Affectation::getHeureDepart))
                .toList();
        long enMaintenance = buses.stream().filter(b -> b.getStatut() == StatutBus.EN_MAINTENANCE).count();
        long disponibles = buses.stream().filter(b -> b.getStatut() == StatutBus.DISPONIBLE).count();
        long incidentsOuverts = incidents.stream()
                .filter(i -> i.getStatut() == StatutIncident.OUVERT || i.getStatut() == StatutIncident.EN_COURS).count();
        LocalDateTime now = LocalDateTime.now();
        long presqueComplets = actives.stream()
                .filter(a -> a.getStatut() == StatutAffectation.PLANIFIEE)
                .filter(a -> !LocalDateTime.of(a.getDateTrajet(), a.getHeureDepart()).isBefore(now))
                .filter(a -> a.getNbInscrits() >= 0.9 * a.getBus().getCapacite())
                .count();
        YearMonth mois = YearMonth.now();
        long incidentsMois = incidents.stream()
                .filter(i -> i.getDateHeure() != null && YearMonth.from(i.getDateHeure()).equals(mois)).count();
        int inscritsJour = duJour.stream().mapToInt(Affectation::getNbInscrits).sum();
        String libelleJour = aujourdhui ? "Aujourd’hui" : "Le " + Fmt.date(jour);

        // --- actions rapides
        HBox quick = new HBox(8, Ui.spacer(),
                Ui.button("Nouveau trajet", "primary", "plus", () -> ctx.openModal("Créer un trajet")),
                Ui.button("Nouvel employé", "secondary", "plus", () -> ctx.openModal("Ajouter un employé")),
                Ui.button("Enregistrer un incident", "soft", null, () -> ctx.openModal("Enregistrer un incident")));

        // --- bandeau « points d'attention »
        HBox alert = new HBox(22,
                Ui.label("Points d’attention", "alert-title"),
                chip(String.valueOf(enMaintenance), " bus en maintenance"),
                chip(String.valueOf(incidentsOuverts), " incidents ouverts"),
                chip(String.valueOf(presqueComplets), " trajets presque complets"));
        alert.getStyleClass().add("alert-strip");
        alert.setAlignment(Pos.CENTER_LEFT);

        // --- cartes statistiques
        String[][] stats = {
            {"Trajets du jour", String.valueOf(duJour.size()), libelleJour, "trips", "TRIPS"},
            {"Employés inscrits", String.valueOf(inscritsJour), libelleJour, "employees", "EMPLOYEES"},
            {"Bus disponibles", disponibles + " / " + buses.size(), (buses.size() - disponibles) + " indisponibles", "buses", "BUSES"},
            {"Incidents du mois", String.valueOf(incidentsMois), incidentsOuverts + " ouverts au total", "incidents", "INCIDENTS"},
        };
        GridPane statGrid = grid(25, 25, 25, 25);
        for (int i = 0; i < stats.length; i++) {
            final Page target = Page.valueOf(stats[i][4]);
            statGrid.add(Ui.statCard(stats[i][0], stats[i][1], stats[i][2], stats[i][3], () -> ctx.navigate(target)), i, 0);
        }

        // --- graphiques
        VBox lineCard = Ui.card(
                Ui.cardTitle("Nombre de trajets par jour", "6 derniers jours programmés", null, null),
                trajetsParJour(actives));
        VBox donutCard = Ui.card(
                Ui.cardTitle("Taux de présence global", "Ce mois", null, null),
                donut(),
                legend());
        VBox barCard = Ui.card(
                Ui.cardTitle("Coût du transport", "En milliers MAD", null, null),
                Ui.barChart(new String[]{"Jan", "Fév", "Mar", "Avr", "Mai", "Juin"}, new double[]{42, 58, 46, 72, 64, 84}));
        for (VBox c : new VBox[]{lineCard, donutCard, barCard}) {
            c.getStyleClass().add("chart-card");
            Ui.fixHeight(c, 250);
        }
        GridPane charts = grid(41, 24, 35);
        charts.add(lineCard, 0, 0);
        charts.add(donutCard, 1, 0);
        charts.add(barCard, 2, 0);

        // --- trajets du jour + derniers incidents
        String[][] rows = duJour.stream().map(a -> new String[]{
                Fmt.time(a.getHeureDepart()),
                a.getTrajet().getLieuArrivee(),
                a.getBus().getImmatriculation(),
                a.getChauffeur().getNomComplet(),
                a.getNbInscrits() + " / " + a.getBus().getCapacite(),
                a.getStatut().getLibelle()
        }).toArray(String[][]::new);
        VBox tripsCard = Ui.card(
                Ui.cardTitle("Trajets du jour", Fmt.longDate(jour), "Voir tous", () -> ctx.navigate(Page.TRIPS)),
                Ui.table(new String[]{"Heure", "Destination", "Bus", "Chauffeur", "Occupation", "Statut"}, rows, 5,
                        () -> ctx.openModal("Détail — Trajets")));
        VBox incidentsCard = Ui.card(
                Ui.cardTitle("Derniers incidents", "Mise à jour en direct", null, null),
                incidentList(ctx, incidents));
        GridPane bottom = grid(66.6, 33.4);
        bottom.add(tripsCard, 0, 0);
        bottom.add(incidentsCard, 1, 0);
        for (VBox c : new VBox[]{tripsCard, incidentsCard}) {
            c.setPadding(new Insets(15));
        }

        return Ui.page(quick, alert, statGrid, charts, bottom);
    }

    private static GridPane grid(double... percents) {
        GridPane g = new GridPane();
        g.setHgap(12);
        for (double p : percents) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(p);
            g.getColumnConstraints().add(c);
        }
        return g;
    }

    private static HBox chip(String number, String text) {
        HBox h = new HBox(Ui.label(number, "alert-num"), Ui.label(text, "alert-text"));
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private static Node donut() {
        Circle base = new Circle(52, Color.web("#DFB6B2"));
        Arc arc = new Arc(0, 0, 52, 52, 90, -327.6); // 91 % de 360°
        arc.setType(ArcType.ROUND);
        arc.setFill(Color.web("#16A34A"));
        Circle hole = new Circle(36, Color.WHITE);
        VBox text = new VBox(Ui.label("91%", "donut-value"), Ui.label("Présence", "donut-sub"));
        text.setAlignment(Pos.CENTER);
        text.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane s = new StackPane(new Group(base, arc, hole), text);
        s.setPadding(new Insets(4, 0, 8, 0));
        return s;
    }

    private static Node legend() {
        HBox h = new HBox(16, legendItem("#16A34A", "Présents 91%"), legendItem("#DFB6B2", "Absents 9%"));
        h.setAlignment(Pos.CENTER);
        return h;
    }

    private static Node legendItem(String color, String text) {
        HBox h = new HBox(6, new Circle(4, Color.web(color)), Ui.label(text, "muted"));
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private static Node trajetsParJour(List<Affectation> actives) {
        Map<LocalDate, Long> parJour = new TreeMap<>();
        for (Affectation a : actives) parJour.merge(a.getDateTrajet(), 1L, Long::sum);
        List<Map.Entry<LocalDate, Long>> derniers = parJour.entrySet().stream()
                .skip(Math.max(0, parJour.size() - 6)).toList();
        String[] cats = derniers.stream().map(e -> Fmt.date(e.getKey()).substring(0, 5)).toArray(String[]::new);
        double[] values = derniers.stream().mapToDouble(Map.Entry::getValue).toArray();
        return Ui.areaChart(cats, values);
    }

    private static Node incidentList(Ctx ctx, List<Incident> incidents) {
        String[][] items = incidents.stream().limit(3).map(i -> new String[]{
                i.getType().getLibelle(),
                Fmt.orDash(i.getNomTrajet()) + " • " + Fmt.date(i.getDateHeure() == null ? null : i.getDateHeure().toLocalDate()),
                Fmt.orDash(i.getDescription())
        }).toArray(String[][]::new);
        if (items.length == 0) return Ui.label("Aucun incident enregistré.", "muted");
        VBox list = new VBox(8);
        for (String[] it : items) {
            HBox row = new HBox(10, Ui.badge(it[0]),
                    new VBox(2, Ui.label(it[2], "incident-title"), Ui.label(it[1], "muted")),
                    Ui.spacer(), Ui.label("›", "chevron"));
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("incident-row");
            row.setOnMouseClicked(e -> ctx.navigate(Page.INCIDENTS));
            list.getChildren().add(row);
        }
        return list;
    }
}
