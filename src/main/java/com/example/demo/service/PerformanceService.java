package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.dto.PerformanceRequest;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Performance;
import com.example.demo.model.PerformanceStatus;
import com.example.demo.repository.PerformanceRepository;
import com.example.demo.repository.PlayRepository;
import com.example.demo.repository.TheatreHallRepository;
import com.example.demo.repository.TicketRepository;

@Service
@Transactional
public class PerformanceService {
    private final PerformanceRepository performances;
    private final PlayRepository plays;
    private final TheatreHallRepository halls;
    private final TicketRepository tickets;

    public PerformanceService(PerformanceRepository performances, PlayRepository plays,
                              TheatreHallRepository halls, TicketRepository tickets) {
        this.performances = performances;
        this.plays = plays;
        this.halls = halls;
        this.tickets = tickets;
    }

    @Transactional(readOnly = true)
    public List<Performance> findAll() { return performances.findAll(); }

    @Transactional(readOnly = true)
    public Performance findById(Long id) { return performances.findById(id).orElseThrow(() -> new ResourceNotFoundException("Performance", id)); }

    public Performance create(PerformanceRequest request) {
        require(request.playId() != null && request.hallId() != null
                && request.startsAt() != null && request.basePrice() != null,
                "playId, hallId, startsAt and basePrice are required");
        var hall = halls.findByIdForUpdate(request.hallId())
                .orElseThrow(() -> new ResourceNotFoundException("Theatre hall", request.hallId()));
        var play = plays.findById(request.playId())
                .orElseThrow(() -> new ResourceNotFoundException("Play", request.playId()));
        if (request.status() != null && request.status() != PerformanceStatus.SCHEDULED) {
            throw new IllegalArgumentException("New performances must be scheduled");
        }
        validateStartsAt(request.startsAt());
        validateHallAvailability(hall.getId(), play.getDurationMinutes(), request.startsAt(), null);
        return performances.save(new Performance(play, hall, request.startsAt(), request.basePrice(),
                PerformanceStatus.SCHEDULED));
    }

    public Performance update(Long id, PerformanceRequest request) {
        Performance performance = performances.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Performance", id));
        PerformanceStatus originalStatus = performance.getStatus();
        if (request.playId() != null) {
            performance.setPlay(plays.findById(request.playId())
                    .orElseThrow(() -> new ResourceNotFoundException("Play", request.playId())));
        }
        Long hallId = request.hallId() == null ? performance.getHall().getId() : request.hallId();
        var hall = halls.findByIdForUpdate(hallId)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre hall", hallId));
        if (request.hallId() != null) {
            performance.setHall(hall);
        }
        if (request.startsAt() != null) performance.setStartsAt(request.startsAt());
        if (request.basePrice() != null) performance.setBasePrice(request.basePrice());
        if (request.status() != null && request.status() != originalStatus) {
            throw new ConflictException("Change performance status through a business operation");
        }
        validateExistingSeats(performance);
        if (performance.getStatus() == PerformanceStatus.SCHEDULED) {
            validateSchedule(performance);
        }
        return performances.save(performance);
    }

    public void delete(Long id) {
        findById(id);
        tickets.deleteAll(tickets.findByPerformanceId(id));
        performances.deleteById(id);
    }

    private void validateExistingSeats(Performance performance) {
        if (tickets.findByPerformanceId(performance.getId()).stream()
                .anyMatch(ticket -> ticket.getSeatNumber() > performance.getHall().getSeatCount())) {
            throw new IllegalArgumentException("The selected hall has fewer seats than existing tickets");
        }
    }

    private void validateStartsAt(LocalDateTime startsAt) {
        if (startsAt == null || !startsAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("A performance must start in the future");
        }
    }

    private void validateHallAvailability(Long hallId, int durationMinutes,
                                          LocalDateTime startsAt, Long excludedId) {
        List<Performance> scheduled = excludedId == null
                ? performances.findByHallIdAndStatus(hallId, PerformanceStatus.SCHEDULED)
                : performances.findByHallIdAndStatusAndIdNot(hallId, PerformanceStatus.SCHEDULED, excludedId);
        LocalDateTime endsAt = startsAt.plusMinutes(durationMinutes);
        boolean overlaps = scheduled.stream().anyMatch(other -> {
            LocalDateTime otherEndsAt = other.getStartsAt().plusMinutes(other.getPlay().getDurationMinutes());
            return startsAt.isBefore(otherEndsAt) && other.getStartsAt().isBefore(endsAt);
        });
        if (overlaps) {
            throw new ConflictException("The theatre hall already has a performance at that time");
        }
    }

    public void validateSchedule(Performance performance) {
        validateStartsAt(performance.getStartsAt());
        validateHallAvailability(performance.getHall().getId(),
                performance.getPlay().getDurationMinutes(), performance.getStartsAt(), performance.getId());
    }

    private void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
