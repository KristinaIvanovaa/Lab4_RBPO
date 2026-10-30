package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.dto.TheatreHallRequest;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.TheatreHall;
import com.example.demo.repository.PerformanceRepository;
import com.example.demo.repository.TheatreHallRepository;
import com.example.demo.repository.TicketRepository;

@Service
@Transactional
public class TheatreHallService {
    private final TheatreHallRepository halls;
    private final PerformanceRepository performances;
    private final TicketRepository tickets;

    public TheatreHallService(TheatreHallRepository halls, PerformanceRepository performances, TicketRepository tickets) {
        this.halls = halls;
        this.performances = performances;
        this.tickets = tickets;
    }

    @Transactional(readOnly = true)
    public List<TheatreHall> findAll() { return halls.findAll(); }

    @Transactional(readOnly = true)
    public TheatreHall findById(Long id) { return halls.findById(id).orElseThrow(() -> new ResourceNotFoundException("Theatre hall", id)); }

    public TheatreHall create(TheatreHallRequest request) {
        require(request.name() != null && request.seatCount() != null, "name and seatCount are required");
        return halls.save(new TheatreHall(request.name(), request.seatCount()));
    }

    public TheatreHall update(Long id, TheatreHallRequest request) {
        TheatreHall hall = halls.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre hall", id));
        if (request.name() != null) hall.setName(request.name());
        if (request.seatCount() != null) {
            boolean invalidatesTickets = performances.findByHallId(id).stream()
                    .flatMap(performance -> tickets.findByPerformanceId(performance.getId()).stream())
                    .anyMatch(ticket -> ticket.getSeatNumber() > request.seatCount());
            if (invalidatesTickets) {
                throw new IllegalArgumentException("seatCount cannot be lower than a seat number already assigned to a ticket");
            }
            hall.setSeatCount(request.seatCount());
        }
        return halls.save(hall);
    }

    public void delete(Long id) {
        halls.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre hall", id));
        performances.findByHallId(id).forEach(performance -> {
            tickets.deleteAll(tickets.findByPerformanceId(performance.getId()));
            performances.delete(performance);
        });
        halls.deleteById(id);
    }

    private void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
