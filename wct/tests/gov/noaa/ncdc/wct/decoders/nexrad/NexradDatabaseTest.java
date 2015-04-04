package gov.noaa.ncdc.wct.decoders.nexrad;

import java.sql.SQLException;
import java.util.HashMap;

import org.junit.Test;

public class NexradDatabaseTest {
    
    @Test
    public void testDbInstance() throws ClassNotFoundException, SQLException {
        NexradDatabase radDB = NexradDatabase.getSharedInstance();
    }

    @Test
    public void testProductLongNameMap() throws ClassNotFoundException, SQLException {
        NexradDatabase radDB = NexradDatabase.getSharedInstance();
        HashMap map = radDB.getRadarProductLongNamesMap();
    }
}
