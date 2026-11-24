package com.transportpro.dao;

import com.transportpro.model.Trajet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Table `trajet` (itinéraires). */
public class TrajetDAO {

    public List<Trajet> findAll() throws SQLException {
        List<Trajet> list = new ArrayList<>();
        String sql = "SELECT id_trajet, nom, lieu_depart, lieu_arrivee FROM trajet ORDER BY id_trajet";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Trajet(
                        rs.getInt("id_trajet"),
                        rs.getString("nom"),
                        rs.getString("lieu_depart"),
                        rs.getString("lieu_arrivee")));
            }
        }
        return list;
    }

    public void insert(Trajet t) throws SQLException {
        String sql = "INSERT INTO trajet (nom, lieu_depart, lieu_arrivee) VALUES (?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getNom());
            ps.setString(2, t.getLieuDepart());
            ps.setString(3, t.getLieuArrivee());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) t.setIdTrajet(keys.getInt(1));
            }
        }
    }

    public void update(Trajet t) throws SQLException {
        String sql = "UPDATE trajet SET nom=?, lieu_depart=?, lieu_arrivee=? WHERE id_trajet=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t.getNom());
            ps.setString(2, t.getLieuDepart());
            ps.setString(3, t.getLieuArrivee());
            ps.setInt(4, t.getIdTrajet());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM trajet WHERE id_trajet=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
