package com.healthcare.queue.repository;

import com.healthcare.queue.model.AppointmentStatus;
import com.healthcare.queue.model.QueueEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface QueueEntryRepository extends JpaRepository<QueueEntry, Long> {

    List<QueueEntry> findByDoctorIdAndQueueDateOrderByQueueNumberAsc(Long doctorId, LocalDate queueDate);

    List<QueueEntry> findByDoctorIdAndQueueDateAndStatusInOrderByQueueNumberAsc(
            Long doctorId, LocalDate queueDate, List<AppointmentStatus> statuses);

    Optional<QueueEntry> findByAppointmentId(Long appointmentId);

    int countByDoctorIdAndQueueDate(Long doctorId, LocalDate queueDate);

    Optional<QueueEntry> findFirstByDoctorIdAndQueueDateAndStatusOrderByQueueNumberAsc(
            Long doctorId, LocalDate queueDate, AppointmentStatus status);
}
