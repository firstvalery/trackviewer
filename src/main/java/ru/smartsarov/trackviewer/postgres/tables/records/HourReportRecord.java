/*
 * This file is generated by jOOQ.
 */
package ru.smartsarov.trackviewer.postgres.tables.records;


import java.sql.Timestamp;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
import org.jooq.impl.UpdatableRecordImpl;

import ru.smartsarov.trackviewer.postgres.tables.HourReport;


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
public class HourReportRecord extends UpdatableRecordImpl<HourReportRecord> implements Record8<Long, Timestamp, Integer, Integer, Integer, Integer, Integer, Float> {

    private static final long serialVersionUID = -1744938247;

    /**
     * Setter for <code>public.hour_report.id</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.hour_report.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.hour_report.ts</code>.
     */
    public void setTs(Timestamp value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.hour_report.ts</code>.
     */
    public Timestamp getTs() {
        return (Timestamp) get(1);
    }

    /**
     * Setter for <code>public.hour_report.vehicle_id</code>.
     */
    public void setVehicleId(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.hour_report.vehicle_id</code>.
     */
    public Integer getVehicleId() {
        return (Integer) get(2);
    }

    /**
     * Setter for <code>public.hour_report.distance</code>.
     */
    public void setDistance(Integer value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.hour_report.distance</code>.
     */
    public Integer getDistance() {
        return (Integer) get(3);
    }

    /**
     * Setter for <code>public.hour_report.waiting</code>.
     */
    public void setWaiting(Integer value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.hour_report.waiting</code>.
     */
    public Integer getWaiting() {
        return (Integer) get(4);
    }

    /**
     * Setter for <code>public.hour_report.driving</code>.
     */
    public void setDriving(Integer value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.hour_report.driving</code>.
     */
    public Integer getDriving() {
        return (Integer) get(5);
    }

    /**
     * Setter for <code>public.hour_report.waitpoints_id</code>.
     */
    public void setWaitpointsId(Integer value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.hour_report.waitpoints_id</code>.
     */
    public Integer getWaitpointsId() {
        return (Integer) get(6);
    }

    /**
     * Setter for <code>public.hour_report.fuel</code>.
     */
    public void setFuel(Float value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.hour_report.fuel</code>.
     */
    public Float getFuel() {
        return (Float) get(7);
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
    // Record8 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row8<Long, Timestamp, Integer, Integer, Integer, Integer, Integer, Float> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row8<Long, Timestamp, Integer, Integer, Integer, Integer, Integer, Float> valuesRow() {
        return (Row8) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return HourReport.HOUR_REPORT.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field2() {
        return HourReport.HOUR_REPORT.TS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field3() {
        return HourReport.HOUR_REPORT.VEHICLE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field4() {
        return HourReport.HOUR_REPORT.DISTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field5() {
        return HourReport.HOUR_REPORT.WAITING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field6() {
        return HourReport.HOUR_REPORT.DRIVING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field7() {
        return HourReport.HOUR_REPORT.WAITPOINTS_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Float> field8() {
        return HourReport.HOUR_REPORT.FUEL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp component2() {
        return getTs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component3() {
        return getVehicleId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component4() {
        return getDistance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component5() {
        return getWaiting();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component6() {
        return getDriving();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component7() {
        return getWaitpointsId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Float component8() {
        return getFuel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value2() {
        return getTs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value3() {
        return getVehicleId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value4() {
        return getDistance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value5() {
        return getWaiting();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value6() {
        return getDriving();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value7() {
        return getWaitpointsId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Float value8() {
        return getFuel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HourReportRecord value1(Long value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HourReportRecord value2(Timestamp value) {
        setTs(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HourReportRecord value3(Integer value) {
        setVehicleId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HourReportRecord value4(Integer value) {
        setDistance(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HourReportRecord value5(Integer value) {
        setWaiting(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HourReportRecord value6(Integer value) {
        setDriving(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HourReportRecord value7(Integer value) {
        setWaitpointsId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HourReportRecord value8(Float value) {
        setFuel(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HourReportRecord values(Long value1, Timestamp value2, Integer value3, Integer value4, Integer value5, Integer value6, Integer value7, Float value8) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached HourReportRecord
     */
    public HourReportRecord() {
        super(HourReport.HOUR_REPORT);
    }

    /**
     * Create a detached, initialised HourReportRecord
     */
    public HourReportRecord(Long id, Timestamp ts, Integer vehicleId, Integer distance, Integer waiting, Integer driving, Integer waitpointsId, Float fuel) {
        super(HourReport.HOUR_REPORT);

        set(0, id);
        set(1, ts);
        set(2, vehicleId);
        set(3, distance);
        set(4, waiting);
        set(5, driving);
        set(6, waitpointsId);
        set(7, fuel);
    }
}
