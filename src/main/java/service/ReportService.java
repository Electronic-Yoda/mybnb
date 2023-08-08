package service;

import data.Dao;
import exception.ServiceException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import domain.*;
import tool.LanguageProcessing;

public class ReportService {
    private final Dao dao;
    private final LanguageProcessing languageProcessing;

    public ReportService(Dao dao) {
        this.dao = dao;
        try {
            this.languageProcessing = new LanguageProcessing();
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while trying to initialize the language processing tool", e);
        }
    }

    public Map<String, Long> getNumberOfBookingsInDateRangePerCity(LocalDate startDate, LocalDate endDate) throws ServiceException {
        try {
            dao.startTransaction();  // Begin transaction
            Map<String, Long> res = dao.getNumberOfBookingsInDateRangePerCity(startDate, endDate);
            dao.commitTransaction();  // Commit transaction if all operations succeeded
            return res;
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occurred while trying to get number of bookings by date range and city", e);
        }
    }

    public Map<String, Map<String, Long>> getNumberOfBookingsInDateRangePerPostalCodePerCity(LocalDate startDate, LocalDate endDate) throws ServiceException {
        try {
            dao.startTransaction();  // Begin transaction
            Map<String, Map<String, Long>> res = dao.getNumberOfBookingsInDateRangePerPostalCodePerCity(startDate, endDate);
            dao.commitTransaction();  // Commit transaction if all operations succeeded
            return res;
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occurred while trying to get number of bookings by date range and city", e);
        }
    }

    public Map<String, Long> getNumberOfListingsPerCountry(List<Listing> allListings) throws ServiceException {
        try {
            dao.startTransaction();  // Begin transaction
            if (allListings == null) {
                allListings = dao.getListings();
            }
            Map<String, Long> res = new HashMap<>();
            for (Listing listing : allListings) {
                if (res.containsKey(listing.country())) {
                    res.put(listing.country(), res.get(listing.country()) + 1);
                } else {
                    res.put(listing.country(), 1L);
                }
            }
            dao.commitTransaction();  // Commit transaction if all operations succeeded
            return res;
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occurred while trying to get number of listings per country", e);
        }
    }

    public Map<String, Map<String, Long>> getNumberOfListingsPerCityPerCountry(List<Listing> allListings) throws ServiceException {
        try {
            dao.startTransaction();
            if (allListings == null) {
                allListings = dao.getListings();
            }
            Map<String, Map<String, Long>> res = new HashMap<>();
            for (Listing listing : allListings) {
                if (res.containsKey(listing.country())) {
                    Map<String, Long> cityMap = res.get(listing.country());
                    if (cityMap.containsKey(listing.city())) {
                        cityMap.put(listing.city(), cityMap.get(listing.city()) + 1);
                    } else {
                        cityMap.put(listing.city(), 1L);
                    }
                } else {
                    Map<String, Long> cityMap = new HashMap<>();
                    cityMap.put(listing.city(), 1L);
                    res.put(listing.country(), cityMap);
                }
            }
            dao.commitTransaction();
            return res;
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException("An error occurred while trying to get number of listings per city per country", e);
        }
    }

    public Map<String, Map<String, Map<String, Long>>> getNumberOfListingsPerPostalCodePerCityPerCountry(List<Listing> allListings) throws ServiceException {
        try {
            dao.startTransaction();
            if (allListings == null) {
                allListings = dao.getListings();
            }
            Map<String, Map<String, Map<String, Long>>> res = new HashMap<>();
            for (Listing listing : allListings) {
                if (res.containsKey(listing.country())) {
                    Map<String, Map<String, Long>> cityMap = res.get(listing.country());
                    if (cityMap.containsKey(listing.city())) {
                        Map<String, Long> postalCodeMap = cityMap.get(listing.city());
                        if (postalCodeMap.containsKey(listing.postal_code())) {
                            postalCodeMap.put(listing.postal_code(), postalCodeMap.get(listing.postal_code()) + 1);
                        } else {
                            postalCodeMap.put(listing.postal_code(), 1L);
                        }
                    } else {
                        Map<String, Long> postalCodeMap = new HashMap<>();
                        postalCodeMap.put(listing.postal_code(), 1L);
                        cityMap.put(listing.city(), postalCodeMap);
                    }
                } else {
                    Map<String, Map<String, Long>> cityMap = new HashMap<>();
                    Map<String, Long> postalCodeMap = new HashMap<>();
                    postalCodeMap.put(listing.postal_code(), 1L);
                    cityMap.put(listing.city(), postalCodeMap);
                    res.put(listing.country(), cityMap);
                }
            }
            dao.commitTransaction();
            return res;
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException("An error occurred while trying to get number of listings per city per country per postal code", e);
        }
    }

    // Map<User, Map<Country, numberOfListings>>
    public Map<User, Map<String, Long>> getNumberOfListingsPerCountryPerHost(List<Listing> allListings) throws ServiceException {
        try {
            dao.startTransaction();
            if (allListings == null) {
                allListings = dao.getListings();
            }
            Map<User, Map<String, Long>> res = new HashMap<>();
            for (Listing listing : allListings) {
                User host = dao.getUser(listing.users_sin());
                if (res.containsKey(host)) {
                    Map<String, Long> countryMap = res.get(host);
                    if (countryMap.containsKey(listing.country())) {
                        countryMap.put(listing.country(), countryMap.get(listing.country()) + 1);
                    } else {
                        countryMap.put(listing.country(), 1L);
                    }
                } else {
                    Map<String, Long> countryMap = new HashMap<>();
                    countryMap.put(listing.country(), 1L);
                    res.put(host, countryMap);
                }
            }
            dao.commitTransaction();
            return res;
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException("An error occurred while trying to get number of listings per country per host", e);
        }
    }

    public Map<User, Map<String, Map<String, Long>>> getNumberOfListingsPerCityPerCountryPerHost(List<Listing> allListings) throws ServiceException {
        try {
            dao.startTransaction();
            if (allListings == null) {
                allListings = dao.getListings();
            }
            Map<User, Map<String, Map<String, Long>>> res = new HashMap<>();
            for (Listing listing : allListings) {
                User host = dao.getUser(listing.users_sin());
                if (res.containsKey(host)) {
                    Map<String, Map<String, Long>> countryMap = res.get(host);
                    if (countryMap.containsKey(listing.country())) {
                        Map<String, Long> cityMap = countryMap.get(listing.country());
                        if (cityMap.containsKey(listing.city())) {
                            cityMap.put(listing.city(), cityMap.get(listing.city()) + 1);
                        } else {
                            cityMap.put(listing.city(), 1L);
                        }
                    } else {
                        Map<String, Long> cityMap = new HashMap<>();
                        cityMap.put(listing.city(), 1L);
                        countryMap.put(listing.country(), cityMap);
                    }
                } else {
                    Map<String, Map<String, Long>> countryMap = new HashMap<>();
                    Map<String, Long> cityMap = new HashMap<>();
                    cityMap.put(listing.city(), 1L);
                    countryMap.put(listing.country(), cityMap);
                    res.put(host, countryMap);
                }
            }
            dao.commitTransaction();
            return res;
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException("An error occurred while trying to get number of listings per city per country per host", e);
        }
    }

    // Map<String, Map<String, Map<String, Long>>>
    // To get the number of listings per city per country per host for hosts that have x percentage of listings in a city
    public Map<User, Map<String, Map<String, Long>>> getPossibleCommercialHosts(
            Map<User, Map<String, Map<String, Long>>> numberOfListingsPerCityPerCountryPerHost,
            Map<String, Map<String, Long>> numberOfListingsPerCityPerCountry,
            int percentage) {
        Map<User, Map<String, Map<String, Long>>> res = new HashMap<>();

        for (User host : numberOfListingsPerCityPerCountryPerHost.keySet()) {
            Map<String, Map<String, Long>> countryMap = numberOfListingsPerCityPerCountryPerHost.get(host);
            for (String country : countryMap.keySet()) {
                Map<String, Long> cityMap = countryMap.get(country);
                for (String city : cityMap.keySet()) {
                    if ((float) cityMap.get(city) / (float) numberOfListingsPerCityPerCountry.get(country).get(city) * 100 >= percentage) {
                        if (res.containsKey(host)) {
                            Map<String, Map<String, Long>> countryMap2 = res.get(host);
                            if (countryMap2.containsKey(country)) {
                                Map<String, Long> cityMap2 = countryMap2.get(country);
                                cityMap2.put(city, cityMap.get(city));
                            } else {
                                Map<String, Long> cityMap2 = new HashMap<>();
                                cityMap2.put(city, cityMap.get(city));
                                countryMap2.put(country, cityMap2);
                            }
                        } else {
                            Map<String, Map<String, Long>> countryMap2 = new HashMap<>();
                            Map<String, Long> cityMap2 = new HashMap<>();
                            cityMap2.put(city, cityMap.get(city));
                            countryMap2.put(country, cityMap2);
                            res.put(host, countryMap2);
                        }
                    }
                }
            }
        }
        return res;
    }

    public Map<User, Long> getNumberOfBookingsInDateRangePerRenter(LocalDate startDate, LocalDate endDate) throws ServiceException {
        try {
            dao.startTransaction();
            Map<User, Long> res = dao.getNumberOfBookingsInDateRangePerRenter(startDate, endDate);
            dao.commitTransaction();
            return res;
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException("An error occurred while trying to get number of bookings in date range per renter", e);
        }
    }

    public Map<String, Map<User, Long>> getNumberOfBookingsInDateRangePerRenterPerCity(LocalDate startDate, LocalDate endDate) throws ServiceException {
        try {
            dao.startTransaction();
            Map<String, Map<User, Long>> res = dao.getNumberOfBookingsInDateRangePerRenterPerCity(startDate, endDate);
            dao.commitTransaction();
            return res;
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException("An error occurred while trying to get number of bookings in date range per renter per city", e);
        }
    }

    public Map<User, Long> getNumberOfCancelledBookingsInDateRangePerRenter(LocalDate startDate, LocalDate endDate) throws ServiceException{
        try {
            dao.startTransaction();
            Map<User, Long> res = dao.getNumberOfCancelledBookingsInDateRangePerRenter(startDate, endDate);
            dao.commitTransaction();
            return res;
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException("An error occurred while trying to get number of cancelled bookings in date range per renter", e);
        }
    }

    public Map<User, Long> getNumberOfCancelledBookingsInDateRangePerHost(LocalDate startDate, LocalDate endDate) throws ServiceException{
        try {
            dao.startTransaction();
            Map<User, Long> res = dao.getNumberOfCancelledBookingsInDateRangePerHost(startDate, endDate);
            dao.commitTransaction();
            return res;
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException("An error occurred while trying to get number of cancelled bookings in date range per host", e);
        }
    }

    public Map<Listing, Map<String, Long>> getMostPopularNounPhrasesPerListing(List<Listing> allListings) throws ServiceException {
        try {
            dao.startTransaction();
            if (allListings == null) {
                allListings = dao.getListings();
            }
            Map<Listing, Map<String, Long>> res = new LinkedHashMap<>(); // ensures order of listings
            for (Listing listing : allListings) {
                List<Review> reviews = dao.getReviewsOfListing(listing.listing_id());
                List<String> commentsFromTenant = reviews.stream().map(Review::comment_from_tenant).collect(Collectors.toList());
                // extract the noun phrases from the comments, and count the number of occurrences
                Map<String, Long> nounPhrases = new HashMap<>(); // key: number of occurrences, value: noun phrase
                for (String comment : commentsFromTenant) {
                    if (comment != null && !comment.isEmpty()) {
                        List<String> nounPhrasesInComment = new ArrayList<>();
                        try {
                            nounPhrasesInComment = languageProcessing.extractNounPhrases(comment);
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new ServiceException("An error occurred while trying to extract noun phrases from comment", e);
                        }
                        for (String nounPhrase : nounPhrasesInComment) {
                            if (nounPhrases.containsKey(nounPhrase)) {
                                nounPhrases.put(nounPhrase, nounPhrases.get(nounPhrase) + 1);
                            } else {
                                nounPhrases.put(nounPhrase, 1L);
                            }
                        }
                    }
                }
                // sort the noun phrases by number of occurrences
                nounPhrases = nounPhrases.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (oldValue, newValue) -> oldValue, LinkedHashMap::new));
                res.put(listing, nounPhrases);
            }
            dao.commitTransaction();
            return res;
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException("An error occurred while trying to get most popular noun phrases per listing", e);
        }
    }
}
