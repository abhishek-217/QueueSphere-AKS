package com.healthcare.queue.service;

import com.healthcare.queue.dto.QueueStatusMessage;
import com.healthcare.queue.exception.BadRequestException;
import com.healthcare.queue.exception.ResourceNotFoundException;
import com.healthcare.queue.model.*;
import com.healthcare.queue.repository.AppointmentRepository;
import com.healthcare.queue.repository.DoctorRepository;
import com.healthcare.queue.repository.QueueEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueEntryRepository queueEntryRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Patient checks in for their appointment -> joins today's live queue for that doctor.
     */
    @Transactional
    public QueueStatusMessage checkIn(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new BadRequestException("Only booked appointments can be checked in");
        }

        LocalDate today = LocalDate.now();
        int currentCount = queueEntryRepository.countByDoctorIdAndQueueDate(appointment.getDoctor().getId(), today);

        QueueEntry entry = QueueEntry.builder()
                .appointment(appointment)
                .doctor(appointment.getDoctor())
                .queueDate(today)
                .queueNumber(currentCount + 1)
                .status(AppointmentStatus.CHECKED_IN)
                .build();
        queueEntryRepository.save(entry);

        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        appointment.setCheckedInAt(java.time.LocalDateTime.now());
        appointmentRepository.save(appointment);

        return broadcastQueueStatus(appointment.getDoctor().getId());
    }

    /**
     * Doctor calls the next patient in queue: completes current in-progress entry (if any)
     * and promotes the next checked-in patient to IN_PROGRESS.
     */
    @Transactional
    public QueueStatusMessage callNext(Long doctorId) {
        LocalDate today = LocalDate.now();

        queueEntryRepository.findFirstByDoctorIdAndQueueDateAndStatusOrderByQueueNumberAsc(
                        doctorId, today, AppointmentStatus.IN_PROGRESS)
                .ifPresent(current -> {
                    current.setStatus(AppointmentStatus.COMPLETED);
                    queueEntryRepository.save(current);
                    Appointment apt = current.getAppointment();
                    apt.setStatus(AppointmentStatus.COMPLETED);
                    apt.setConsultationEndedAt(java.time.LocalDateTime.now());
                    appointmentRepository.save(apt);
                });

        queueEntryRepository.findFirstByDoctorIdAndQueueDateAndStatusOrderByQueueNumberAsc(
                        doctorId, today, AppointmentStatus.CHECKED_IN)
                .ifPresent(next -> {
                    next.setStatus(AppointmentStatus.IN_PROGRESS);
                    queueEntryRepository.save(next);
                    Appointment apt = next.getAppointment();
                    apt.setStatus(AppointmentStatus.IN_PROGRESS);
                    apt.setConsultationStartedAt(java.time.LocalDateTime.now());
                    appointmentRepository.save(apt);
                });

        return broadcastQueueStatus(doctorId);
    }

    @Transactional
    public QueueStatusMessage markNoShow(Long queueEntryId) {
        QueueEntry entry = queueEntryRepository.findById(queueEntryId)
                .orElseThrow(() -> new ResourceNotFoundException("Queue entry not found"));
        entry.setStatus(AppointmentStatus.NO_SHOW);
        queueEntryRepository.save(entry);

        Appointment apt = entry.getAppointment();
        apt.setStatus(AppointmentStatus.NO_SHOW);
        appointmentRepository.save(apt);

        return broadcastQueueStatus(entry.getDoctor().getId());
    }

    public QueueStatusMessage getQueueStatus(Long doctorId) {
        return buildQueueStatus(doctorId);
    }

    /**
     * Builds the current queue snapshot and pushes it to all subscribers of /topic/queue/{doctorId}.
     */
    private QueueStatusMessage broadcastQueueStatus(Long doctorId) {
        QueueStatusMessage message = buildQueueStatus(doctorId);
        messagingTemplate.convertAndSend("/topic/queue/" + doctorId, message);
        return message;
    }

    private QueueStatusMessage buildQueueStatus(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        LocalDate today = LocalDate.now();
        List<QueueEntry> activeEntries = queueEntryRepository.findByDoctorIdAndQueueDateAndStatusInOrderByQueueNumberAsc(
                doctorId, today, List.of(AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_PROGRESS));

        Integer nowServing = activeEntries.stream()
                .filter(e -> e.getStatus() == AppointmentStatus.IN_PROGRESS)
                .map(QueueEntry::getQueueNumber)
                .findFirst()
                .orElse(null);

        long waitingCount = activeEntries.stream()
                .filter(e -> e.getStatus() == AppointmentStatus.CHECKED_IN)
                .count();

        List<QueueStatusMessage.QueueEntryDto> entryDtos = activeEntries.stream()
                .map(e -> QueueStatusMessage.QueueEntryDto.builder()
                        .appointmentId(e.getAppointment().getId())
                        .queueNumber(e.getQueueNumber())
                        .patientName(e.getAppointment().getPatient().getUser().getFullName())
                        .status(e.getStatus())
                        .build())
                .toList();

        return QueueStatusMessage.builder()
                .doctorId(doctorId)
                .doctorName(doctor.getUser().getFullName())
                .nowServingNumber(nowServing)
                .totalWaiting((int) waitingCount)
                .estimatedWaitMinutes((int) waitingCount * doctor.getAvgConsultationMinutes())
                .entries(entryDtos)
                .updatedAt(java.time.LocalDateTime.now())
                .build();
    }
}
