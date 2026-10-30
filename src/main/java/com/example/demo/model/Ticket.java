package com.example.demo.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "performance_id", nullable = false)
    private Performance performance;
    @Column(name = "seat_number", nullable = false)
    private int seatNumber;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;
    @Column(name = "price_paid", precision = 10, scale = 2)
    private BigDecimal pricePaid;

    protected Ticket() {
    }

    public Ticket(Performance performance, int seatNumber, User user, TicketStatus status) {
        this.performance = performance;
        this.seatNumber = seatNumber;
        this.user = user;
        this.status = status;
    }

    public Long getId() { return id; }
    public Performance getPerformance() { return performance; }
    public void setPerformance(Performance performance) { this.performance = performance; }
    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public TicketStatus getStatus() { return status; }
    public BigDecimal getPricePaid() { return pricePaid; }

    public void purchase(BigDecimal price) {
        if (status != TicketStatus.RESERVED) {
            throw new IllegalStateException("Only a reserved ticket can be purchased");
        }
        status = TicketStatus.PURCHASED;
        pricePaid = price;
    }

    public void cancelReservation() {
        if (status != TicketStatus.RESERVED) {
            throw new IllegalStateException("Only a reserved ticket can be cancelled");
        }
        status = TicketStatus.CANCELLED;
    }

    public void refund() {
        if (status != TicketStatus.PURCHASED) {
            throw new IllegalStateException("Only a purchased ticket can be refunded");
        }
        status = TicketStatus.REFUNDED;
    }

    public void cancelForPerformance() {
        if (status == TicketStatus.RESERVED) {
            status = TicketStatus.CANCELLED;
        } else if (status == TicketStatus.PURCHASED) {
            status = TicketStatus.REFUNDED;
        }
    }
}
