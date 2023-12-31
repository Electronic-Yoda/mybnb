package domain;

// Domain record for the 'reviews' table
public record Review(Long review_id, Integer rating_of_listing,
                     Integer rating_of_host, Integer rating_of_tenant,
                     String comment_from_tenant, String comment_from_host, Long bookings_booking_id) {}
