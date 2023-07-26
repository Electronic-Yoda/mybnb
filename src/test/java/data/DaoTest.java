package data;

import domain.User;
import filter.UserFilter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DaoTest {

    @org.junit.jupiter.api.Test
    void tableTest() {
        Dao dao = new Dao();
        dao.dropTables();
        List<String> tables = dao.getOriginalTables();
        dao.createTables();
        List<String> tablesRetrieved = dao.getTables();
        Collections.sort(tables);
        System.out.println(tables);
        System.out.println(tablesRetrieved);
        Collections.sort(tablesRetrieved);
        assertTrue(tables.equals(tablesRetrieved));
        dao.dropTables();
        tablesRetrieved = dao.getTables();
        assertTrue(tablesRetrieved.isEmpty());
    }

    @org.junit.jupiter.api.Test
    void userTest() {
        Dao dao = new Dao();
        dao.dropTables();
        dao.createTables();
        assertTrue(dao.getUsers().isEmpty());

        User user = new User(
                123456789L,
                "John Doe",
                "32 Main St. Toronto, ON",
                LocalDate.parse("2001-03-12"),
                "Student"
        );
        dao.insertUser(user);

        User userRetrieved = dao.getUsers().get(0);
        assertTrue(dao.getUsers().size() == 1);
        assertTrue(user.equals(userRetrieved));
        dao.deleteUser(123456789L);
        assertTrue(dao.getUsers().isEmpty());
        dao.dropTables();
    }

    @org.junit.jupiter.api.Test
    void userFilterTest() {
        Dao dao = new Dao();
        dao.dropTables();
        dao.createTables();
        assertTrue(dao.getUsers().isEmpty());

        User user1 = new User(
                1L,
                "John Doe",
                "32 Main St. Toronto, ON",
                LocalDate.parse("2001-03-12"),
                "Student"
        );
        dao.insertUser(user1);
        User user2 = new User(
                2L,
                "John Doe",
                "12 College St. Toronto, ON",
                LocalDate.parse("1976-01-09"),
                "Doctor"
        );
        dao.insertUser(user2);

        assertTrue(dao.getUsers().size() == 2);
        List<User> usersRetrieved = dao.getUsersByFilter(
                new UserFilter(
                        1L,
                        null,
                        null,
                        null,
                        null
                )
        );
        assertTrue(usersRetrieved.size() == 1);
        assertTrue(usersRetrieved.get(0).equals(user1));

        usersRetrieved = dao.getUsersByFilter(
                new UserFilter(
                        null,
                        "John Doe",
                        null,
                        null,
                        null
                )
        );
        assertTrue(usersRetrieved.size() == 2);
        for (User user : usersRetrieved) {
            assertTrue(user.name().equals("John Doe"));
            System.out.println(user);
        }
    }

    @org.junit.jupiter.api.Test
    void listingBasicTest() {
//        Dao dao = new Dao();
//        dao.dropTables();
//        dao.createTables();
//        assertTrue(dao.getListings().isEmpty());
//        dao.dropTables();
    }

    @org.junit.jupiter.api.Test
    void insertListing() {
    }

    @org.junit.jupiter.api.Test
    void listingExists() {
    }

    @org.junit.jupiter.api.Test
    void getAmenities() {
    }
}