package com.shiftscheduler.repository;

import com.shiftscheduler.model.Shift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    Page<Shift> findByActiveTrue(Pageable pageable);
    Page<Shift> findByActiveTrueAndStartTimeAfter(LocalDateTime startTime, Pageable pageable);
    List<Shift> findByCreatedById(Long userId);
}

