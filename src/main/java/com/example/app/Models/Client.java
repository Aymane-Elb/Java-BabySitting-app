package com.example.app.Models;

/**
 * Classe représentant un client dans l'application
 */
public class Client {
    private String name;
    private String location;
    private double rating;
    private double price;

    /**
     * Constructeur pour créer un nouveau client
     *
     * @param name Le nom du client
     * @param location La localisation du client
     * @param rating L'évaluation du client (sur 5)
     * @param price Le prix horaire demandé par le client
     */
    public Client(String name, String location, double rating, double price) {
        this.name = name;
        this.location = location;
        this.rating = rating;
        this.price = price;
    }

    /**
     * @return Le nom du client
     */
    public String getName() {
        return name;
    }

    /**
     * @param name Le nouveau nom du client
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return La localisation du client
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location La nouvelle localisation du client
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return L'évaluation du client
     */
    public double getRating() {
        return rating;
    }

    /**
     * @param rating La nouvelle évaluation du client
     */
    public void setRating(double rating) {
        this.rating = rating;
    }

    /**
     * @return Le prix horaire demandé par le client
     */
    public double getPrice() {
        return price;
    }

    /**
     * @param price Le nouveau prix horaire du client
     */
    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return name + " - " + location;
    }
}