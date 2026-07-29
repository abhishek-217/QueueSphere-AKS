package com.healthcare.queue.service;

import com.healthcare.queue.dto.DoctorResponse;
import com.healthcare.queue.exception.ResourceNotFoundException;
import com.healthcare.queue.model.Doctor;
import com.healthcare.queue.model.DoctorAvailability;
import com.healthcare.queue.repository.DoctorAvailabilityRepository;
import com.healthcare.queue.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;

    public List<DoctorResponse> getAllActiveDoctors() {
        return doctorRepository.findByActiveTrue().stream()
                .map(DoctorResponse::from)
                .toList();
    }

    public List<DoctorResponse> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecializationIgnoreCaseAndActiveTrue(specialization).stream()
                .map(DoctorResponse::from)
                .toList();
    }

    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        return DoctorResponse.from(doctor);
    }

    public List<DoctorAvailability> getWeeklyAvailability(Long doctorId) {
        return availabilityRepository.findByDoctorIdAndActiveTrue(doctorId);
    }

    public List<DoctorAvailability> getAvailabilityForDay(Long doctorId, DayOfWeek dayOfWeek) {
        return availabilityRepository.findByDoctorIdAndDayOfWeekAndActiveTrue(doctorId, dayOfWeek);
    }

    /**
     * Adds a weekly availability slot for a doctor. Only the doctor themself (matched by
     * their account email) or an admin may modify a doctor's schedule.
     */
    public DoctorAvailability addAvailability(String requesterEmail, boolean isAdmin, Long doctorId,
                                               com.healthcare.queue.dto.AvailabilityRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        if (!isAdmin && !doctor.getUser().getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new com.healthcare.queue.exception.BadRequestException(
                    "You can only manage your own availability");
        }

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new com.healthcare.queue.exception.BadRequestException(
                    "Start time must be before end time");
        }

        DoctorAvailability availability = DoctorAvailability.builder()
                .doctor(doctor)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .slotDurationMinutes(request.getSlotDurationMinutes() > 0 ? request.getSlotDurationMinutes() : 15)
                .active(true)
                .build();
        return availabilityRepository.save(availability);
    }
}
