/*
 * This file is generated by jOOQ.
 */
package ru.smartsarov.trackviewer.postgres.tables;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import ru.smartsarov.trackviewer.postgres.Indexes;
import ru.smartsarov.trackviewer.postgres.Keys;
import ru.smartsarov.trackviewer.postgres.Public;
import ru.smartsarov.trackviewer.postgres.tables.records.TrackingDataRecord;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TrackingData extends TableImpl<TrackingDataRecord> {

    private static final long serialVersionUID = -1906896017;

    /**
     * The reference instance of <code>public.tracking_data</code>
     */
    public static final TrackingData TRACKING_DATA = new TrackingData();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<TrackingDataRecord> getRecordType() {
        return TrackingDataRecord.class;
    }

    /**
     * The column <code>public.tracking_data.timestamp</code>.
     */
    public final TableField<TrackingDataRecord, Timestamp> TIMESTAMP = createField("timestamp", org.jooq.impl.SQLDataType.TIMESTAMP, this, "");

    /**
     * The column <code>public.tracking_data.longitude</code>.
     */
    public final TableField<TrackingDataRecord, BigDecimal> LONGITUDE = createField("longitude", org.jooq.impl.SQLDataType.NUMERIC(13, 10), this, "");

    /**
     * The column <code>public.tracking_data.latitude</code>.
     */
    public final TableField<TrackingDataRecord, BigDecimal> LATITUDE = createField("latitude", org.jooq.impl.SQLDataType.NUMERIC(13, 10), this, "");

    /**
     * The column <code>public.tracking_data.velocity</code>.
     */
    public final TableField<TrackingDataRecord, Short> VELOCITY = createField("velocity", org.jooq.impl.SQLDataType.SMALLINT, this, "");

    /**
     * The column <code>public.tracking_data.direction</code>.
     */
    public final TableField<TrackingDataRecord, Short> DIRECTION = createField("direction", org.jooq.impl.SQLDataType.SMALLINT, this, "");

    /**
     * The column <code>public.tracking_data.vehicle_uid</code>.
     */
    public final TableField<TrackingDataRecord, Integer> VEHICLE_UID = createField("vehicle_uid", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.tracking_data.id</code>.
     */
    public final TableField<TrackingDataRecord, Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('tracking_data_id_seq'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.tracking_data.region</code>.
     */
    public final TableField<TrackingDataRecord, Integer> REGION = createField("region", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.tracking_data.odometer</code>.
     */
    public final TableField<TrackingDataRecord, Integer> ODOMETER = createField("odometer", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.tracking_data.additional</code>.
     */
    public final TableField<TrackingDataRecord, Integer> ADDITIONAL = createField("additional", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * Create a <code>public.tracking_data</code> table reference
     */
    public TrackingData() {
        this(DSL.name("tracking_data"), null);
    }

    /**
     * Create an aliased <code>public.tracking_data</code> table reference
     */
    public TrackingData(String alias) {
        this(DSL.name(alias), TRACKING_DATA);
    }

    /**
     * Create an aliased <code>public.tracking_data</code> table reference
     */
    public TrackingData(Name alias) {
        this(alias, TRACKING_DATA);
    }

    private TrackingData(Name alias, Table<TrackingDataRecord> aliased) {
        this(alias, aliased, null);
    }

    private TrackingData(Name alias, Table<TrackingDataRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> TrackingData(Table<O> child, ForeignKey<O, TrackingDataRecord> key) {
        super(child, key, TRACKING_DATA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.ADDITIONAL_FKEY, Indexes.LOG_FOREIGN_KEY, Indexes.REGION_FKEY, Indexes.TRACKING_DATA_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<TrackingDataRecord, Long> getIdentity() {
        return Keys.IDENTITY_TRACKING_DATA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<TrackingDataRecord> getPrimaryKey() {
        return Keys.TRACKING_DATA_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<TrackingDataRecord>> getKeys() {
        return Arrays.<UniqueKey<TrackingDataRecord>>asList(Keys.TRACKING_DATA_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<TrackingDataRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<TrackingDataRecord, ?>>asList(Keys.TRACKING_DATA__TRACKING_DATA_VEHICLE_UID_FKEY, Keys.TRACKING_DATA__TRACKING_DATA_REGION_FKEY, Keys.TRACKING_DATA__TRACKING_DATA_ADDITIONAL_FKEY);
    }

    public VehicleData vehicleData() {
        return new VehicleData(this, Keys.TRACKING_DATA__TRACKING_DATA_VEHICLE_UID_FKEY);
    }

    public RegionRb regionRb() {
        return new RegionRb(this, Keys.TRACKING_DATA__TRACKING_DATA_REGION_FKEY);
    }

    public Additional additional() {
        return new Additional(this, Keys.TRACKING_DATA__TRACKING_DATA_ADDITIONAL_FKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrackingData as(String alias) {
        return new TrackingData(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrackingData as(Name alias) {
        return new TrackingData(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public TrackingData rename(String name) {
        return new TrackingData(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public TrackingData rename(Name name) {
        return new TrackingData(name, null);
    }
}
