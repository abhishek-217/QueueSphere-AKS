package com.healthcare.queue.controller;

import com.healthcare.queue.dto.QueueStatusMessage;
import com.healthcare.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    /** Public: anyone (patients in the waiting room) can view live queue status for a doctor. */
    @GetMapping("/public/{doctorId}")
    public ResponseEntity<QueueStatusMessage> getQueueStatus(@PathVariable Long doctorId) {
        return ResponseEntity.ok(queueService.getQueueStatus(doctorId));
    }

    /** Doctor/Admin: call the next patient in queue. */
    @PostMapping("/doctor/{doctorId}/call-next")
    public ResponseEntity<QueueStatusMessage> callNext(@PathVariable Long doctorId) {
        return ResponseEntity.ok(queueService.callNext(doctorId));
    }

    /** Doctor/Admin: mark a queued patient as a no-show. */
    @PostMapping("/entry/{queueEntryId}/no-show")
    public ResponseEntity<QueueStatusMessage> markNoShow(@PathVariable Long queueEntryId) {
        return ResponseEntity.ok(queueService.markNoShow(queueEntryId));
    }
}
