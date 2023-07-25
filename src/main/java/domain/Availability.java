package domain;

import java.time.LocalDate;

// Domain record for the 'availabilities' table
public record Availability(Long availabilityId, LocalDate startDate, LocalDate endDate, Listing listing) {}

