package com.healthcare.queue.controller;

import com.healthcare.queue.dto.AppointmentRequest;
import com.healthcare.queue.dto.AppointmentResponse;
import com.healthcare.queue.dto.QueueStatusMessage;
import com.healthcare.queue.service.AppointmentService;
import com.healthcare.queue.service.QueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final QueueService queueService;

    /** Patient books a new appointment. */
    @PostMapping
    public ResponseEntity<AppointmentResponse> book(Authentication authentication,
                                                      @Valid @RequestBody AppointmentRequest request) {
        String email = authentication.getName();
        return ResponseEntity.ok(appointmentService.bookAppointment(email, request));
    }

    /** Patient views their own appointment history. */
    @GetMapping("/me")
    public ResponseEntity<List<AppointmentResponse>> myAppointments(Authentication authentication) {
        return ResponseEntity.ok(appointmentService.getPatientAppointments(authentication.getName()));
    }

    /** Patient cancels an upcoming appointment. */
    @DeleteMapping("/{id}")
    public ResponseEntity<AppointmentResponse> cancel(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(authentication.getName(), id));
    }

    /** Patient checks in on arrival -> joins the live queue. */
    @PostMapping("/{id}/check-in")
    public ResponseEntity<QueueStatusMessage> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(queueService.checkIn(id));
    }

    /** Doctor/Admin views a specific doctor's appointments for a given day. */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponse>> doctorAppointments(
            @PathVariable Long doctorId,
            @RequestParam(required = false) String date) {
        LocalDate day = (date != null) ? LocalDate.parse(date) : LocalDate.now();
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = day.plusDays(1).atStartOfDay();
        return ResponseEntity.ok(appointmentService.getDoctorAppointmentsForDay(doctorId, start, end));
    }
}
