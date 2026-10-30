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
import com.example.demo.dto.PlayRequest;
import com.example.demo.dto.PlayResponse;
import com.example.demo.model.Play;
import com.example.demo.service.PlayService;

@RestController
@RequestMapping("/api/plays")
public class PlayController {
    private final PlayService service;

    public PlayController(PlayService service) { this.service = service; }

    @GetMapping
    public List<PlayResponse> findAll() { return service.findAll().stream().map(this::toResponse).toList(); }

    @GetMapping("/{id}")
    public PlayResponse findById(@PathVariable Long id) { return toResponse(service.findById(id)); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlayResponse> create(@Valid @RequestBody PlayRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(service.create(request)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PlayResponse update(@PathVariable Long id, @Valid @RequestBody PlayRequest request) {
        return toResponse(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private PlayResponse toResponse(Play play) {
        return new PlayResponse(play.getId(), play.getTitle(), play.getDurationMinutes(), play.getAgeRestriction());
    }
}
