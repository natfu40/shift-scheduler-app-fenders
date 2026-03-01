package com.shiftscheduler.repository;

import com.shiftscheduler.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUserId(Long userId);
    void deleteByUserId(Long userId);

    @Query("SELECT COUNT(ur) > 0 FROM UserRole ur JOIN ur.role r WHERE ur.user.id = :userId AND r.name = 'ADMIN'")
    boolean isUserAdmin(@Param("userId") Long userId);
}

