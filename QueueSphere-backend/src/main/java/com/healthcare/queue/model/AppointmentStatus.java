package com.healthcare.queue.model;

public enum AppointmentStatus {
    BOOKED,       // patient booked, hasn't checked in yet
    CHECKED_IN,   // patient checked in, waiting in queue
    IN_PROGRESS,  // currently being seen by doctor
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
