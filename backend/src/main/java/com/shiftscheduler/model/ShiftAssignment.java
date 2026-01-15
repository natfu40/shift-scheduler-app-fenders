package com.shiftscheduler.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "shift_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @Column(nullable = false)
    private boolean accepted = false;

    @Column(name = "signed_up_at", nullable = false, updatable = false)
    private Instant signedUpAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @PrePersist
    protected void onCreate() {
        signedUpAt = Instant.now();
    }
}

