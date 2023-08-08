package service;

import domain.Booking;
import domain.Listing;
import domain.User;
import data.Dao;
import exception.ServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class UserService {
    private final Dao dao;
    private static final Logger logger = LogManager.getLogger(UserService.class);

    public UserService(Dao dao) {
        this.dao = dao;
    }

    public Long addUser(User user) throws ServiceException{
        try {
            dao.startTransaction();  // Begin transaction
            if (dao.userExists(user.sin())) {
                throw new ServiceException(
                        String.format(
                                "Unable to add user because user with SIN %d already exists", user.sin()
                        )
                );
            }
            Long sin = dao.insertUser(user);
            dao.commitTransaction();  // Commit transaction if all operations succeeded
            return sin;
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occurred while trying to add user", e);
        }
    }

    public void deleteUser(Long sin) throws ServiceException{
        try {
            dao.startTransaction();  // Begin transaction
            if (!dao.userExists(sin)) {
                throw new ServiceException(
                        String.format(
                                "Unable to delete user because user with SIN %d does not exist", sin
                        )
                );
            }
            List<Listing> listings = dao.getListingsByHostSin(sin);
            if (!listings.isEmpty()) {
                throw new ServiceException(
                        String.format(
                                "Unable to delete user because user with SIN %d is a host of %d listings", sin, listings.size()
                        )
                );
            }
            List<Booking> bookings = dao.getTenenatBookings(sin);
            if (!bookings.isEmpty()) {
                throw new ServiceException(
                        String.format(
                                "Unable to delete user because user with SIN %d is a tenant of %d bookings", sin, bookings.size()
                        )
                );
            }
            dao.deleteUser(sin);
            dao.commitTransaction();  // Commit transaction if all operations succeeded
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occurred while trying to delete user", e);
        }
    }

    public List<User> getUsers() throws ServiceException{
        try {
            dao.startTransaction();  // Begin transaction
            List<User> users = dao.getUsers();
            dao.commitTransaction();  // Commit transaction if all operations succeeded
            return users;
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occurred while trying to show users", e);
        }
    }

    public boolean userExists(Long sin) throws ServiceException{
        try {
            dao.startTransaction();  // Begin transaction
            boolean exists = dao.userExists(sin);
            dao.commitTransaction();  // Commit transaction if all operations succeeded
            return exists;
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occurred while trying to check if user exists", e);
        }
    }
}
