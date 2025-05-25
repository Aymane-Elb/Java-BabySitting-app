/* import com.example.app.Models.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

/*package com.example.app.Services;

import com.example.app.Models.Database;
import com.example.app.Models.HashUtils;
import com.example.app.Models.UserSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service responsable de l'authentification des utilisateurs
 */
/*
public class AuthenticationService {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());

    /**
     * Authentifie un utilisateur avec son email et mot de passe
     *
     * @param email Email de l'utilisateur
     * @param password Mot de passe en clair
     * @return true si l'authentification est réussie, false sinon
     * @throws SQLException En cas d'erreur lors de l'accès à la base de données
     */
/*
    public boolean authenticate(String email, String password) throws SQLException {
        String hashedPassword = HashUtils.hashPassword(password);

        LOGGER.info("Tentative d'authentification pour: " + email);

        if (verifyCredentials(email, hashedPassword)) {
            loadUserSession(email);
            return true;
        }

        return false;
    }

    /**
     * Vérifie les identifiants dans la base de données
     */
/*
    private boolean verifyCredentials(String email, String hashedPassword) throws SQLException {
        String sql = "SELECT id FROM users WHERE email = ? AND password = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, hashedPassword);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Retourne vrai si au moins une ligne est trouvée
            }
        }
    }

    /**
     * Charge les informations de l'utilisateur dans la session
     */
/*
    private void loadUserSession(String email) throws SQLException {
        String sql = "SELECT id, username, user_type FROM users WHERE email = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    String username = rs.getString("username");
                    String userType = rs.getString("user_type");

                    // Initialiser la session utilisateur
                    UserSession.getInstance().initSession(userId, username, email, userType);

                    LOGGER.log(Level.INFO, "Session initialisée pour l'utilisateur: {0} (ID: {1}, Rôle: {2})",
                            new Object[]{username, userId, userType});
                }
            }
        }
    }
}*/