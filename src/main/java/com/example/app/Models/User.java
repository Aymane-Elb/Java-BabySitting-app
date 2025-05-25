package com.example.app.Models;

import java.time.LocalDateTime;
import java.sql.Timestamp; // Import Timestamp for direct conversion from ResultSet

public class User {
    private int id;
    private String username; // Corresponds to 'name' in your FXML
    private String email;
    private String userType;
    private String phoneNumber;
    private String address;
    private String profilePicture; // Can be null
    private String bio;            // Can be null
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;  // Can be null
    private boolean isActive;

    // Babysitter-specific fields (will be null/default for other user types)
    private String location;
    private double rating;
    private double price;

    // --- Constructors ---

    // Full constructor for retrieving from DB (including new fields)
    public User(int id, String username, String email, String userType, String phoneNumber, String address,
                String profilePicture, String bio, LocalDateTime createdAt, LocalDateTime lastLogin, boolean isActive,
                String location, double rating, double price) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.userType = userType;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.profilePicture = profilePicture;
        this.bio = bio;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.isActive = isActive;
        this.location = location;
        this.rating = rating;
        this.price = price;
    }

    // Constructor for creating a new User (e.g., during registration)
    // Note: ID, timestamps, isActive are typically set by the DB/default
    public User(String username, String email, String userType, String phoneNumber, String address) {
        this.username = username;
        this.email = email;
        this.userType = userType;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.isActive = true; // Default for new users
        // Other fields (profilePicture, bio, location, rating, price) are null/default
        this.rating = 0.0; // Default rating
        this.price = 0.0;  // Default price
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getName() { return username; } // Use getName() for consistency with UI
    public String getEmail() { return email; }
    public String getUserType() { return userType; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public String getProfilePicture() { return profilePicture; }
    public String getBio() { return bio; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public boolean isActive() { return isActive; }

    // New getters for babysitter-specific fields
    public String getLocation() { return location; }
    public double getRating() { return rating; }
    public double getPrice() { return price; }


    // --- Setters (if needed for partial updates or building User objects) ---
    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setUserType(String userType) { this.userType = userType; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setAddress(String address) { this.address = address; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
    public void setBio(String bio) { this.bio = bio; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    public void setActive(boolean active) { isActive = active; }
    public void setLocation(String location) { this.location = location; }
    public void setRating(double rating) { this.rating = rating; }
    public void setPrice(double price) { this.price = price; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", userType='" + userType + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                ", bio='" + bio + '\'' +
                ", createdAt=" + createdAt +
                ", lastLogin=" + lastLogin +
                ", isActive=" + isActive +
                ", location='" + location + '\'' +
                ", rating=" + rating +
                ", price=" + price +
                '}';
    }
}