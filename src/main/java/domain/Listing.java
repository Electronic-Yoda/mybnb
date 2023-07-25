package domain;

import java.math.BigDecimal;

// Domain record for the 'listings' table
public record Listing(Long listingId, String listingType, BigDecimal pricePerNight, BigDecimal longitude,
                      String postalCode, BigDecimal latitude, String city,
                      String country, String amenities, User user) {}
