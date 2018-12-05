/*
 * This file is generated by jOOQ.
 */
package ru.smartsarov.trackviewer.postgres;


import javax.annotation.Generated;

import org.jooq.Sequence;
import org.jooq.impl.SequenceImpl;


/**
 * Convenience access to all sequences in public
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Sequences {

    /**
     * The sequence <code>public.region_rb_id_seq</code>
     */
    public static final Sequence<Integer> REGION_RB_ID_SEQ = new SequenceImpl<Integer>("region_rb_id_seq", Public.PUBLIC, org.jooq.impl.SQLDataType.INTEGER.nullable(false));

    /**
     * The sequence <code>public.tracking_data_id_seq</code>
     */
    public static final Sequence<Long> TRACKING_DATA_ID_SEQ = new SequenceImpl<Long>("tracking_data_id_seq", Public.PUBLIC, org.jooq.impl.SQLDataType.BIGINT.nullable(false));

    /**
     * The sequence <code>public.vehicle_dat_id_seq</code>
     */
    public static final Sequence<Short> VEHICLE_DAT_ID_SEQ = new SequenceImpl<Short>("vehicle_dat_id_seq", Public.PUBLIC, org.jooq.impl.SQLDataType.SMALLINT.nullable(false));
}
