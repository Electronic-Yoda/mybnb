package domain;

import java.awt.geom.Point2D;

// Domain record for the 'listings' table
public record Listing(Long listing_id, String listing_type,
                      String address, String postal_code, Point2D location,
                      String city, String country, Long users_sin) {}

