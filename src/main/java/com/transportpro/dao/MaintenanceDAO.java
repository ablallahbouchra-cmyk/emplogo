package com.transportpro.dao;

import com.transportpro.model.Maintenance;
import com.transportpro.model.enums.StatutMaintenance;
import com.transportpro.model.enums.TypeMaintenance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Table `maintenance` ; le trigger synchronise le statut du bus. */
public class MaintenanceDAO {

    public List<Maintenance> findAll() throws SQLException {
        String sql = """
            SELECT m.id_maintenance, m.type, m.date_debut, m.date_fin, m.statut AS maintenance_statut,
                   b.id_bus, b.immatriculation, b.marque, b.modele, b.capacite, b.statut
            FROM maintenance m
            JOIN bus b ON b.id_bus = m.id_bus
            ORDER BY m.date_debut DESC
            """;
        List<Maintenance> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Maintenance(rs.getInt("id_maintenance"), BusDAO.map(rs),
                        JdbcUtil.enumOf(TypeMaintenance.class, rs.getString("type"), TypeMaintenance.PREVENTIVE),
                        JdbcUtil.date(rs, "date_debut"), JdbcUtil.date(rs, "date_fin"),
                        JdbcUtil.enumOf(StatutMaintenance.class, rs.getString("maintenance_statut"), StatutMaintenance.PLANIFIEE)));
            }
        }
        return list;
    }

    public void insert(Maintenance m) throws SQLException {
        String sql = "INSERT INTO maintenance (id_bus, type, date_debut, date_fin, statut) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getBus().getIdBus());
            ps.setString(2, m.getType().name());
            JdbcUtil.setDate(ps, 3, m.getDateDebut());
            JdbcUtil.setDate(ps, 4, m.getDateFin());
            ps.setString(5, m.getStatut().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) m.setIdMaintenance(keys.getInt(1));
            }
        }
    }
}
