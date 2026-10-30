package com.example.demo.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.example.demo.repository.TicketRepository;
import com.example.demo.repository.UserRepository;

@Component("ticketAuthorization")
public class TicketAuthorization {
    private final TicketRepository tickets;
    private final UserRepository users;

    public TicketAuthorization(TicketRepository tickets, UserRepository users) {
        this.tickets = tickets;
        this.users = users;
    }

    public boolean ownsAccount(Authentication authentication, Long userId) {
        return users.findByEmailIgnoreCase(authentication.getName())
                .map(user -> user.getId().equals(userId))
                .orElse(false);
    }

    public boolean ownsTicket(Authentication authentication, Long ticketId) {
        return tickets.existsByIdAndUser_EmailIgnoreCase(ticketId, authentication.getName());
    }
}
