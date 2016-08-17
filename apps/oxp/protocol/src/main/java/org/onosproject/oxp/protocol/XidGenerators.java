package org.onosproject.oxp.protocol;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by cr on 16-4-9.
 */
public final class XidGenerators {
    private static final XidGenerator GLOBAL_XID_GENERATOR = new StandardXidGenerator();

    private XidGenerators(){

    }
    public static XidGenerator create() {
        return new StandardXidGenerator();
    }

    public static XidGenerator global() {
        return GLOBAL_XID_GENERATOR;
    }
}

class StandardXidGenerator implements XidGenerator {

    private final AtomicLong xidGen = new AtomicLong();
    static final long MAX_XID = 0xFFffFFffL;

    @Override
    public long nextXid() {
        long xid;
        do {
            xid = xidGen.incrementAndGet();
            if (xid > MAX_XID) {
                synchronized (this) {
                    if (xidGen.get() > MAX_XID) {
                        xidGen.set(0);
                    }
                }
            }
        } while (xid > MAX_XID);
        return xid;
    }

}
