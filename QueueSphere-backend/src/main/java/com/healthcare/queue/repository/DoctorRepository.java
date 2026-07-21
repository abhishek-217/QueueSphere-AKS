package com.healthcare.queue.repository;

import com.healthcare.queue.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    Optional<Doctor> findByUserEmail(String email);
    List<Doctor> findBySpecializationIgnoreCaseAndActiveTrue(String specialization);
    List<Doctor> findByActiveTrue();
}
