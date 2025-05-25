package com.example.app.Models;

/**
 * Classe Modèle pour stocker les données de la session utilisateur courante
 * Implémente le pattern Singleton pour un accès global
 */
public class model {
    private static model instance;

    // Données de l'utilisateur connecté
    private int userId;
    private String username;
    private String email;
    private String role;
    private boolean isLoggedIn;

    // Constructeur privé (pattern Singleton)
    private model() {
        this.isLoggedIn = false;
    }

    /**
     * Obtient l'instance unique du modèle
     * @return L'instance du modèle
     */
    public static model getInstance() {
        if (instance == null) {
            instance = new model();
        }
        return instance;
    }

    /**
     * Définit les informations de l'utilisateur connecté
     * @param userId ID de l'utilisateur
     * @param username Nom d'utilisateur
     * @param email Email de l'utilisateur
     * @param role Rôle de l'utilisateur
     */
    public void setCurrentUser(int userId, String username, String email, String role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.isLoggedIn = true;
    }

    /**
     * Déconnecte l'utilisateur en effaçant ses données de session
     */
    public void logout() {
        this.userId = 0;
        this.username = null;
        this.email = null;
        this.role = null;
        this.isLoggedIn = false;
    }

    /**
     * Vérifie si un utilisateur est connecté
     * @return true si un utilisateur est connecté
     */
    public boolean isUserLoggedIn() {
        return this.isLoggedIn;
    }

    /**
     * Obtient l'ID de l'utilisateur connecté
     * @return ID de l'utilisateur
     */
    public int getUserId() {
        return this.userId;
    }

    /**
     * Obtient le nom d'utilisateur de l'utilisateur connecté
     * @return Nom d'utilisateur
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Obtient l'email de l'utilisateur connecté
     * @return Email de l'utilisateur
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Obtient le rôle de l'utilisateur connecté
     * @return Rôle de l'utilisateur
     */
    public String getUserRole() {
        return this.role;
    }

    /**
     * Vérifie si l'utilisateur connecté a un rôle spécifique
     * @param role Rôle à vérifier
     * @return true si l'utilisateur a le rôle spécifié
     */
    public boolean hasRole(String role) {
        return this.isLoggedIn && this.role != null && this.role.equalsIgnoreCase(role);
    }

    /**
     * Vérifie si l'utilisateur connecté est un administrateur
     * @return true si l'utilisateur est un administrateur
     */
    public boolean isAdmin() {
        return hasRole("admin");
    }

    /**
     * Vérifie si l'utilisateur connecté est un parent
     * @return true si l'utilisateur est un parent
     */
    public boolean isParent() {
        return hasRole("parent");
    }

    /**
     * Vérifie si l'utilisateur connecté est un baby-sitter
     * @return true si l'utilisateur est un baby-sitter
     */
    public boolean isBabysitter() {
        return hasRole("babysitter");
    }
}