package com.example.demo.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.example.demo.dto.RescheduleRequest;
import com.example.demo.dto.PerformanceResponse;
import com.example.demo.dto.TicketResponse;
import com.example.demo.model.Performance;
import com.example.demo.model.Ticket;
import com.example.demo.service.TheatreOperationService;

@RestController
@RequestMapping("/api/operations")
public class TheatreOperationController {
    private final TheatreOperationService operations;

    public TheatreOperationController(TheatreOperationService operations) {
        this.operations = operations;
    }

    @PostMapping("/tickets/{id}/purchase")
    @PreAuthorize("hasRole('ADMIN') or @ticketAuthorization.ownsTicket(authentication, #p0)")
    public TicketResponse purchaseTicket(@PathVariable Long id) {
        return toTicketResponse(operations.purchaseTicket(id));
    }

    @PostMapping("/tickets/{id}/cancel-reservation")
    @PreAuthorize("hasRole('ADMIN') or @ticketAuthorization.ownsTicket(authentication, #p0)")
    public TicketResponse cancelReservation(@PathVariable Long id) {
        return toTicketResponse(operations.cancelReservation(id));
    }

    @PostMapping("/tickets/{id}/refund")
    @PreAuthorize("hasRole('ADMIN') or @ticketAuthorization.ownsTicket(authentication, #p0)")
    public TicketResponse refundTicket(@PathVariable Long id) {
        return toTicketResponse(operations.refundTicket(id));
    }

    @PostMapping("/performances/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public PerformanceResponse cancelPerformance(@PathVariable Long id) {
        return toPerformanceResponse(operations.cancelPerformance(id));
    }

    @PatchMapping("/performances/{id}/reschedule")
    @PreAuthorize("hasRole('ADMIN')")
    public PerformanceResponse reschedulePerformance(
            @PathVariable Long id, @Valid @RequestBody RescheduleRequest request) {
        return toPerformanceResponse(operations.reschedulePerformance(id, request));
    }

    private TicketResponse toTicketResponse(Ticket ticket) {
        return new TicketResponse(ticket.getId(), ticket.getPerformance().getId(), ticket.getSeatNumber(),
                ticket.getUser().getId(), ticket.getStatus(), ticket.getPricePaid());
    }

    private PerformanceResponse toPerformanceResponse(Performance performance) {
        return new PerformanceResponse(performance.getId(), performance.getPlay().getId(),
                performance.getHall().getId(), performance.getStartsAt(),
                performance.getBasePrice(), performance.getStatus());
    }
}
