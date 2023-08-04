package data;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DbConfigTest {
    @org.junit.jupiter.api.Test
    void tableTest() {
        DbConfig dbConfig = new DbConfig();
        dbConfig.resetTables();
        List<String> tables = dbConfig.getOriginalTables();
        List<String> tablesRetrieved = dbConfig.getTables();
        Collections.sort(tables);
        System.out.println(tables);
        System.out.println(tablesRetrieved);
        Collections.sort(tablesRetrieved);
        assertTrue(tables.equals(tablesRetrieved));
        dbConfig.dropTables();
        tablesRetrieved = dbConfig.getTables();
        assertTrue(tablesRetrieved.isEmpty());
        dbConfig.resetTables();
    }
}