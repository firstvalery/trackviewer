/*
 * This file is generated by jOOQ.
 */
package ru.smartsarov.trackviewer.postgres;


import javax.annotation.Generated;

import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.UniqueKey;
import org.jooq.impl.Internal;

import ru.smartsarov.trackviewer.postgres.tables.Additional;
import ru.smartsarov.trackviewer.postgres.tables.DayReport;
import ru.smartsarov.trackviewer.postgres.tables.RegionRb;
import ru.smartsarov.trackviewer.postgres.tables.TrackingData;
import ru.smartsarov.trackviewer.postgres.tables.VehicleData;
import ru.smartsarov.trackviewer.postgres.tables.records.AdditionalRecord;
import ru.smartsarov.trackviewer.postgres.tables.records.DayReportRecord;
import ru.smartsarov.trackviewer.postgres.tables.records.RegionRbRecord;
import ru.smartsarov.trackviewer.postgres.tables.records.TrackingDataRecord;
import ru.smartsarov.trackviewer.postgres.tables.records.VehicleDataRecord;


/**
 * A class modelling foreign key relationships and constraints of tables of 
 * the <code>public</code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------

    public static final Identity<AdditionalRecord, Integer> IDENTITY_ADDITIONAL = Identities0.IDENTITY_ADDITIONAL;
    public static final Identity<DayReportRecord, Integer> IDENTITY_DAY_REPORT = Identities0.IDENTITY_DAY_REPORT;
    public static final Identity<RegionRbRecord, Integer> IDENTITY_REGION_RB = Identities0.IDENTITY_REGION_RB;
    public static final Identity<TrackingDataRecord, Long> IDENTITY_TRACKING_DATA = Identities0.IDENTITY_TRACKING_DATA;
    public static final Identity<VehicleDataRecord, Integer> IDENTITY_VEHICLE_DATA = Identities0.IDENTITY_VEHICLE_DATA;

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<AdditionalRecord> ADDITIONAL_PKEY = UniqueKeys0.ADDITIONAL_PKEY;
    public static final UniqueKey<DayReportRecord> DAY_REPORT_PKEY = UniqueKeys0.DAY_REPORT_PKEY;
    public static final UniqueKey<RegionRbRecord> REGION_RB_PKEY = UniqueKeys0.REGION_RB_PKEY;
    public static final UniqueKey<TrackingDataRecord> TRACKING_DATA_PKEY = UniqueKeys0.TRACKING_DATA_PKEY;
    public static final UniqueKey<VehicleDataRecord> VEHICLE_DAT_PKEY = UniqueKeys0.VEHICLE_DAT_PKEY;
    public static final UniqueKey<VehicleDataRecord> VEHICLE_DATA_NUMBER_KEY = UniqueKeys0.VEHICLE_DATA_NUMBER_KEY;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<TrackingDataRecord, VehicleDataRecord> TRACKING_DATA__TRACKING_DATA_VEHICLE_UID_FKEY = ForeignKeys0.TRACKING_DATA__TRACKING_DATA_VEHICLE_UID_FKEY;
    public static final ForeignKey<TrackingDataRecord, RegionRbRecord> TRACKING_DATA__TRACKING_DATA_REGION_FKEY = ForeignKeys0.TRACKING_DATA__TRACKING_DATA_REGION_FKEY;
    public static final ForeignKey<TrackingDataRecord, AdditionalRecord> TRACKING_DATA__TRACKING_DATA_ADDITIONAL_FKEY = ForeignKeys0.TRACKING_DATA__TRACKING_DATA_ADDITIONAL_FKEY;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 {
        public static Identity<AdditionalRecord, Integer> IDENTITY_ADDITIONAL = Internal.createIdentity(Additional.ADDITIONAL, Additional.ADDITIONAL.ID);
        public static Identity<DayReportRecord, Integer> IDENTITY_DAY_REPORT = Internal.createIdentity(DayReport.DAY_REPORT, DayReport.DAY_REPORT.ID);
        public static Identity<RegionRbRecord, Integer> IDENTITY_REGION_RB = Internal.createIdentity(RegionRb.REGION_RB, RegionRb.REGION_RB.ID);
        public static Identity<TrackingDataRecord, Long> IDENTITY_TRACKING_DATA = Internal.createIdentity(TrackingData.TRACKING_DATA, TrackingData.TRACKING_DATA.ID);
        public static Identity<VehicleDataRecord, Integer> IDENTITY_VEHICLE_DATA = Internal.createIdentity(VehicleData.VEHICLE_DATA, VehicleData.VEHICLE_DATA.ID);
    }

    private static class UniqueKeys0 {
        public static final UniqueKey<AdditionalRecord> ADDITIONAL_PKEY = Internal.createUniqueKey(Additional.ADDITIONAL, "additional_pkey", Additional.ADDITIONAL.ID);
        public static final UniqueKey<DayReportRecord> DAY_REPORT_PKEY = Internal.createUniqueKey(DayReport.DAY_REPORT, "day_report_pkey", DayReport.DAY_REPORT.ID);
        public static final UniqueKey<RegionRbRecord> REGION_RB_PKEY = Internal.createUniqueKey(RegionRb.REGION_RB, "region_rb_pkey", RegionRb.REGION_RB.ID);
        public static final UniqueKey<TrackingDataRecord> TRACKING_DATA_PKEY = Internal.createUniqueKey(TrackingData.TRACKING_DATA, "tracking_data_pkey", TrackingData.TRACKING_DATA.ID);
        public static final UniqueKey<VehicleDataRecord> VEHICLE_DAT_PKEY = Internal.createUniqueKey(VehicleData.VEHICLE_DATA, "vehicle_dat_pkey", VehicleData.VEHICLE_DATA.ID);
        public static final UniqueKey<VehicleDataRecord> VEHICLE_DATA_NUMBER_KEY = Internal.createUniqueKey(VehicleData.VEHICLE_DATA, "vehicle_data_number_key", VehicleData.VEHICLE_DATA.NUMBER);
    }

    private static class ForeignKeys0 {
        public static final ForeignKey<TrackingDataRecord, VehicleDataRecord> TRACKING_DATA__TRACKING_DATA_VEHICLE_UID_FKEY = Internal.createForeignKey(ru.smartsarov.trackviewer.postgres.Keys.VEHICLE_DAT_PKEY, TrackingData.TRACKING_DATA, "tracking_data__tracking_data_vehicle_uid_fkey", TrackingData.TRACKING_DATA.VEHICLE_UID);
        public static final ForeignKey<TrackingDataRecord, RegionRbRecord> TRACKING_DATA__TRACKING_DATA_REGION_FKEY = Internal.createForeignKey(ru.smartsarov.trackviewer.postgres.Keys.REGION_RB_PKEY, TrackingData.TRACKING_DATA, "tracking_data__tracking_data_region_fkey", TrackingData.TRACKING_DATA.REGION);
        public static final ForeignKey<TrackingDataRecord, AdditionalRecord> TRACKING_DATA__TRACKING_DATA_ADDITIONAL_FKEY = Internal.createForeignKey(ru.smartsarov.trackviewer.postgres.Keys.ADDITIONAL_PKEY, TrackingData.TRACKING_DATA, "tracking_data__tracking_data_additional_fkey", TrackingData.TRACKING_DATA.ADDITIONAL);
    }
}
