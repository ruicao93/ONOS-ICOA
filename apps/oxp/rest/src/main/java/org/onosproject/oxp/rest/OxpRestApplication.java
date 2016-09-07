package org.onosproject.oxp.rest;

import org.onlab.rest.AbstractWebApplication;

import java.util.Set;

/**
 * OXP Web Base.
 */
public class OxpRestApplication extends AbstractWebApplication {

    @Override
    public Set<Class<?>> getClasses(){
        return getClasses(OxpRestResource.class);
    }
}
