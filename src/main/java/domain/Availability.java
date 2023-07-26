package domain;

import java.time.LocalDate;

// Domain record for the 'availabilities' table
public record Availability(Long availability_id, LocalDate start_date, LocalDate end_date, Long listings_listing_id) {}

