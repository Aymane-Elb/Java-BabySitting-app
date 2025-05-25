package com.example.app.Services;

import com.example.app.Models.User; // Assuming your User model is in this package

/**
 * Manages the current user session. This is a simple singleton-like class
 * to hold the logged-in user's information globally.
 */
public class SessionManager {
    private static User currentUser;

    /**
     * Sets the currently logged-in user.
     * This should be called after successful authentication.
     * @param user The User object of the logged-in user.
     */
    public static void setCurrentUser(User user) {
        SessionManager.currentUser = user;
    }

    /**
     * Retrieves the currently logged-in user.
     *
     * @return The User object, or null if no user is logged in.
     */
    public static User getCurrentUser() {
        return currentUser; // Correctly return the static currentUser
    }

    /**
     * Retrieves the ID of the currently logged-in user.
     * @return The user's ID, or -1 if no user is logged in.
     */
    public static int getCurrentUserId() {
        return (currentUser != null) ? currentUser.getId() : -1;
    }

    /**
     * Retrieves the user type of the currently logged-in user.
     * @return The user's type (e.g., "client", "babysitter"), or null if no user is logged in.
     */
    public static String getCurrentUserType() {
        return (currentUser != null) ? currentUser.getUserType() : null;
    }

    /**
     * Clears the current user session, effectively logging out the user.
     */
    public static void clearSession() {
        currentUser = null;
    }

    /**
     * Checks if a user is currently logged in.
     * @return true if a user is logged in, false otherwise.
     */
    public static boolean isLoggedIn() {
        return currentUser != null; // Checks if the currentUser object is set
    }

    // The previous `public static Object getUserType()` method was redundant
    // as `public static String getCurrentUserType()` already provides the user type.
    // It has been removed to avoid confusion and maintain consistency.
}