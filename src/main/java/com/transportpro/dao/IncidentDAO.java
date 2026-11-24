package com.transportpro.dao;

import com.transportpro.model.Incident;
import com.transportpro.model.enums.GraviteIncident;
import com.transportpro.model.enums.ModeDeclaration;
import com.transportpro.model.enums.StatutIncident;
import com.transportpro.model.enums.TypeIncident;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Table `incident`. */
public class IncidentDAO {

    public List<Incident> findAll() throws SQLException {
        String sql = """
            SELECT i.id_incident, i.id_affectation, i.id_compte, i.type, i.gravite, i.description,
                   i.date_incident, i.statut, i.mode_declaration,
                   t.nom AS trajet_nom, b.immatriculation, e.nom, e.prenom, cu.login
            FROM incident i
            JOIN affectation a ON a.id_affectation = i.id_affectation
            JOIN trajet t ON t.id_trajet = a.id_trajet
            JOIN bus b ON b.id_bus = a.id_bus
            JOIN chauffeur c ON c.id_chauffeur = a.id_chauffeur
            JOIN employe e ON e.id_employe = c.id_employe
            LEFT JOIN compte_utilisateur cu ON cu.id_compte = i.id_compte
            ORDER BY i.date_incident DESC, i.id_incident DESC
            """;
        List<Incident> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Incident i = new Incident(rs.getInt("id_incident"),
                        JdbcUtil.enumOf(TypeIncident.class, rs.getString("type"), TypeIncident.AUTRE),
                        rs.getString("description"), JdbcUtil.dateTime(rs, "date_incident"));
                i.setIdAffectation(rs.getInt("id_affectation"));
                i.setIdCompte(JdbcUtil.intOrNull(rs, "id_compte"));
                i.setGravite(JdbcUtil.enumOf(GraviteIncident.class, rs.getString("gravite"), GraviteIncident.MINEURE));
                i.setStatut(JdbcUtil.enumOf(StatutIncident.class, rs.getString("statut"), StatutIncident.OUVERT));
                i.setModeDeclaration(JdbcUtil.enumOf(ModeDeclaration.class, rs.getString("mode_declaration"), ModeDeclaration.APPLICATION));
                i.setNomTrajet(rs.getString("trajet_nom"));
                i.setImmatriculationBus(rs.getString("immatriculation"));
                i.setNomChauffeur(rs.getString("prenom") + " " + rs.getString("nom"));
                i.setLoginDeclarant(rs.getString("login"));
                list.add(i);
            }
        }
        return list;
    }

    public void insert(Incident i) throws SQLException {
        String sql = """
            INSERT INTO incident (id_affectation, id_compte, type, gravite, description,
                                  date_incident, statut, mode_declaration)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i.getIdAffectation());
            JdbcUtil.setInt(ps, 2, i.getIdCompte());
            ps.setString(3, i.getType().name());
            ps.setString(4, i.getGravite().name());
            ps.setString(5, i.getDescription());
            JdbcUtil.setDateTime(ps, 6, i.getDateHeure() == null ? LocalDateTime.now() : i.getDateHeure());
            ps.setString(7, i.getStatut().name());
            ps.setString(8, i.getModeDeclaration().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) i.setIdIncident(keys.getInt(1));
            }
        }
    }

    public void updateStatut(int idIncident, StatutIncident statut) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE incident SET statut=? WHERE id_incident=?")) {
            ps.setString(1, statut.name());
            ps.setInt(2, idIncident);
            ps.executeUpdate();
        }
    }
}
