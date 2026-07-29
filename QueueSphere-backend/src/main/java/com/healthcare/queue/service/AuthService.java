package com.healthcare.queue.service;

import com.healthcare.queue.dto.AuthResponse;
import com.healthcare.queue.dto.LoginRequest;
import com.healthcare.queue.dto.RegisterRequest;
import com.healthcare.queue.exception.BadRequestException;
import com.healthcare.queue.model.Doctor;
import com.healthcare.queue.model.Patient;
import com.healthcare.queue.model.Role;
import com.healthcare.queue.model.User;
import com.healthcare.queue.repository.DoctorRepository;
import com.healthcare.queue.repository.PatientRepository;
import com.healthcare.queue.repository.UserRepository;
import com.healthcare.queue.security.CustomUserDetails;
import com.healthcare.queue.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists");
        }
        if (request.getRole() == Role.ADMIN) {
            throw new BadRequestException("Cannot self-register as ADMIN");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .build();
        user = userRepository.save(user);

        Long savedDoctorId = null;

        if (request.getRole() == Role.PATIENT) {
            Patient patient = Patient.builder().user(user).build();
            patientRepository.save(patient);
        } else if (request.getRole() == Role.DOCTOR) {
            if (request.getSpecialization() == null || request.getDepartment() == null) {
                throw new BadRequestException("specialization and department are required for doctor registration");
            }
            Doctor doctor = Doctor.builder()
                    .user(user)
                    .specialization(request.getSpecialization())
                    .department(request.getDepartment())
                    .qualifications(request.getQualifications())
                    .build();
            doctor = doctorRepository.save(doctor);
            savedDoctorId = doctor.getId();
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .doctorId(savedDoctorId)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        Long doctorId = null;
        if (user.getRole() == Role.DOCTOR) {
            doctorId = doctorRepository.findByUserId(user.getId())
                    .map(Doctor::getId)
                    .orElse(null);
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .doctorId(doctorId)
                .build();
    }
}
