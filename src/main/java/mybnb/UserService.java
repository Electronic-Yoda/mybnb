package mybnb;

import domain.User;
import data.Dao;
import exception.ServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserService {
    private final Dao dao;
    private static final Logger logger = LogManager.getLogger(UserService.class);

    public UserService(Dao dao) {
        this.dao = dao;
    }

    public void addUser(User user) {
        // check if user already exists
        if (!dao.userExists(user.sin())) {
            dao.insertUser(user);
            logger.info("User added successfully");
        } else {
            throw new ServiceException(
                String.format(
                    "Unable to add user because user with SIN %d already exists", user.sin()
                )
            );
        }
    }

    public void deleteUser(Long sin) {
        if (dao.userExists(sin)) {
            dao.deleteUser(sin);
            logger.info("User deleted successfully");
        } else {
            throw new ServiceException(
                String.format(
                    "Unable to delete user because user with SIN %d does not exist", sin
                )
            );
        }
    }
}
