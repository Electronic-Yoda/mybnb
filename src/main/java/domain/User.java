package domain;

import java.time.LocalDate;

// Domain record for the 'users' table
public record User(Long sin, String name, String address, LocalDate birthdate, String occupation) {}

