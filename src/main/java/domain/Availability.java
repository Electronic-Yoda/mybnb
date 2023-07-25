package domain;

import java.time.LocalDate;

// Domain record for the 'availabilities' table
public record Availability(long availabilityId, LocalDate startDate, LocalDate endDate, Listing listing) {}

