package com.travelbooking.entity;

import jakarta.persistence.*;

/**
 * TravelPackage Entity representing tour packages.
 */
@Entity
@Table(name = "travel_packages")
public class TravelPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String destination;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String duration;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false, columnDefinition = "double default 0")
    private Double discountPercent = 0.0;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean familyFriendly;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean groupTrip;

    @Column(columnDefinition = "TEXT")
    private String places;

    @Column(columnDefinition = "TEXT")
    private String placeImages;

    @Column(columnDefinition = "TEXT")
    private String placeImageSources;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    public TravelPackage() {
    }

    public TravelPackage(Long id, String name, String destination, String description, String duration, Double price) {
        this.id = id;
        this.name = name;
        this.destination = destination;
        this.description = description;
        this.duration = duration;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public boolean isFamilyFriendly() {
        return familyFriendly;
    }

    public void setFamilyFriendly(boolean familyFriendly) {
        this.familyFriendly = familyFriendly;
    }

    public boolean isGroupTrip() {
        return groupTrip;
    }

    public void setGroupTrip(boolean groupTrip) {
        this.groupTrip = groupTrip;
    }

    public String getPlaces() {
        return places;
    }

    public void setPlaces(String places) {
        this.places = places;
    }

    public String getPlaceImages() {
        return placeImages;
    }

    public void setPlaceImages(String placeImages) {
        this.placeImages = placeImages;
    }

    public String getPlaceImageSources() {
        return placeImageSources;
    }

    public void setPlaceImageSources(String placeImageSources) {
        this.placeImageSources = placeImageSources;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
