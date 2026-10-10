package com.travelbooking.controller;

import com.travelbooking.entity.TravelPackage;
import com.travelbooking.service.PackageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
public class PackageController {

    private final PackageService packageService;

    public PackageController(PackageService packageService) {
        this.packageService = packageService;
    }

    /**
     * GET /api/packages
     * Retrieve all travel packages
     */
    @GetMapping
    public ResponseEntity<List<TravelPackage>> getAllPackages() {
        return ResponseEntity.ok(packageService.getAllPackages());
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<TravelPackage>> getAllPackagesForAdmin() {
        return ResponseEntity.ok(packageService.getAllPackagesForAdmin());
    }

    /**
     * GET /api/packages/{id}
     * Retrieve a specific travel package by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPackageById(@PathVariable Long id) {
        try {
            TravelPackage pkg = packageService.getPackageById(id);
            return ResponseEntity.ok(pkg);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * POST /api/packages
     * Create a new travel package (Admin)
     */
    @PostMapping
    public ResponseEntity<?> createPackage(@RequestBody TravelPackage travelPackage) {
        try {
            TravelPackage created = packageService.createPackage(travelPackage);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating package: " + e.getMessage());
        }
    }

    /**
     * PUT /api/packages/{id}
     * Update an existing travel package (Admin)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePackage(@PathVariable Long id, @RequestBody TravelPackage travelPackage) {
        try {
            TravelPackage updated = packageService.updatePackage(id, travelPackage);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating package: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/packages/{id}
     * Delete a travel package (Admin)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePackage(@PathVariable Long id) {
        try {
            packageService.deletePackage(id);
            return ResponseEntity.ok("Package deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting package: " + e.getMessage());
        }
    }
}
