package com.example.demo.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.RescheduleRequest;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Performance;
import com.example.demo.model.PerformanceStatus;
import com.example.demo.model.Ticket;
import com.example.demo.model.TicketStatus;
import com.example.demo.repository.PerformanceRepository;
import com.example.demo.repository.TicketRepository;
import com.example.demo.repository.TheatreHallRepository;

@Service
@Transactional
public class TheatreOperationService {
    private final TicketRepository tickets;
    private final PerformanceRepository performances;
    private final TheatreHallRepository halls;
    private final PerformanceService performanceService;

    public TheatreOperationService(
            TicketRepository tickets,
            PerformanceRepository performances,
            TheatreHallRepository halls,
            PerformanceService performanceService) {
        this.tickets = tickets;
        this.performances = performances;
        this.halls = halls;
        this.performanceService = performanceService;
    }

    public Ticket purchaseTicket(Long ticketId) {
        Ticket initial = ticket(ticketId);
        Performance performance = performanceForUpdate(initial.getPerformance().getId());
        Ticket ticket = ticketForUpdate(ticketId);
        requireUpcoming(performance);
        if (ticket.getStatus() != TicketStatus.RESERVED) {
            throw new ConflictException("Only a reserved ticket can be purchased");
        }
        ticket.purchase(performance.getBasePrice());
        return ticket;
    }

    public Ticket cancelReservation(Long ticketId) {
        Ticket initial = ticket(ticketId);
        Performance performance = performanceForUpdate(initial.getPerformance().getId());
        Ticket ticket = ticketForUpdate(ticketId);
        requireUpcoming(performance);
        if (ticket.getStatus() != TicketStatus.RESERVED) {
            throw new ConflictException("Only a reserved ticket can be cancelled");
        }
        ticket.cancelReservation();
        return ticket;
    }

    public Ticket refundTicket(Long ticketId) {
        Ticket initial = ticket(ticketId);
        Performance performance = performanceForUpdate(initial.getPerformance().getId());
        Ticket ticket = ticketForUpdate(ticketId);
        requireUpcoming(performance);
        if (ticket.getStatus() != TicketStatus.PURCHASED) {
            throw new ConflictException("Only a purchased ticket can be refunded");
        }
        ticket.refund();
        return ticket;
    }

    public Performance cancelPerformance(Long performanceId) {
        Performance performance = performanceForUpdate(performanceId);
        requireUpcoming(performance);
        performance.setStatus(PerformanceStatus.CANCELLED);
        tickets.findByPerformanceId(performanceId).forEach(Ticket::cancelForPerformance);
        return performance;
    }

    public Performance reschedulePerformance(Long performanceId, RescheduleRequest request) {
        if (request == null || request.startsAt() == null
                || !request.startsAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("The new performance time must be in the future");
        }
        Performance performance = performanceForUpdate(performanceId);
        requireUpcoming(performance);
        halls.findByIdForUpdate(performance.getHall().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Theatre hall", performance.getHall().getId()));
        performance.setStartsAt(request.startsAt());
        performanceService.validateSchedule(performance);
        return performance;
    }

    private void requireUpcoming(Performance performance) {
        if (performance.getStatus() != PerformanceStatus.SCHEDULED
                || !performance.getStartsAt().isAfter(LocalDateTime.now())) {
            throw new ConflictException("The operation is only available for upcoming scheduled performances");
        }
    }

    private Ticket ticket(Long id) {
        return tickets.findById(id).orElseThrow(() -> new ResourceNotFoundException("Ticket", id));
    }

    private Ticket ticketForUpdate(Long id) {
        return tickets.findByIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Ticket", id));
    }

    private Performance performanceForUpdate(Long id) {
        return performances.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Performance", id));
    }
}
