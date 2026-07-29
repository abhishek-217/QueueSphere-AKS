package com.healthcare.queue.service;

import com.healthcare.queue.dto.AppointmentRequest;
import com.healthcare.queue.dto.AppointmentResponse;
import com.healthcare.queue.exception.BadRequestException;
import com.healthcare.queue.exception.ResourceNotFoundException;
import com.healthcare.queue.model.Appointment;
import com.healthcare.queue.model.AppointmentStatus;
import com.healthcare.queue.model.Doctor;
import com.healthcare.queue.model.Patient;
import com.healthcare.queue.repository.AppointmentRepository;
import com.healthcare.queue.repository.DoctorAvailabilityRepository;
import com.healthcare.queue.repository.DoctorRepository;
import com.healthcare.queue.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;

    @Transactional
    public AppointmentResponse bookAppointment(String patientEmail, AppointmentRequest request) {
        Patient patient = patientRepository.findByUserEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        if (!doctor.isActive()) {
            throw new BadRequestException("This doctor is not currently accepting appointments");
        }

        if (appointmentRepository.existsByDoctorIdAndScheduledTime(doctor.getId(), request.getScheduledTime())) {
            throw new BadRequestException("This time slot is already booked. Please choose another.");
        }

        // Only enforce working-hours validation once a doctor has actually configured
        // availability; doctors with no schedule set up yet can still receive bookings.
        List<com.healthcare.queue.model.DoctorAvailability> allSlots =
                availabilityRepository.findByDoctorIdAndActiveTrue(doctor.getId());
        if (!allSlots.isEmpty()) {
            DayOfWeek requestedDay = request.getScheduledTime().getDayOfWeek();
            LocalTime requestedTime = request.getScheduledTime().toLocalTime();
            boolean withinWorkingHours = allSlots.stream()
                    .filter(slot -> slot.getDayOfWeek() == requestedDay)
                    .anyMatch(slot -> !requestedTime.isBefore(slot.getStartTime())
                            && requestedTime.isBefore(slot.getEndTime()));
            if (!withinWorkingHours) {
                throw new BadRequestException(
                        "Dr. " + doctor.getUser().getFullName() + " is not available at that day/time. "
                                + "Check their schedule via GET /api/doctors/" + doctor.getId() + "/availability");
            }
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .scheduledTime(request.getScheduledTime())
                .reasonForVisit(request.getReasonForVisit())
                .status(AppointmentStatus.BOOKED)
                .build();

        appointment = appointmentRepository.save(appointment);
        return AppointmentResponse.from(appointment);
    }

    public List<AppointmentResponse> getPatientAppointments(String patientEmail) {
        Patient patient = patientRepository.findByUserEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        return appointmentRepository.findByPatientIdOrderByScheduledTimeDesc(patient.getId())
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    public List<AppointmentResponse> getDoctorAppointmentsForDay(Long doctorId, LocalDateTime dayStart, LocalDateTime dayEnd) {
        return appointmentRepository.findByDoctorIdAndScheduledTimeBetweenOrderByScheduledTimeAsc(doctorId, dayStart, dayEnd)
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    @Transactional
    public AppointmentResponse cancelAppointment(String patientEmail, Long appointmentId) {
        Patient patient = patientRepository.findByUserEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new BadRequestException("You can only cancel your own appointments");
        }
        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new BadRequestException("Only booked appointments can be cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        return AppointmentResponse.from(appointment);
    }
}
