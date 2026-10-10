package com.travelbooking.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Booking Entity representing a travel package reservation made by a user.
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private TravelPackage travelPackage;

    @ManyToOne
    @JoinColumn(name = "staff_in_charge_id")
    private User staffInCharge;

    @Column(name = "travel_date", nullable = false, length = 50)
    private String travelDate;

    @Column(name = "number_of_persons", nullable = false)
    private Integer numberOfPersons;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(nullable = false, length = 50)
    private String status = "CONFIRMED"; // CONFIRMED or CANCELLED

    @Column(name = "payment_status", nullable = false, length = 20, columnDefinition = "varchar(20) default 'UNPAID'")
    private String paymentStatus = "UNPAID";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "booking_travelers", joinColumns = @JoinColumn(name = "booking_id"))
    @OrderColumn(name = "traveler_order")
    private List<TravelerDetails> travelers = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("reportedAt ASC, id ASC")
    private List<TripProgressUpdate> progressUpdates = new ArrayList<>();

    @Transient
    private String notificationEmailStatus;

    @Transient
    private String notificationSmsStatus;

    public Booking() {
    }

    public Booking(Long id, User user, TravelPackage travelPackage, String travelDate, Integer numberOfPersons,
            Double totalAmount, String status) {
        this.id = id;
        this.user = user;
        this.travelPackage = travelPackage;
        this.travelDate = travelDate;
        this.numberOfPersons = numberOfPersons;
        this.totalAmount = totalAmount;
        this.status = (status != null) ? status : "CONFIRMED";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TravelPackage getTravelPackage() {
        return travelPackage;
    }

    public void setTravelPackage(TravelPackage travelPackage) {
        this.travelPackage = travelPackage;
    }

    public User getStaffInCharge() {
        return staffInCharge;
    }

    public void setStaffInCharge(User staffInCharge) {
        this.staffInCharge = staffInCharge;
    }

    public String getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(String travelDate) {
        this.travelDate = travelDate;
    }

    public Integer getNumberOfPersons() {
        return numberOfPersons;
    }

    public void setNumberOfPersons(Integer numberOfPersons) {
        this.numberOfPersons = numberOfPersons;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public List<TravelerDetails> getTravelers() {
        return travelers;
    }

    public void setTravelers(List<TravelerDetails> travelers) {
        this.travelers = travelers;
    }

    public List<TripProgressUpdate> getProgressUpdates() {
        return progressUpdates;
    }

    public void setProgressUpdates(List<TripProgressUpdate> progressUpdates) {
        this.progressUpdates = progressUpdates;
    }

    public String getNotificationEmailStatus() {
        return notificationEmailStatus;
    }

    public void setNotificationEmailStatus(String notificationEmailStatus) {
        this.notificationEmailStatus = notificationEmailStatus;
    }

    public String getNotificationSmsStatus() {
        return notificationSmsStatus;
    }

    public void setNotificationSmsStatus(String notificationSmsStatus) {
        this.notificationSmsStatus = notificationSmsStatus;
    }
}
