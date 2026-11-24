package com.transportpro.view;

import com.transportpro.model.Affectation;
import com.transportpro.model.enums.StatutAffectation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Formatage d'affichage et choix des affectations « courantes ». */
final class Fmt {
    private Fmt() {}

    static final Locale FR = Locale.FRANCE;
    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter T = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter LONG = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", FR);

    static String date(LocalDate d) { return d == null ? "—" : D.format(d); }
    static String time(LocalTime t) { return t == null ? "—" : T.format(t); }
    static String dateTime(LocalDateTime dt) { return dt == null ? "—" : D.format(dt) + " • " + T.format(dt); }
    static String longDate(LocalDate d) { return d == null ? "—" : LONG.format(d); }
    static String orDash(String s) { return s == null || s.isBlank() ? "—" : s; }

    static String affectation(Affectation a) {
        return date(a.getDateTrajet()) + " • " + time(a.getHeureDepart()) + " • " + a.getTrajet().getNom();
    }

    static String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] p = name.trim().split("\\s+");
        return (p[0].substring(0, 1) + (p.length > 1 ? p[p.length - 1].substring(0, 1) : "")).toUpperCase();
    }

    static List<Affectation> actives(List<Affectation> all) {
        return all.stream().filter(a -> a.getStatut() != StatutAffectation.ANNULEE).toList();
    }

    /** Aujourd'hui s'il y a des trajets, sinon le prochain jour planifié, sinon le dernier jour connu. */
    static LocalDate jourDeReference(List<Affectation> all) {
        LocalDate today = LocalDate.now();
        List<LocalDate> dates = actives(all).stream().map(Affectation::getDateTrajet)
                .filter(Objects::nonNull).distinct().sorted().toList();
        if (dates.isEmpty()) return today;
        return dates.stream().filter(d -> !d.isBefore(today)).findFirst().orElse(dates.get(dates.size() - 1));
    }

    static Affectation parDefaut(List<Affectation> all, Integer preferee) {
        List<Affectation> act = actives(all);
        if (preferee != null) {
            for (Affectation a : act) if (a.getIdAffectation() == preferee) return a;
        }
        LocalDate ref = jourDeReference(all);
        return act.stream().filter(a -> ref.equals(a.getDateTrajet()))
                .min(Comparator.comparing(Affectation::getHeureDepart))
                .orElse(act.isEmpty() ? null : act.get(0));
    }
}
