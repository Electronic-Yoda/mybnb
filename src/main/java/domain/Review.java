package domain;

// Domain record for the 'reviews' table
public record Review(long review_id, Integer rating_of_listing, Integer rating_of_host, Integer rating_of_renter,
                     String comment_from_renter, String comment_from_host, long bookings_booking_id) {}
