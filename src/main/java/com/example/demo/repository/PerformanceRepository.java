package com.example.demo.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.demo.model.Performance;
import com.example.demo.model.PerformanceStatus;
import jakarta.persistence.LockModeType;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    List<Performance> findByPlayId(Long playId);
    List<Performance> findByHallId(Long hallId);
    List<Performance> findByHallIdAndStatus(Long hallId, PerformanceStatus status);
    List<Performance> findByHallIdAndStatusAndIdNot(Long hallId, PerformanceStatus status, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Performance p where p.id = :id")
    Optional<Performance> findByIdForUpdate(@Param("id") Long id);
}
