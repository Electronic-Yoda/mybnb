package domain;

import java.math.BigDecimal;
import java.time.LocalDate;

// Domain record for the 'rentings' table
public record Renting(long rentingId, LocalDate startDate, LocalDate endDate, LocalDate transactionDate,
                      BigDecimal amount, String currency, String paymentMethod, User user, Listing listing) {}

