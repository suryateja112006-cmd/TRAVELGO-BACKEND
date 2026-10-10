package com.travelbooking.service;

import com.travelbooking.entity.TravelPackage;
import com.travelbooking.repository.TravelPackageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PackageService {

    private final TravelPackageRepository packageRepository;

    public PackageService(TravelPackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    public List<TravelPackage> getAllPackages() {
        return packageRepository.findByActiveTrue();
    }

    public List<TravelPackage> getAllPackagesForAdmin() {
        return packageRepository.findAll();
    }

    public TravelPackage getPackageById(Long id) {
        return packageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Travel package not found with id: " + id));
    }

    public TravelPackage createPackage(TravelPackage travelPackage) {
        validateDiscount(travelPackage.getDiscountPercent());
        if (travelPackage.getDiscountPercent() == null)
            travelPackage.setDiscountPercent(0.0);
        return packageRepository.save(travelPackage);
    }

    public TravelPackage updatePackage(Long id, TravelPackage packageDetails) {
        TravelPackage existing = getPackageById(id);
        if (!existing.isActive()) {
            throw new IllegalArgumentException("Deleted packages cannot be edited.");
        }
        existing.setName(packageDetails.getName());
        existing.setDestination(packageDetails.getDestination());
        existing.setDescription(packageDetails.getDescription());
        existing.setDuration(packageDetails.getDuration());
        existing.setPrice(packageDetails.getPrice());
        validateDiscount(packageDetails.getDiscountPercent());
        existing.setDiscountPercent(
                packageDetails.getDiscountPercent() == null ? 0.0 : packageDetails.getDiscountPercent());
        existing.setFamilyFriendly(packageDetails.isFamilyFriendly());
        existing.setGroupTrip(packageDetails.isGroupTrip());
        existing.setPlaces(packageDetails.getPlaces());
        existing.setPlaceImages(packageDetails.getPlaceImages());
        existing.setPlaceImageSources(packageDetails.getPlaceImageSources());
        return packageRepository.save(existing);
    }

    private void validateDiscount(Double discountPercent) {
        if (discountPercent != null
                && (!Double.isFinite(discountPercent) || discountPercent < 0 || discountPercent > 90)) {
            throw new IllegalArgumentException("Discount must be between 0 and 90 percent.");
        }
    }

    public void deletePackage(Long id) {
        TravelPackage travelPackage = getPackageById(id);
        if (!travelPackage.isActive()) {
            throw new IllegalArgumentException("Travel package has already been deleted.");
        }
        travelPackage.setActive(false);
        packageRepository.save(travelPackage);
    }
}
