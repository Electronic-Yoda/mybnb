package data;

import domain.User;

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
                "John",
                "Doe",
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
    void insertListing() {
    }

    @org.junit.jupiter.api.Test
    void listingExists() {
    }

    @org.junit.jupiter.api.Test
    void getAmenities() {
    }
}