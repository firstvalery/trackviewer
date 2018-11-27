/*
 * This file is generated by jOOQ.
 */
package ru.smartsarov.trackviewer.postgres;


import javax.annotation.Generated;

import ru.smartsarov.trackviewer.postgres.tables.TrackingData;
import ru.smartsarov.trackviewer.postgres.tables.VehicleData;


/**
 * Convenience access to all tables in public
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>public.tracking_data</code>.
     */
    public static final TrackingData TRACKING_DATA = ru.smartsarov.trackviewer.postgres.tables.TrackingData.TRACKING_DATA;

    /**
     * The table <code>public.vehicle_data</code>.
     */
    public static final VehicleData VEHICLE_DATA = ru.smartsarov.trackviewer.postgres.tables.VehicleData.VEHICLE_DATA;
}