package com.example.demo.service;

import java.util.List;
import java.util.Collection;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.dto.TicketRequest;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Ticket;
import com.example.demo.model.TicketStatus;
import com.example.demo.model.PerformanceStatus;
import com.example.demo.model.Performance;
import com.example.demo.repository.PerformanceRepository;
import com.example.demo.repository.TicketRepository;
import com.example.demo.repository.TheatreHallRepository;
import com.example.demo.repository.UserRepository;

@Service
@Transactional
public class TicketService {
    private final TicketRepository tickets;
    private final PerformanceRepository performances;
    private final TheatreHallRepository halls;
    private final UserRepository users;

    public TicketService(TicketRepository tickets, PerformanceRepository performances,
                         TheatreHallRepository halls, UserRepository users) {
        this.tickets = tickets;
        this.performances = performances;
        this.halls = halls;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<Ticket> findAll() { return tickets.findAll(); }

    @Transactional(readOnly = true)
    public Ticket findById(Long id) { return tickets.findById(id).orElseThrow(() -> new ResourceNotFoundException("Ticket", id)); }

    public Ticket create(TicketRequest request) {
        require(request.performanceId() != null && request.seatNumber() != null && request.userId() != null,
                "performanceId, seatNumber and userId are required");
        var performance = performances.findByIdForUpdate(request.performanceId())
                .orElseThrow(() -> new ResourceNotFoundException("Performance", request.performanceId()));
        var hall = halls.findByIdForUpdate(performance.getHall().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Theatre hall", performance.getHall().getId()));
        var user = users.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));
        requireUpcoming(performance);
        validateSeat(hall.getSeatCount(), request.seatNumber());
        if (tickets.existsByPerformanceIdAndSeatNumberAndStatusIn(
                performance.getId(), request.seatNumber(), activeStatuses())) {
            throw new ConflictException("The seat is already reserved or sold for this performance");
        }
        return tickets.save(new Ticket(performance, request.seatNumber(), user, TicketStatus.RESERVED));
    }

    public Ticket update(Long id, TicketRequest request) {
        Ticket initial = findById(id);
        if (request.performanceId() != null
                && !request.performanceId().equals(initial.getPerformance().getId())) {
            throw new ConflictException("A ticket cannot be moved to another performance");
        }
        var performance = performances.findByIdForUpdate(initial.getPerformance().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Performance", initial.getPerformance().getId()));
        var hall = halls.findByIdForUpdate(performance.getHall().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Theatre hall", performance.getHall().getId()));
        Ticket ticket = tickets.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));
        if (request.seatNumber() != null) ticket.setSeatNumber(request.seatNumber());
        if (request.userId() != null) {
            ticket.setUser(users.findById(request.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.userId())));
        }
        requireUpcoming(performance);
        validateSeat(hall.getSeatCount(), ticket.getSeatNumber());
        if (tickets.existsByPerformanceIdAndSeatNumberAndStatusInAndIdNot(
                ticket.getPerformance().getId(), ticket.getSeatNumber(), activeStatuses(), id)) {
            throw new ConflictException("The seat is already reserved or sold for this performance");
        }
        return tickets.save(ticket);
    }

    public void delete(Long id) {
        findById(id);
        tickets.deleteById(id);
    }

    private void validateSeat(int seatCount, int seatNumber) {
        if (seatNumber < 1 || seatNumber > seatCount) {
            throw new IllegalArgumentException("seatNumber must be between 1 and " + seatCount);
        }
    }

    private void requireUpcoming(Performance performance) {
        if (performance.getStatus() != PerformanceStatus.SCHEDULED
                || !performance.getStartsAt().isAfter(LocalDateTime.now())) {
            throw new ConflictException("Tickets can only be changed for upcoming scheduled performances");
        }
    }

    private void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }

    private Collection<TicketStatus> activeStatuses() {
        return List.of(TicketStatus.RESERVED, TicketStatus.PURCHASED);
    }
}
