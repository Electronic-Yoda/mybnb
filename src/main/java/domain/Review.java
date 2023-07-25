package domain;

// Domain record for the 'reviews' table
public record Review(long reviewId, Integer ratingOfListing, Integer ratingOfHost, Integer ratingOfRenter,
                     String commentFromRenter, String commentFromHost, Renting renting) {}
