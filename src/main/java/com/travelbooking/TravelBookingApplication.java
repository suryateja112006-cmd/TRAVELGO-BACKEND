package com.travelbooking;

import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Entry Point for Travel Booking System (TravelGo) Backend.
 * College Full Stack Java Project with Spring Boot & MySQL.
 */
@SpringBootApplication
public class TravelBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelBookingApplication.class, args);
        System.out.println("=================================================");
        System.out.println(" TravelGo Backend Server Started Successfully! ");
        System.out.println(" Spring Boot Version: " + SpringBootVersion.getVersion());
        System.out.println(" REST API Base URL: http://localhost:8085/api    ");
        System.out.println("=================================================");
    }
}
