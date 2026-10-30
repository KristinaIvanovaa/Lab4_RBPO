package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.dto.PlayRequest;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Play;
import com.example.demo.model.PerformanceStatus;
import com.example.demo.repository.PerformanceRepository;
import com.example.demo.repository.PlayRepository;
import com.example.demo.repository.TicketRepository;

@Service
@Transactional
public class PlayService {
    private final PlayRepository plays;
    private final PerformanceRepository performances;
    private final TicketRepository tickets;

    public PlayService(PlayRepository plays, PerformanceRepository performances, TicketRepository tickets) {
        this.plays = plays;
        this.performances = performances;
        this.tickets = tickets;
    }

    @Transactional(readOnly = true)
    public List<Play> findAll() { return plays.findAll(); }

    @Transactional(readOnly = true)
    public Play findById(Long id) { return plays.findById(id).orElseThrow(() -> new ResourceNotFoundException("Play", id)); }

    public Play create(PlayRequest request) {
        require(request.title() != null && request.durationMinutes() != null && request.ageRestriction() != null,
                "title, durationMinutes and ageRestriction are required");
        return plays.save(new Play(request.title(), request.durationMinutes(), request.ageRestriction()));
    }

    public Play update(Long id, PlayRequest request) {
        Play play = findById(id);
        if (request.title() != null) play.setTitle(request.title());
        if (request.durationMinutes() != null && request.durationMinutes() != play.getDurationMinutes()) {
            if (performances.findByPlayId(id).stream()
                    .anyMatch(performance -> performance.getStatus() == PerformanceStatus.SCHEDULED)) {
                throw new ConflictException("Cannot change play duration while it has scheduled performances");
            }
            play.setDurationMinutes(request.durationMinutes());
        }
        if (request.ageRestriction() != null) play.setAgeRestriction(request.ageRestriction());
        return plays.save(play);
    }

    public void delete(Long id) {
        findById(id);
        performances.findByPlayId(id).forEach(performance -> {
            tickets.deleteAll(tickets.findByPerformanceId(performance.getId()));
            performances.delete(performance);
        });
        plays.deleteById(id);
    }

    private void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
