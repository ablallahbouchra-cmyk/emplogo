package com.transportpro.dao;

import com.transportpro.model.Affectation;
import com.transportpro.model.Bus;
import com.transportpro.model.Chauffeur;
import com.transportpro.model.Trajet;
import com.transportpro.model.enums.StatutAffectation;
import com.transportpro.model.enums.StatutBus;
import com.transportpro.model.enums.StatutChauffeur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Table `affectation` ; les triggers MySQL contrôlent bus, chauffeur et chevauchements. */
public class AffectationDAO {

    private static final String SELECT = """
        SELECT a.id_affectation, a.date_trajet, a.heure_depart, a.heure_arrivee, a.statut,
               t.id_trajet, t.nom AS trajet_nom, t.lieu_depart, t.lieu_arrivee,
               b.id_bus, b.immatriculation, b.marque, b.modele, b.capacite, b.statut AS bus_statut,
               c.id_chauffeur, c.id_employe, c.numero_permis, c.validite_permis, c.statut AS chauffeur_statut,
               e.nom, e.prenom, e.telephone,
               (SELECT COUNT(*) FROM inscription i
                 WHERE i.id_affectation = a.id_affectation AND i.statut = 'CONFIRMEE') AS nb_inscrits
        FROM affectation a
        JOIN trajet t ON t.id_trajet = a.id_trajet
        JOIN bus b ON b.id_bus = a.id_bus
        JOIN chauffeur c ON c.id_chauffeur = a.id_chauffeur
        JOIN employe e ON e.id_employe = c.id_employe
        """;

    private static final String ORDER = " ORDER BY a.date_trajet, a.heure_depart, a.id_affectation";

    public List<Affectation> findAll() throws SQLException {
        return query(SELECT + ORDER, null);
    }

    public List<Affectation> findByChauffeur(int idChauffeur) throws SQLException {
        return query(SELECT + " WHERE a.id_chauffeur = ?" + ORDER, idChauffeur);
    }

    public void insert(Affectation a) throws SQLException {
        String sql = """
            INSERT INTO affectation (id_trajet, id_bus, id_chauffeur, date_trajet, heure_depart, heure_arrivee, statut)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getTrajet().getIdTrajet());
            ps.setInt(2, a.getBus().getIdBus());
            ps.setInt(3, a.getChauffeur().getIdChauffeur());
            JdbcUtil.setDate(ps, 4, a.getDateTrajet());
            JdbcUtil.setTime(ps, 5, a.getHeureDepart());
            JdbcUtil.setTime(ps, 6, a.getHeureArrivee());
            ps.setString(7, (a.getStatut() == null ? StatutAffectation.PLANIFIEE : a.getStatut()).name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) a.setIdAffectation(keys.getInt(1));
            }
        }
    }

    public void updateStatut(int idAffectation, StatutAffectation statut) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE affectation SET statut=? WHERE id_affectation=?")) {
            ps.setString(1, statut.name());
            ps.setInt(2, idAffectation);
            ps.executeUpdate();
        }
    }

    private List<Affectation> query(String sql, Integer param) throws SQLException {
        List<Affectation> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (param != null) ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    private Affectation map(ResultSet rs) throws SQLException {
        Trajet t = new Trajet(rs.getInt("id_trajet"), rs.getString("trajet_nom"),
                rs.getString("lieu_depart"), rs.getString("lieu_arrivee"));
        Bus b = new Bus(rs.getInt("id_bus"), rs.getString("immatriculation"), rs.getString("marque"),
                rs.getString("modele"), rs.getInt("capacite"),
                JdbcUtil.enumOf(StatutBus.class, rs.getString("bus_statut"), StatutBus.DISPONIBLE));
        Chauffeur ch = new Chauffeur(rs.getInt("id_chauffeur"), rs.getInt("id_employe"),
                rs.getString("nom"), rs.getString("prenom"), rs.getString("telephone"),
                rs.getString("numero_permis"), JdbcUtil.date(rs, "validite_permis"),
                JdbcUtil.enumOf(StatutChauffeur.class, rs.getString("chauffeur_statut"), StatutChauffeur.ACTIF));
        Affectation a = new Affectation(rs.getInt("id_affectation"), t, b, ch,
                JdbcUtil.date(rs, "date_trajet"), JdbcUtil.time(rs, "heure_depart"), JdbcUtil.time(rs, "heure_arrivee"),
                JdbcUtil.enumOf(StatutAffectation.class, rs.getString("statut"), StatutAffectation.PLANIFIEE));
        a.setNbInscrits(rs.getInt("nb_inscrits"));
        return a;
    }
}
