package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.demo.model.TheatreHall;
import jakarta.persistence.LockModeType;

public interface TheatreHallRepository extends JpaRepository<TheatreHall, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from TheatreHall h where h.id = :id")
    Optional<TheatreHall> findByIdForUpdate(@Param("id") Long id);
}
