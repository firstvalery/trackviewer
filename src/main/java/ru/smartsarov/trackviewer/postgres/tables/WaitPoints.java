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
import ru.smartsarov.trackviewer.postgres.tables.records.WaitPointsRecord;


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
public class WaitPoints extends TableImpl<WaitPointsRecord> {

    private static final long serialVersionUID = -543167131;

    /**
     * The reference instance of <code>public.wait_points</code>
     */
    public static final WaitPoints WAIT_POINTS = new WaitPoints();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<WaitPointsRecord> getRecordType() {
        return WaitPointsRecord.class;
    }

    /**
     * The column <code>public.wait_points.id</code>.
     */
    public final TableField<WaitPointsRecord, Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('wait_points_id_seq'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.wait_points.ts</code>.
     */
    public final TableField<WaitPointsRecord, Timestamp> TS = createField("ts", org.jooq.impl.SQLDataType.TIMESTAMP, this, "");

    /**
     * The column <code>public.wait_points.lng</code>.
     */
    public final TableField<WaitPointsRecord, BigDecimal> LNG = createField("lng", org.jooq.impl.SQLDataType.NUMERIC(13, 10), this, "");

    /**
     * The column <code>public.wait_points.lat</code>.
     */
    public final TableField<WaitPointsRecord, BigDecimal> LAT = createField("lat", org.jooq.impl.SQLDataType.NUMERIC(13, 10), this, "");

    /**
     * The column <code>public.wait_points.waiting</code>.
     */
    public final TableField<WaitPointsRecord, Integer> WAITING = createField("waiting", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.wait_points.waitpoints_id</code>.
     */
    public final TableField<WaitPointsRecord, Integer> WAITPOINTS_ID = createField("waitpoints_id", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * Create a <code>public.wait_points</code> table reference
     */
    public WaitPoints() {
        this(DSL.name("wait_points"), null);
    }

    /**
     * Create an aliased <code>public.wait_points</code> table reference
     */
    public WaitPoints(String alias) {
        this(DSL.name(alias), WAIT_POINTS);
    }

    /**
     * Create an aliased <code>public.wait_points</code> table reference
     */
    public WaitPoints(Name alias) {
        this(alias, WAIT_POINTS);
    }

    private WaitPoints(Name alias, Table<WaitPointsRecord> aliased) {
        this(alias, aliased, null);
    }

    private WaitPoints(Name alias, Table<WaitPointsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> WaitPoints(Table<O> child, ForeignKey<O, WaitPointsRecord> key) {
        super(child, key, WAIT_POINTS);
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
        return Arrays.<Index>asList(Indexes.WAIT_POINTS_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<WaitPointsRecord, Long> getIdentity() {
        return Keys.IDENTITY_WAIT_POINTS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<WaitPointsRecord> getPrimaryKey() {
        return Keys.WAIT_POINTS_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<WaitPointsRecord>> getKeys() {
        return Arrays.<UniqueKey<WaitPointsRecord>>asList(Keys.WAIT_POINTS_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WaitPoints as(String alias) {
        return new WaitPoints(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WaitPoints as(Name alias) {
        return new WaitPoints(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public WaitPoints rename(String name) {
        return new WaitPoints(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public WaitPoints rename(Name name) {
        return new WaitPoints(name, null);
    }
}
