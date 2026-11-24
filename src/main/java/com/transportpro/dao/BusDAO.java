package com.transportpro.dao;

import com.transportpro.model.Bus;
import com.transportpro.model.enums.StatutBus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BusDAO {

    public List<Bus> findAll() throws SQLException {
        List<Bus> list = new ArrayList<>();
        String sql = "SELECT id_bus, immatriculation, marque, modele, capacite, statut FROM bus ORDER BY id_bus";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public void insert(Bus b) throws SQLException {
        String sql = "INSERT INTO bus (immatriculation, marque, modele, capacite, statut) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, b);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) b.setIdBus(keys.getInt(1));
            }
        }
    }

    public void update(Bus b) throws SQLException {
        String sql = "UPDATE bus SET immatriculation=?, marque=?, modele=?, capacite=?, statut=? WHERE id_bus=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, b);
            ps.setInt(6, b.getIdBus());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM bus WHERE id_bus=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Bus b) throws SQLException {
        ps.setString(1, b.getImmatriculation());
        ps.setString(2, b.getMarque());
        ps.setString(3, b.getModele());
        ps.setInt(4, b.getCapacite());
        ps.setString(5, (b.getStatut() == null ? StatutBus.DISPONIBLE : b.getStatut()).name());
    }

    static Bus map(ResultSet rs) throws SQLException {
        return new Bus(
                rs.getInt("id_bus"),
                rs.getString("immatriculation"),
                rs.getString("marque"),
                rs.getString("modele"),
                rs.getInt("capacite"),
                JdbcUtil.enumOf(StatutBus.class, rs.getString("statut"), StatutBus.DISPONIBLE));
    }
}
