package domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CancelledBooking(Long cancelled_booking_id, LocalDate start_date, LocalDate end_date, LocalDate transaction_date,
                               BigDecimal amount, String payment_method, Long card_number, Long tenant_sin, Long listings_listing_id) {}


