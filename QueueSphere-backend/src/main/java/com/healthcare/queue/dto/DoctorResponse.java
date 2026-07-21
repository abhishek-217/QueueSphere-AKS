package com.healthcare.queue.dto;

import com.healthcare.queue.model.Doctor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {
    private Long id;
    private String fullName;
    private String specialization;
    private String department;
    private String qualifications;
    private int avgConsultationMinutes;

    public static DoctorResponse from(Doctor d) {
        return DoctorResponse.builder()
                .id(d.getId())
                .fullName(d.getUser().getFullName())
                .specialization(d.getSpecialization())
                .department(d.getDepartment())
                .qualifications(d.getQualifications())
                .avgConsultationMinutes(d.getAvgConsultationMinutes())
                .build();
    }
}
