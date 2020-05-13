package com.example.googlemapdemo;

public class Cafe {
    private String vicinity, name, rating;

    public Cafe() {
    }

    public Cafe(String vicinity, String name, String rating) {
        this.vicinity = vicinity;
        this.name = name;
        this.rating = rating;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}