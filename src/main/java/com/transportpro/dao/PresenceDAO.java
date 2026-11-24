package com.transportpro.dao;

import com.transportpro.model.Presence;
import com.transportpro.model.enums.StatutPresence;

import java.sql.*;
import java.util.List;

/** Table `presence` : une ligne au plus par inscription (UNIQUE id_inscription). */
public class PresenceDAO {

    private static final String UPSERT = """
        INSERT INTO presence (id_inscription, statut, heure_montee, remarque)
        VALUES (?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE statut = VALUES(statut),
                                heure_montee = VALUES(heure_montee),
                                remarque = VALUES(remarque)
        """;

    public void upsert(Presence p) throws SQLException {
        upsertAll(List.of(p));
    }

    /** Enregistre toutes les présences dans une seule transaction. */
    public void upsertAll(List<Presence> presences) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(UPSERT)) {
                for (Presence p : presences) {
                    if (p.getStatut() == null || p.getStatut() == StatutPresence.NON_RENSEIGNE) continue;
                    ps.setInt(1, p.getIdInscription());
                    ps.setString(2, p.getStatut().name());
                    JdbcUtil.setTime(ps, 3, p.getHeureMontee());
                    ps.setString(4, p.getRemarque());
                    ps.addBatch();
                }
                ps.executeBatch();
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }
}
