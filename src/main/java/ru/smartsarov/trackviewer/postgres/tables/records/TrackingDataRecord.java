/*
 * This file is generated by jOOQ.
 */
package ru.smartsarov.trackviewer.postgres.tables.records;


import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record9;
import org.jooq.Row9;
import org.jooq.impl.UpdatableRecordImpl;

import ru.smartsarov.trackviewer.postgres.tables.TrackingData;


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
public class TrackingDataRecord extends UpdatableRecordImpl<TrackingDataRecord> implements Record9<Timestamp, BigDecimal, BigDecimal, Short, Short, Integer, Long, Integer, Integer> {

    private static final long serialVersionUID = -889919164;

    /**
     * Setter for <code>public.tracking_data.timestamp</code>.
     */
    public void setTimestamp(Timestamp value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.tracking_data.timestamp</code>.
     */
    public Timestamp getTimestamp() {
        return (Timestamp) get(0);
    }

    /**
     * Setter for <code>public.tracking_data.longitude</code>.
     */
    public void setLongitude(BigDecimal value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.tracking_data.longitude</code>.
     */
    public BigDecimal getLongitude() {
        return (BigDecimal) get(1);
    }

    /**
     * Setter for <code>public.tracking_data.latitude</code>.
     */
    public void setLatitude(BigDecimal value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.tracking_data.latitude</code>.
     */
    public BigDecimal getLatitude() {
        return (BigDecimal) get(2);
    }

    /**
     * Setter for <code>public.tracking_data.velocity</code>.
     */
    public void setVelocity(Short value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.tracking_data.velocity</code>.
     */
    public Short getVelocity() {
        return (Short) get(3);
    }

    /**
     * Setter for <code>public.tracking_data.direction</code>.
     */
    public void setDirection(Short value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.tracking_data.direction</code>.
     */
    public Short getDirection() {
        return (Short) get(4);
    }

    /**
     * Setter for <code>public.tracking_data.vehicle_uid</code>.
     */
    public void setVehicleUid(Integer value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.tracking_data.vehicle_uid</code>.
     */
    public Integer getVehicleUid() {
        return (Integer) get(5);
    }

    /**
     * Setter for <code>public.tracking_data.id</code>.
     */
    public void setId(Long value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.tracking_data.id</code>.
     */
    public Long getId() {
        return (Long) get(6);
    }

    /**
     * Setter for <code>public.tracking_data.region</code>.
     */
    public void setRegion(Integer value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.tracking_data.region</code>.
     */
    public Integer getRegion() {
        return (Integer) get(7);
    }

    /**
     * Setter for <code>public.tracking_data.odometer</code>.
     */
    public void setOdometer(Integer value) {
        set(8, value);
    }

    /**
     * Getter for <code>public.tracking_data.odometer</code>.
     */
    public Integer getOdometer() {
        return (Integer) get(8);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record9 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row9<Timestamp, BigDecimal, BigDecimal, Short, Short, Integer, Long, Integer, Integer> fieldsRow() {
        return (Row9) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row9<Timestamp, BigDecimal, BigDecimal, Short, Short, Integer, Long, Integer, Integer> valuesRow() {
        return (Row9) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field1() {
        return TrackingData.TRACKING_DATA.TIMESTAMP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<BigDecimal> field2() {
        return TrackingData.TRACKING_DATA.LONGITUDE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<BigDecimal> field3() {
        return TrackingData.TRACKING_DATA.LATITUDE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Short> field4() {
        return TrackingData.TRACKING_DATA.VELOCITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Short> field5() {
        return TrackingData.TRACKING_DATA.DIRECTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field6() {
        return TrackingData.TRACKING_DATA.VEHICLE_UID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field7() {
        return TrackingData.TRACKING_DATA.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field8() {
        return TrackingData.TRACKING_DATA.REGION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field9() {
        return TrackingData.TRACKING_DATA.ODOMETER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp component1() {
        return getTimestamp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal component2() {
        return getLongitude();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal component3() {
        return getLatitude();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Short component4() {
        return getVelocity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Short component5() {
        return getDirection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component6() {
        return getVehicleUid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component7() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component8() {
        return getRegion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component9() {
        return getOdometer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value1() {
        return getTimestamp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal value2() {
        return getLongitude();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal value3() {
        return getLatitude();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Short value4() {
        return getVelocity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Short value5() {
        return getDirection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value6() {
        return getVehicleUid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value7() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value8() {
        return getRegion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value9() {
        return getOdometer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrackingDataRecord value1(Timestamp value) {
        setTimestamp(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrackingDataRecord value2(BigDecimal value) {
        setLongitude(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrackingDataRecord value3(BigDecimal value) {
        setLatitude(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrackingDataRecord value4(Short value) {
        setVelocity(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrackingDataRecord value5(Short value) {
        setDirection(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrackingDataRecord value6(Integer value) {
        setVehicleUid(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrackingDataRecord value7(Long value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrackingDataRecord value8(Integer value) {
        setRegion(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrackingDataRecord value9(Integer value) {
        setOdometer(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrackingDataRecord values(Timestamp value1, BigDecimal value2, BigDecimal value3, Short value4, Short value5, Integer value6, Long value7, Integer value8, Integer value9) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached TrackingDataRecord
     */
    public TrackingDataRecord() {
        super(TrackingData.TRACKING_DATA);
    }

    /**
     * Create a detached, initialised TrackingDataRecord
     */
    public TrackingDataRecord(Timestamp timestamp, BigDecimal longitude, BigDecimal latitude, Short velocity, Short direction, Integer vehicleUid, Long id, Integer region, Integer odometer) {
        super(TrackingData.TRACKING_DATA);

        set(0, timestamp);
        set(1, longitude);
        set(2, latitude);
        set(3, velocity);
        set(4, direction);
        set(5, vehicleUid);
        set(6, id);
        set(7, region);
        set(8, odometer);
    }
}
