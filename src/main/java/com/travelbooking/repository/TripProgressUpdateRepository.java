package com.travelbooking.repository;

import com.travelbooking.entity.TripProgressUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripProgressUpdateRepository extends JpaRepository<TripProgressUpdate, Long> {
    List<TripProgressUpdate> findByStaff_Id(Long staffId);
}