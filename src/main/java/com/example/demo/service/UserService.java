package com.example.demo.service;

import java.util.List;
import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.dto.RegistrationRequest;
import com.example.demo.dto.UserRequest;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.TicketRepository;

@Service
@Transactional
public class UserService {
    private final UserRepository users;
    private final TicketRepository tickets;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, TicketRepository tickets, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.tickets = tickets;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() { return users.findAll(); }

    @Transactional(readOnly = true)
    public User findById(Long id) { return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id)); }

    public User register(RegistrationRequest request) {
        String email = request.email().trim().toLowerCase(java.util.Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must not exceed 72 UTF-8 bytes");
        }
        return users.save(new User(request.name().trim(), email,
                passwordEncoder.encode(request.password()), Role.CUSTOMER, true));
    }

    public User update(Long id, UserRequest request) {
        User user = findById(id);
        if (request.name() != null) user.setName(request.name());
        if (request.email() != null) {
            String email = request.email().trim().toLowerCase(java.util.Locale.ROOT);
            if (users.existsByEmailIgnoreCase(email) && !user.getEmail().equalsIgnoreCase(email)) {
                throw new ConflictException("An account with this email already exists");
            }
            user.setEmail(email);
        }
        if (request.enabled() != null) user.setEnabled(request.enabled());
        return users.save(user);
    }

    public void delete(Long id) {
        findById(id);
        tickets.deleteAll(tickets.findByUserId(id));
        users.deleteById(id);
    }
}
