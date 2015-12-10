package com.atlassian.configurable;

import java.util.Collections;
import java.util.Map;

import com.atlassian.annotations.PublicSpi;

/**
 * Interface to implement when you want to retrieve a list of choices for a ConfigurationProperty.
 */
@PublicSpi
public interface ValuesGenerator <K>
{
    /**
     * Implementation that returns no values at all.
     *
     * @since 28 Aug 2007 for JIRA v3.11
     */
    static final ValuesGenerator NONE = new ValuesGenerator()
    {

        public Map getValues(Map userParams)
        {
            return Collections.EMPTY_MAP;
        }
    };

    /**
     * Map of choices to use
     *
     * @param userParams Used to generate a relevant list of choices. e.g. may have some permissions in it
     * @return Map which contains a list of choices as key value pairs. Can be null.
     */
    Map<K, String> getValues(Map userParams);
}
