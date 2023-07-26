package domain;

import java.math.BigDecimal;

// Domain record for the 'listings' table
public record Listing(long listing_id, String listing_type, BigDecimal price_per_night,
                      String postal_code, BigDecimal longitude,
                      BigDecimal latitude, String city,
                      String country, String amenities, long users_sin) {}
