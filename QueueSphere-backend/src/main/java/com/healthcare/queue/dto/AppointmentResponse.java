package com.healthcare.queue.dto;

import com.healthcare.queue.model.Appointment;
import com.healthcare.queue.model.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String specialization;
    private LocalDateTime scheduledTime;
    private AppointmentStatus status;
    private String reasonForVisit;
    private Integer queuePosition; // null if not checked in

    public static AppointmentResponse from(Appointment a) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .patientId(a.getPatient().getId())
                .patientName(a.getPatient().getUser().getFullName())
                .doctorId(a.getDoctor().getId())
                .doctorName(a.getDoctor().getUser().getFullName())
                .specialization(a.getDoctor().getSpecialization())
                .scheduledTime(a.getScheduledTime())
                .status(a.getStatus())
                .reasonForVisit(a.getReasonForVisit())
                .build();
    }
}
