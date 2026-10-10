package com.travelbooking.controller;

public record TripProgressRequest(String location, Double latitude, Double longitude, boolean reached) {
}