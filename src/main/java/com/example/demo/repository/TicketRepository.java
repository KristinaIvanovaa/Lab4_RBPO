package com.example.demo.repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.demo.model.Ticket;
import com.example.demo.model.TicketStatus;
import jakarta.persistence.LockModeType;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByPerformanceId(Long performanceId);
    List<Ticket> findByUserId(Long userId);
    boolean existsByIdAndUser_EmailIgnoreCase(Long id, String email);
    boolean existsByPerformanceIdAndSeatNumberAndStatusIn(
            Long performanceId, int seatNumber, Collection<TicketStatus> statuses);
    boolean existsByPerformanceIdAndSeatNumberAndStatusInAndIdNot(
            Long performanceId, int seatNumber, Collection<TicketStatus> statuses, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.id = :id")
    Optional<Ticket> findByIdForUpdate(@Param("id") Long id);
}
