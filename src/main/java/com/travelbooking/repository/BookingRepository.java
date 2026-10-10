package com.travelbooking.repository;

import com.travelbooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdOrderByIdDesc(Long userId);

    List<Booking> findByStaffInCharge_IdOrderByTravelDateAsc(Long staffId);

    boolean existsByStaffInCharge_Id(Long staffId);

    boolean existsByProgressUpdates_Staff_Id(Long staffId);
}
