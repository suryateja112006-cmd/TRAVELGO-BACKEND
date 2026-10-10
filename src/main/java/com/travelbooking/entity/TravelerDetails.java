package com.travelbooking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class TravelerDetails {

    @Column(name = "traveler_name", nullable = false, length = 100)
    private String name;

    @Column(name = "traveler_email", nullable = false, length = 100)
    private String email;

    @Column(name = "traveler_phone", nullable = false, length = 20)
    private String phone;

    public TravelerDetails() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
