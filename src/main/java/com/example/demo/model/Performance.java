package com.example.demo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "performances")
public class Performance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "play_id", nullable = false)
    private Play play;
    @ManyToOne(optional = false)
    @JoinColumn(name = "hall_id", nullable = false)
    private TheatreHall hall;
    @Column(nullable = false)
    private LocalDateTime startsAt;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerformanceStatus status;

    protected Performance() {
    }

    public Performance(Play play, TheatreHall hall, LocalDateTime startsAt,
                       BigDecimal basePrice, PerformanceStatus status) {
        this.play = play;
        this.hall = hall;
        this.startsAt = startsAt;
        this.basePrice = basePrice;
        this.status = status;
    }

    public Long getId() { return id; }
    public Play getPlay() { return play; }
    public void setPlay(Play play) { this.play = play; }
    public TheatreHall getHall() { return hall; }
    public void setHall(TheatreHall hall) { this.hall = hall; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public PerformanceStatus getStatus() { return status; }
    public void setStatus(PerformanceStatus status) { this.status = status; }
}
