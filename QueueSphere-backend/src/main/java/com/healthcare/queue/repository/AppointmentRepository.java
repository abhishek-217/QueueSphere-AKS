package com.healthcare.queue.repository;

import com.healthcare.queue.model.Appointment;
import com.healthcare.queue.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdOrderByScheduledTimeDesc(Long patientId);

    List<Appointment> findByDoctorIdAndScheduledTimeBetweenOrderByScheduledTimeAsc(
            Long doctorId, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByDoctorIdAndStatusOrderByScheduledTimeAsc(Long doctorId, AppointmentStatus status);

    boolean existsByDoctorIdAndScheduledTime(Long doctorId, LocalDateTime scheduledTime);
}
