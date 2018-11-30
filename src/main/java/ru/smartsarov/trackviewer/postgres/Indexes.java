/*
 * This file is generated by jOOQ.
 */
package ru.smartsarov.trackviewer.postgres;


import javax.annotation.Generated;

import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.Internal;

import ru.smartsarov.trackviewer.postgres.tables.RegionRb;
import ru.smartsarov.trackviewer.postgres.tables.TrackingData;
import ru.smartsarov.trackviewer.postgres.tables.VehicleData;


/**
 * A class modelling indexes of tables of the <code>public</code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index REGION_RB_PKEY = Indexes0.REGION_RB_PKEY;
    public static final Index LOG_FOREIGN_KEY = Indexes0.LOG_FOREIGN_KEY;
    public static final Index REGION_FKEY = Indexes0.REGION_FKEY;
    public static final Index TRACKING_DATA_PKEY = Indexes0.TRACKING_DATA_PKEY;
    public static final Index VEHICLE_DAT_PKEY = Indexes0.VEHICLE_DAT_PKEY;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Indexes0 {
        public static Index REGION_RB_PKEY = Internal.createIndex("region_rb_pkey", RegionRb.REGION_RB, new OrderField[] { RegionRb.REGION_RB.ID }, true);
        public static Index LOG_FOREIGN_KEY = Internal.createIndex("log_foreign_key", TrackingData.TRACKING_DATA, new OrderField[] { TrackingData.TRACKING_DATA.VEHICLE_UID }, false);
        public static Index REGION_FKEY = Internal.createIndex("region_fkey", TrackingData.TRACKING_DATA, new OrderField[] { TrackingData.TRACKING_DATA.REGION }, false);
        public static Index TRACKING_DATA_PKEY = Internal.createIndex("tracking_data_pkey", TrackingData.TRACKING_DATA, new OrderField[] { TrackingData.TRACKING_DATA.ID }, true);
        public static Index VEHICLE_DAT_PKEY = Internal.createIndex("vehicle_dat_pkey", VehicleData.VEHICLE_DATA, new OrderField[] { VehicleData.VEHICLE_DATA.ID }, true);
    }
}
