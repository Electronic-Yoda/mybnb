package filter;

import domain.Amenity;
import domain.Availability;
import domain.Listing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ListingFilter {
    private Listing listing;
    private Availability availability;
    private List<String> amenities;

    // additional fields
    private List<String> listingTypes; // setting this overrides the listing type in the listing object
    private BigDecimal searchRadius; // setting this searches the radius around the longitude and latitude in the listing object
    private BigDecimal minPricePerNight; // setting this overrides the price per night in the availability object
    private BigDecimal maxPricePerNight; // setting this overrides the price per night in the availability object
    private LocalDate startDateRange; // setting this overrides the start date in the availability object
    private LocalDate endDateRange; // setting this overrides the end date in the availability object
    private boolean groupByPriceAscend = false;
    private boolean groupByPriceDescend = false;

    public ListingFilter(Listing listing, Availability availability, List<String> amenities,
                         List<String> listingTypes, BigDecimal searchRadius, BigDecimal minPricePerNight,
                         BigDecimal maxPricePerNight, LocalDate startDateRange, LocalDate endDateRange,
                         boolean groupByPriceAscend, boolean groupByPriceDescend) {
        this.listing = listing;
        this.availability = availability;
        this.amenities = amenities;
        this.listingTypes = listingTypes;
        this.searchRadius = searchRadius;
        this.minPricePerNight = minPricePerNight;
        this.maxPricePerNight = maxPricePerNight;
        this.startDateRange = startDateRange;
        this.endDateRange = endDateRange;
        this.groupByPriceAscend = groupByPriceAscend;
        this.groupByPriceDescend = groupByPriceDescend;
    }

    public Listing listing() {
        return listing;
    }

    public Availability availability() {
        return availability;
    }

    public List<String> amenities() {
        return amenities;
    }

    public List<String> listingTypes() {
        return listingTypes;
    }

    public BigDecimal searchRadius() {
        return searchRadius;
    }

    public BigDecimal minPricePerNight() {
        return minPricePerNight;
    }

    public BigDecimal maxPricePerNight() {
        return maxPricePerNight;
    }

    public LocalDate startDateRange() {
        return startDateRange;
    }

    public LocalDate endDateRange() {
        return endDateRange;
    }

    public boolean groupByPriceAscend() {
        return groupByPriceAscend;
    }

    public boolean groupByPriceDescend() {
        return groupByPriceDescend;
    }

    public void updateListing(Listing listing) {
        this.listing = listing;
    }


    public static class Builder {
        private Listing listing;
        private Availability availability;
        private List<String> amenities;

        // additional fields
        private List<String> listingTypes;
        private BigDecimal searchRadius;
        private BigDecimal minPricePerNight;
        private BigDecimal maxPricePerNight;
        private LocalDate startDateRange;
        private LocalDate endDateRange;

        private boolean groupByPriceAscend = false;

        private boolean groupByPriceDescend = false;

        public Builder withListing(Listing listing) {
            this.listing = listing;
            return this;
        }

        public Builder withAvailability(Availability availability) {
            this.availability = availability;
            return this;
        }

        public Builder withAmenities(List<String> amenities) {
            this.amenities = amenities;
            return this;
        }

        // setting this overrides the listing type in the listing object
        public Builder withListingTypes(List<String> listingTypes) {
            this.listingTypes = listingTypes;
            return this;
        }

        // setting this searches the radius around the longitude and latitude in the listing object
        public Builder withSearchRadius(BigDecimal searchRadius) {
            this.searchRadius = searchRadius;
            return this;
        }

        // setting this overrides the price per night in the availability object
        public Builder withMinPricePerNight(BigDecimal minPricePerNight) {
            this.minPricePerNight = minPricePerNight;
            return this;
        }

        // setting this overrides the price per night in the availability object
        public Builder withMaxPricePerNight(BigDecimal maxPricePerNight) {
            this.maxPricePerNight = maxPricePerNight;
            return this;
        }

        public Builder withStartDateRange(LocalDate startDateRange) {
            this.startDateRange = startDateRange;
            return this;
        }

        public Builder withEndDateRange(LocalDate endDateRange) {
            this.endDateRange = endDateRange;
            return this;
        }

        public Builder withGroupByPriceAscend(boolean groupByPriceAscend) {
            this.groupByPriceAscend = groupByPriceAscend;
            return this;
        }

        public Builder withGroupByPriceDescend(boolean groupByPriceDescend) {
            this.groupByPriceDescend = groupByPriceDescend;
            return this;
        }

        public ListingFilter build() {
            return new ListingFilter(listing, availability, amenities, listingTypes, searchRadius,
                    minPricePerNight, maxPricePerNight, startDateRange, endDateRange,
                    groupByPriceAscend, groupByPriceDescend);
        }
    }
}

