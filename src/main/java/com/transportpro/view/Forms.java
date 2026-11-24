package com.transportpro.view;

import com.transportpro.Ctx;
import com.transportpro.controller.MainController;
import com.transportpro.model.Affectation;
import com.transportpro.model.Bus;
import com.transportpro.model.CompteUtilisateur;
import com.transportpro.model.Employe;
import com.transportpro.model.Incident;
import com.transportpro.model.Inscription;
import com.transportpro.model.enums.GraviteIncident;
import com.transportpro.model.enums.ModeDeclaration;
import com.transportpro.model.enums.Role;
import com.transportpro.model.enums.StatutBus;
import com.transportpro.model.enums.TypeIncident;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Formulaires des modales reliés au controller. */
final class Forms {
    private Forms() {}

    static <T> ComboBox<T> combo(List<T> items, Function<T, String> label) {
        ComboBox<T> c = new ComboBox<>();
        c.getItems().setAll(items);
        c.setConverter(new StringConverter<>() {
            @Override public String toString(T t) { return t == null ? "" : label.apply(t); }
            @Override public T fromString(String s) { return null; }
        });
        c.getSelectionModel().selectFirst();
        c.setMaxWidth(Double.MAX_VALUE);
        return c;
    }

    private static GridPane grid() {
        GridPane g = new GridPane();
        g.setHgap(14);
        g.setVgap(14);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(50);
            g.getColumnConstraints().add(c);
        }
        return g;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    static void bus(Ctx ctx, MainController controller) {
        TextField immat = new TextField();
        immat.setPromptText("AB-123-CD");
        TextField marque = new TextField();
        TextField modele = new TextField();
        TextField capacite = new TextField();
        capacite.setPromptText("50");
        ComboBox<StatutBus> statut = combo(Arrays.asList(StatutBus.values()), StatutBus::getLibelle);

        GridPane g = grid();
        g.add(Ui.field("Immatriculation", immat), 0, 0);
        g.add(Ui.field("Capacité (places)", capacite), 1, 0);
        g.add(Ui.field("Marque", marque), 0, 1);
        g.add(Ui.field("Modèle", modele), 1, 1);
        g.add(Ui.field("Statut", statut), 0, 2);

        ctx.openModal("Ajouter un bus", g, () -> {
            int cap;
            try {
                cap = Integer.parseInt(capacite.getText().trim());
            } catch (NumberFormatException ex) {
                cap = 0;
            }
            if (immat.getText().isBlank() || cap <= 0) {
                ctx.toastErreur("Immatriculation obligatoire et capacité > 0.");
                return false;
            }
            return controller.ajouterBus(new Bus(0, immat.getText().trim(), blankToNull(marque.getText()),
                    blankToNull(modele.getText()), cap, statut.getValue()));
        });
    }

    static void incident(Ctx ctx, MainController controller, String title) {
        List<Affectation> affectations = Fmt.actives(controller.chargerAffectations()).stream()
                .sorted(Comparator.comparing(Affectation::getDateTrajet).thenComparing(Affectation::getHeureDepart).reversed())
                .toList();
        ComboBox<Affectation> aff = combo(affectations, a -> Fmt.affectation(a) + " • " + a.getBus().getImmatriculation());
        ComboBox<TypeIncident> type = combo(Arrays.asList(TypeIncident.values()), TypeIncident::getLibelle);
        ComboBox<GraviteIncident> gravite = combo(Arrays.asList(GraviteIncident.values()), GraviteIncident::getLibelle);
        ComboBox<ModeDeclaration> mode = combo(Arrays.asList(ModeDeclaration.values()), ModeDeclaration::getLibelle);
        CompteUtilisateur compte = controller.getCompteConnecte();
        if (compte != null && compte.getRole() == Role.CHAUFFEUR) mode.setValue(ModeDeclaration.SIGNALEMENT_CHAUFFEUR);
        TextArea d = new TextArea();
        d.setPromptText("Décrivez précisément l’incident...");
        d.setPrefRowCount(4);
        d.setWrapText(true);

        GridPane g = grid();
        g.add(Ui.field("Trajet (affectation)", aff), 0, 0, 2, 1);
        g.add(Ui.field("Type", type), 0, 1);
        g.add(Ui.field("Gravité", gravite), 1, 1);
        g.add(Ui.field("Mode de déclaration", mode), 0, 2);
        g.add(Ui.field("Description", d), 0, 3, 2, 1);

        ctx.openModal(title, g, () -> {
            if (aff.getValue() == null) {
                ctx.toastErreur("Sélectionnez une affectation.");
                return false;
            }
            Incident i = new Incident();
            i.setIdAffectation(aff.getValue().getIdAffectation());
            i.setType(type.getValue());
            i.setGravite(gravite.getValue());
            i.setModeDeclaration(mode.getValue());
            i.setDescription(blankToNull(d.getText()));
            return controller.declarerIncident(i);
        });
    }

    static void inscription(Ctx ctx, MainController controller, Affectation a, List<Inscription> deja) {
        Set<Integer> inscrits = deja.stream()
                .filter(i -> i.getStatut() == com.transportpro.model.enums.StatutInscription.CONFIRMEE)
                .map(Inscription::getIdEmploye).collect(Collectors.toSet());
        List<Employe> candidats = controller.chargerEmployes().stream()
                .filter(e -> !inscrits.contains(e.getIdEmploye())).toList();
        ComboBox<Employe> emp = combo(candidats, e -> e.getNomComplet() + " (" + e.getMatricule() + ")"
                + (e.isActif() ? "" : " — inactif"));

        GridPane g = grid();
        g.add(Ui.field("Trajet", Ui.label(Fmt.affectation(a), "cell-text")), 0, 0, 2, 1);
        g.add(Ui.field("Employé", emp), 0, 1, 2, 1);

        ctx.openModal("Inscrire un employé", g, () -> emp.getValue() != null
                && controller.inscrireEmploye(emp.getValue().getIdEmploye(), a.getIdAffectation()));
    }
}
