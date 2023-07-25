package domain;

import java.math.BigDecimal;

// Domain record for the 'listings' table
public record Listing(Long listing_id, String listing_type, BigDecimal price_per_night, BigDecimal longitude,
                      String postal_code, BigDecimal latitude, String city,
                      String country, String amenities, Long users_sin) {}
