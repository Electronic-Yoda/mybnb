package service;

import data.Dao;
import exception.ServiceException;
import filter.ListingFilter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map;
import domain.*;

public class ReportService {
    private final Dao dao;
    public ReportService(Dao dao) {
        this.dao = dao;
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

    public Map<String, Map<String, Long>> getNumberOfBookingsInDateRangePerPostalCodePerCity (LocalDate startDate, LocalDate endDate) throws ServiceException{
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
    
    public Map<String, Long> getNumberOfListingsPerCountry(List<Listing> allListings) throws ServiceException{
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

    public Map<String, Map<String, Map<String, Long>>> getNumberOfListingsPerCityPerCountryPerPostalCode(List<Listing> allListings) throws ServiceException {
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

    // Map<host_name+SIN, Map<Country, numberOfListings>>
    public Map<String, Map<String, Long>> getNumberOfListingsPerCountryPerHost(List<Listing> allListings) throws ServiceException {
        try {
            dao.startTransaction();
            if (allListings == null) {
                allListings = dao.getListings();
            }
            Map<String, Map<String, Long>> res = new HashMap<>();
            for (Listing listing : allListings) {
                User host = dao.getUser(listing.users_sin());
                String hostNameSin = host.name() + host.sin();
                if (res.containsKey(hostNameSin)) {
                    Map<String, Long> countryMap = res.get(hostNameSin);
                    if (countryMap.containsKey(listing.country())) {
                        countryMap.put(listing.country(), countryMap.get(listing.country()) + 1);
                    } else {
                        countryMap.put(listing.country(), 1L);
                    }
                } else {
                    Map<String, Long> countryMap = new HashMap<>();
                    countryMap.put(listing.country(), 1L);
                    res.put(hostNameSin, countryMap);
                }
            }
            dao.commitTransaction();
            return res;
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException("An error occurred while trying to get number of listings per country per host", e);
        }
    }
}
