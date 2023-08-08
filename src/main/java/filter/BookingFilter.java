package filter;

import domain.Booking;
import domain.Listing;

public class BookingFilter {
    private Booking booking;
    private Listing listing;

    public BookingFilter(Booking booking, Listing listing) {
        this.booking = booking;
        this.listing = listing;
    }

    public Booking booking() {
        return booking;
    }

    public Listing listing() {
        return listing;
    }

    public static class Builder {
        private Booking booking;
        private Listing listing;

        public Builder booking(Booking booking) {
            this.booking = booking;
            return this;
        }

        public Builder listing(Listing listing) {
            this.listing = listing;
            return this;
        }

        public BookingFilter build() {
            return new BookingFilter(booking, listing);
        }
    }
}
