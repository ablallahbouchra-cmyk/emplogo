package com.transportpro.dao;

import com.transportpro.model.CompteUtilisateur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Table `compte_utilisateur` (+ rôle et employé lié). */
public class CompteUtilisateurDAO {

    private static final String SELECT = """
        SELECT cu.id_compte, cu.login, cu.mot_de_passe_hash, cu.actif, cu.derniere_connexion,
               cu.id_role, r.nom AS role_nom, cu.id_employe, e.nom, e.prenom
        FROM compte_utilisateur cu
        JOIN role r ON r.id_role = cu.id_role
        LEFT JOIN employe e ON e.id_employe = cu.id_employe
        """;

    public CompteUtilisateur findByLogin(String login) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + " WHERE cu.login = ?")) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<CompteUtilisateur> findAll() throws SQLException {
        List<CompteUtilisateur> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + " ORDER BY cu.id_compte");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public void updateMotDePasse(int idCompte, String hash) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE compte_utilisateur SET mot_de_passe_hash=? WHERE id_compte=?")) {
            ps.setString(1, hash);
            ps.setInt(2, idCompte);
            ps.executeUpdate();
        }
    }

    public void majDerniereConnexion(int idCompte) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE compte_utilisateur SET derniere_connexion=NOW() WHERE id_compte=?")) {
            ps.setInt(1, idCompte);
            ps.executeUpdate();
        }
    }

    private CompteUtilisateur map(ResultSet rs) throws SQLException {
        CompteUtilisateur cu = new CompteUtilisateur(rs.getInt("id_compte"), rs.getString("login"),
                rs.getString("mot_de_passe_hash"), rs.getBoolean("actif"), JdbcUtil.dateTime(rs, "derniere_connexion"),
                rs.getInt("id_role"), rs.getString("role_nom"), JdbcUtil.intOrNull(rs, "id_employe"));
        cu.setNomEmploye(rs.getString("nom"));
        cu.setPrenomEmploye(rs.getString("prenom"));
        return cu;
    }
}
