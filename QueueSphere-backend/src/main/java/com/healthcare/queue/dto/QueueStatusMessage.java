package com.healthcare.queue.dto;

import com.healthcare.queue.model.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusMessage {
    private Long doctorId;
    private String doctorName;
    private Integer nowServingNumber; // queue number currently in progress
    private int totalWaiting;
    private int estimatedWaitMinutes; // for a specific patient, if applicable
    private List<QueueEntryDto> entries;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueueEntryDto {
        private Long appointmentId;
        private int queueNumber;
        private String patientName;
        private AppointmentStatus status;
    }
}
