package filter;

import domain.Availability;
import domain.Listing;

import java.util.List;

public record ListingFilter(Listing listing, Availability availability, List<String> amenities) {
}
