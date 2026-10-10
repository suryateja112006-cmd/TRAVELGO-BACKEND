package com.travelbooking.service;

import com.travelbooking.entity.TravelPackage;
import com.travelbooking.repository.TravelPackageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageServiceTests {

    @Mock
    private TravelPackageRepository packageRepository;

    @InjectMocks
    private PackageService packageService;

    @Test
    void publicPackageListExcludesDeletedPackages() {
        TravelPackage activePackage = new TravelPackage();
        when(packageRepository.findByActiveTrue()).thenReturn(List.of(activePackage));

        List<TravelPackage> packages = packageService.getAllPackages();

        assertSame(activePackage, packages.get(0));
        verify(packageRepository).findByActiveTrue();
    }

    @Test
    void adminPackageListRetainsDeletedPackages() {
        TravelPackage deletedPackage = new TravelPackage();
        deletedPackage.setActive(false);
        when(packageRepository.findAll()).thenReturn(List.of(deletedPackage));

        List<TravelPackage> packages = packageService.getAllPackagesForAdmin();

        assertSame(deletedPackage, packages.get(0));
    }

    @Test
    void deletePackageMarksItInactiveWithoutRemovingBookingReference() {
        TravelPackage travelPackage = new TravelPackage();
        when(packageRepository.findById(4L)).thenReturn(Optional.of(travelPackage));
        when(packageRepository.save(travelPackage)).thenReturn(travelPackage);

        packageService.deletePackage(4L);

        assertFalse(travelPackage.isActive());
        verify(packageRepository).save(travelPackage);
        verify(packageRepository, never()).deleteById(4L);
    }

    @Test
    void deletedPackageCannotBeEdited() {
        TravelPackage deletedPackage = new TravelPackage();
        deletedPackage.setActive(false);
        when(packageRepository.findById(4L)).thenReturn(Optional.of(deletedPackage));

        assertThrows(IllegalArgumentException.class,
                () -> packageService.updatePackage(4L, new TravelPackage()));
        verify(packageRepository, never()).save(any(TravelPackage.class));
    }
}
