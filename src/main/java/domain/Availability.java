package domain;

import java.time.LocalDate;

// Domain record for the 'availabilities' table
public record Availability(long availability_id, LocalDate start_date, LocalDate end_date, long listings_listing_id) {}

