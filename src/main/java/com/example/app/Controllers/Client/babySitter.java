package com.example.app.Controllers.Client;

public class babySitter {
    private final String Sitersname;
    private final String Location;
    private final double experience;
    private final double price;

    public babySitter(String Sitersname, String Location, double experience, double price) {
        this.Sitersname = Sitersname;
        this.Location = Location;
        this.experience = experience;
        this.price = price;
    }

    public String getName() { return Sitersname; }
    public String getLocation() { return Location; }
    public double getExperience() { return experience; }
    public double getPrice() { return price; }
}
