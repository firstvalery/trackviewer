/*
 * This file is generated by jOOQ.
 */
package ru.smartsarov.trackviewer.postgres.tables;


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
import ru.smartsarov.trackviewer.postgres.tables.records.RegionRbRecord;


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
public class RegionRb extends TableImpl<RegionRbRecord> {

    private static final long serialVersionUID = 174410553;

    /**
     * The reference instance of <code>public.region_rb</code>
     */
    public static final RegionRb REGION_RB = new RegionRb();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RegionRbRecord> getRecordType() {
        return RegionRbRecord.class;
    }

    /**
     * The column <code>public.region_rb.id</code>.
     */
    public final TableField<RegionRbRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('region_rb_id_seq'::regclass)", org.jooq.impl.SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>public.region_rb.region</code>.
     */
    public final TableField<RegionRbRecord, String> REGION = createField("region", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * Create a <code>public.region_rb</code> table reference
     */
    public RegionRb() {
        this(DSL.name("region_rb"), null);
    }

    /**
     * Create an aliased <code>public.region_rb</code> table reference
     */
    public RegionRb(String alias) {
        this(DSL.name(alias), REGION_RB);
    }

    /**
     * Create an aliased <code>public.region_rb</code> table reference
     */
    public RegionRb(Name alias) {
        this(alias, REGION_RB);
    }

    private RegionRb(Name alias, Table<RegionRbRecord> aliased) {
        this(alias, aliased, null);
    }

    private RegionRb(Name alias, Table<RegionRbRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> RegionRb(Table<O> child, ForeignKey<O, RegionRbRecord> key) {
        super(child, key, REGION_RB);
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
        return Arrays.<Index>asList(Indexes.REGION_RB_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<RegionRbRecord, Integer> getIdentity() {
        return Keys.IDENTITY_REGION_RB;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<RegionRbRecord> getPrimaryKey() {
        return Keys.REGION_RB_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<RegionRbRecord>> getKeys() {
        return Arrays.<UniqueKey<RegionRbRecord>>asList(Keys.REGION_RB_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegionRb as(String alias) {
        return new RegionRb(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegionRb as(Name alias) {
        return new RegionRb(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public RegionRb rename(String name) {
        return new RegionRb(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public RegionRb rename(Name name) {
        return new RegionRb(name, null);
    }
}
