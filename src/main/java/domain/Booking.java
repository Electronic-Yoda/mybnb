package domain;

import java.math.BigDecimal;
import java.time.LocalDate;

// Domain record for the 'rentings' table
public record Booking(long booking_id, LocalDate start_date, LocalDate end_date, LocalDate transaction_date,
                      BigDecimal amount, String currency, String payment_method, long users_sin, long listings_listing_id) {}

