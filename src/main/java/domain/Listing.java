package domain;

import java.math.BigDecimal;

// Domain record for the 'listings' table
public record Listing(long listingId, String listingType, BigDecimal longitude,
                      String postalCode, BigDecimal latitude, String city,
                      String country, String amenities, User user) {}
