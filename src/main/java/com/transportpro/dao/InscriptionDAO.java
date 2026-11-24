package com.transportpro.dao;

import com.transportpro.model.Employe;
import com.transportpro.model.Inscription;
import com.transportpro.model.Presence;
import com.transportpro.model.enums.StatutInscription;
import com.transportpro.model.enums.StatutPresence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Table `inscription` (+ employé et présence éventuelle). */
public class InscriptionDAO {

    public List<Inscription> findByAffectation(int idAffectation) throws SQLException {
        String sql = """
            SELECT i.id_inscription, i.id_employe, i.id_affectation, i.date_inscription, i.statut,
                   e.matricule, e.nom, e.prenom, e.email, e.telephone, e.adresse, e.departement,
                   e.date_embauche, e.actif,
                   p.id_presence, p.statut AS presence_statut, p.heure_montee, p.remarque
            FROM inscription i
            JOIN employe e ON e.id_employe = i.id_employe
            LEFT JOIN presence p ON p.id_inscription = i.id_inscription
            WHERE i.id_affectation = ?
            ORDER BY i.statut, e.nom, e.prenom
            """;
        List<Inscription> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idAffectation);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Inscription i = new Inscription(rs.getInt("id_inscription"), rs.getInt("id_employe"),
                            rs.getInt("id_affectation"), JdbcUtil.dateTime(rs, "date_inscription"),
                            JdbcUtil.enumOf(StatutInscription.class, rs.getString("statut"), StatutInscription.CONFIRMEE));
                    i.setEmploye(new Employe(rs.getInt("id_employe"), rs.getString("matricule"),
                            rs.getString("nom"), rs.getString("prenom"), rs.getString("email"),
                            rs.getString("telephone"), rs.getString("adresse"), rs.getString("departement"),
                            JdbcUtil.date(rs, "date_embauche"), rs.getBoolean("actif")));
                    Integer idPresence = JdbcUtil.intOrNull(rs, "id_presence");
                    if (idPresence != null) {
                        i.setPresence(new Presence(idPresence, i.getIdInscription(),
                                JdbcUtil.enumOf(StatutPresence.class, rs.getString("presence_statut"), StatutPresence.NON_RENSEIGNE),
                                JdbcUtil.time(rs, "heure_montee"), rs.getString("remarque")));
                    }
                    list.add(i);
                }
            }
        }
        return list;
    }

    /**
     * Le trigger refuse l'inscription (employé désactivé, bus complet, doublon) via SIGNAL.
     * Une inscription ANNULEE existante (même employé, même affectation) est réactivée.
     */
    public void insert(Inscription i) throws SQLException {
        String sql = """
            INSERT INTO inscription (id_employe, id_affectation, statut) VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE id_inscription = LAST_INSERT_ID(id_inscription),
                                    statut = VALUES(statut),
                                    date_inscription = CURRENT_TIMESTAMP
            """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i.getIdEmploye());
            ps.setInt(2, i.getIdAffectation());
            ps.setString(3, (i.getStatut() == null ? StatutInscription.CONFIRMEE : i.getStatut()).name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) i.setIdInscription(keys.getInt(1));
            }
        }
    }

    public void annuler(int idInscription) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE inscription SET statut='ANNULEE' WHERE id_inscription=?")) {
            ps.setInt(1, idInscription);
            ps.executeUpdate();
        }
    }
}
