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
import com.example.demo.dto.TheatreHallRequest;
import com.example.demo.dto.TheatreHallResponse;
import com.example.demo.model.TheatreHall;
import com.example.demo.service.TheatreHallService;

@RestController
@RequestMapping("/api/theatre-halls")
public class TheatreHallController {
    private final TheatreHallService service;

    public TheatreHallController(TheatreHallService service) { this.service = service; }

    @GetMapping
    public List<TheatreHallResponse> findAll() { return service.findAll().stream().map(this::toResponse).toList(); }

    @GetMapping("/{id}")
    public TheatreHallResponse findById(@PathVariable Long id) { return toResponse(service.findById(id)); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TheatreHallResponse> create(@Valid @RequestBody TheatreHallRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(service.create(request)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TheatreHallResponse update(@PathVariable Long id, @Valid @RequestBody TheatreHallRequest request) {
        return toResponse(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private TheatreHallResponse toResponse(TheatreHall hall) {
        return new TheatreHallResponse(hall.getId(), hall.getName(), hall.getSeatCount());
    }
}
