package com.shiftscheduler.repository;

import com.shiftscheduler.model.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    List<ShiftAssignment> findByUserId(Long userId);
    List<ShiftAssignment> findByShiftId(Long shiftId);
    Optional<ShiftAssignment> findByUserIdAndShiftId(Long userId, Long shiftId);
    int countByShiftIdAndAcceptedTrue(Long shiftId);
}

