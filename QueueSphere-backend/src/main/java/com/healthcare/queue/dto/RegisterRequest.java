package com.healthcare.queue.dto;

import com.healthcare.queue.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String fullName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank
    private String phoneNumber;

    @NotNull
    private Role role; // PATIENT or DOCTOR

    // Optional, required only when role == DOCTOR
    private String specialization;
    private String department;
    private String qualifications;
}
