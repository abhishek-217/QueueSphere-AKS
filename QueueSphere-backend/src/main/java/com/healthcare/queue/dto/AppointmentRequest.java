package com.healthcare.queue.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentRequest {

    @NotNull
    private Long doctorId;

    @NotNull
    @Future(message = "Appointment time must be in the future")
    private LocalDateTime scheduledTime;

    private String reasonForVisit;
}
