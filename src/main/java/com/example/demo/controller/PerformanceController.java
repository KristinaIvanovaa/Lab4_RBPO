package com.example.demo.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.dto.PerformanceRequest;
import com.example.demo.dto.PerformanceResponse;
import com.example.demo.model.Performance;
import com.example.demo.service.PerformanceService;

@RestController
@RequestMapping("/api/performances")
public class PerformanceController {
    private final PerformanceService service;

    public PerformanceController(PerformanceService service) { this.service = service; }

    @GetMapping
    public List<PerformanceResponse> findAll() { return service.findAll().stream().map(this::toResponse).toList(); }

    @GetMapping("/{id}")
    public PerformanceResponse findById(@PathVariable Long id) { return toResponse(service.findById(id)); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PerformanceResponse> create(@Valid @RequestBody PerformanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(service.create(request)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PerformanceResponse update(@PathVariable Long id, @Valid @RequestBody PerformanceRequest request) {
        return toResponse(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private PerformanceResponse toResponse(Performance performance) {
        return new PerformanceResponse(performance.getId(), performance.getPlay().getId(),
                performance.getHall().getId(), performance.getStartsAt(),
                performance.getBasePrice(), performance.getStatus());
    }
}
