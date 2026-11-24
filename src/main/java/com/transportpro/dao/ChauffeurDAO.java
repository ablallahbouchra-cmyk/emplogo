package com.transportpro.dao;

import com.transportpro.model.Chauffeur;
import com.transportpro.model.enums.StatutChauffeur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Table `chauffeur` ; nom, prénom et téléphone sont lus dans `employe`. */
public class ChauffeurDAO {

    static final String SELECT = """
        SELECT c.id_chauffeur, c.id_employe, e.nom, e.prenom, e.telephone,
               c.numero_permis, c.validite_permis, c.statut
        FROM chauffeur c
        JOIN employe e ON e.id_employe = c.id_employe
        """;

    public List<Chauffeur> findAll() throws SQLException {
        List<Chauffeur> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + " ORDER BY c.id_chauffeur");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Chauffeur findByEmploye(int idEmploye) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + " WHERE c.id_employe = ?")) {
            ps.setInt(1, idEmploye);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void insert(Chauffeur ch) throws SQLException {
        String sql = "INSERT INTO chauffeur (id_employe, numero_permis, validite_permis, statut) VALUES (?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, ch);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) ch.setIdChauffeur(keys.getInt(1));
            }
        }
    }

    public void update(Chauffeur ch) throws SQLException {
        String sql = "UPDATE chauffeur SET id_employe=?, numero_permis=?, validite_permis=?, statut=? WHERE id_chauffeur=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, ch);
            ps.setInt(5, ch.getIdChauffeur());
            ps.executeUpdate();
        }
    }

    public void delete(int idChauffeur) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM chauffeur WHERE id_chauffeur=?")) {
            ps.setInt(1, idChauffeur);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Chauffeur ch) throws SQLException {
        ps.setInt(1, ch.getIdEmploye());
        ps.setString(2, ch.getNumeroPermis());
        JdbcUtil.setDate(ps, 3, ch.getValiditePermis());
        ps.setString(4, (ch.getStatut() == null ? StatutChauffeur.ACTIF : ch.getStatut()).name());
    }

    static Chauffeur map(ResultSet rs) throws SQLException {
        return new Chauffeur(
                rs.getInt("id_chauffeur"),
                rs.getInt("id_employe"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("telephone"),
                rs.getString("numero_permis"),
                JdbcUtil.date(rs, "validite_permis"),
                JdbcUtil.enumOf(StatutChauffeur.class, rs.getString("statut"), StatutChauffeur.ACTIF));
    }
}
