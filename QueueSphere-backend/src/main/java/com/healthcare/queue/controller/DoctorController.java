package com.healthcare.queue.controller;

import com.healthcare.queue.dto.AvailabilityRequest;
import com.healthcare.queue.dto.AvailabilityResponse;
import com.healthcare.queue.dto.DoctorResponse;
import com.healthcare.queue.model.DoctorAvailability;
import com.healthcare.queue.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> getAllDoctors(
            @RequestParam(required = false) String specialization) {
        if (specialization != null && !specialization.isBlank()) {
            return ResponseEntity.ok(doctorService.getDoctorsBySpecialization(specialization));
        }
        return ResponseEntity.ok(doctorService.getAllActiveDoctors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<List<AvailabilityResponse>> getAvailability(@PathVariable Long id) {
        List<AvailabilityResponse> response = doctorService.getWeeklyAvailability(id).stream()
                .map(AvailabilityResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /** Doctor sets their own weekly working hours (admins may set for any doctor). */
    @PostMapping("/{id}/availability")
    public ResponseEntity<AvailabilityResponse> addAvailability(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityRequest request) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        DoctorAvailability saved = doctorService.addAvailability(authentication.getName(), isAdmin, id, request);
        return ResponseEntity.ok(AvailabilityResponse.from(saved));
    }
}
